package com.digital.pianoassist.feature_songs.domain.use_cases

import android.content.Context
import com.digital.pianoassist.feature_songs.domain.components.AndroidAudioRecorder
import com.digital.pianoassist.feature_songs.domain.fft.FourierTransformer
import com.digital.pianoassist.feature_songs.domain.fft.HpsCalculator
import com.digital.pianoassist.feature_songs.domain.fft.MidiNote
import com.digital.pianoassist.feature_songs.domain.fft.MidiWindowProcessor
import com.digital.pianoassist.feature_songs.domain.fft.NoteFinder
import com.digital.pianoassist.feature_songs.domain.fft.Window
import com.digital.pianoassist.feature_songs.domain.fft.WindowOverlapHalf
import com.digital.pianoassist.feature_songs.domain.util.roundToDecimals
import com.digital.pianoassist.logDebug
import com.digital.pianoassist.logInformation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject

class PerformRecordingUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val sampleRate = 44100
    val windowSize = 4096
    val nrHpsHarmonics = 3
    val hpsThreshold = 40.0
    val fftMagnitudeThresholdDb = -60.0
    val fftMagnitudeReleaseThresholdDb = -90.0
    private var midiWindows: MutableList<MutableList<String>>? = null
    private lateinit var androidRecorder: AndroidAudioRecorder

    private var finalScore: Double = 0.0
    fun createMidiModel(midiInputStream: InputStream): List<MidiNote> {
        val midiWindowProcessor = MidiWindowProcessor(windowSize, sampleRate)
        val (midiNotes, midiWindows) = midiInputStream.let {
            midiWindowProcessor.findMidiNotesInWindows(it)
        }
        this.midiWindows = midiWindows
        return midiNotes
    }

    fun startRecording(
        intermediateScoreCallback: (Double) -> Unit,
        newNotesCallback: (Pair<Window, List<String>>) -> Unit,
        recordingComplete: CompletableDeferred<Unit>
    ) {
        androidRecorder = AndroidAudioRecorder(sampleRate, windowSize)
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.IO) {
                logDebug("Coroutine started the recorder")
                androidRecorder.start(context)
            }
            val recorderWindowReader = androidRecorder.getWindowReader()
            midiWindows?.let {
                performFFT(recorderWindowReader, it, intermediateScoreCallback, newNotesCallback)
            }
            recordingComplete.complete(Unit) // Signal that the recording is complete
        }
    }

    private fun performFFT(
        recorderWindowReader: WindowOverlapHalf,
        midiWindows: MutableList<MutableList<String>>,
        intermediateScoreCallback: (Double) -> Unit,
        newNotesCallback: (Pair<Window, List<String>>) -> Unit
    ) {
        val fftTransformer = FourierTransformer()
        val frequencies = FourierTransformer.calculateFrequencies(sampleRate, windowSize)
        val noteFinder = NoteFinder(
            frequencies,
            windowSize,
            nrHpsHarmonics,
            hpsThreshold,
            fftMagnitudeThresholdDb,
            fftMagnitudeReleaseThresholdDb
        )
        val windowNotes: Sequence<Pair<Window, List<String>>> = sequence {
            for (window: Window in recorderWindowReader.iterateWindows().map { it.hamming() }) {
                val fftMagnitude = fftTransformer.transform(window)
                val hps = HpsCalculator(nrHpsHarmonics, fftMagnitude).calculate()
                val notes = noteFinder.findNotes(hps, fftMagnitude)
                yield(Pair(window, notes))
            }
        }
        var windowCount = 0
        var audioSubtract = 0.0
        var percentagePerWindow: Double
        val last10WindowAverages = ArrayDeque<Double>()
        var last10WindowSum = 0.0

        for ((window, notes) in eliminateNotesInSingleWindow(windowNotes)) {
            if (notes.isEmpty()) continue
            if (audioSubtract == 0.0) {
                audioSubtract = window.startSeconds
            }

            val shiftedWindow = window.clone()
            shiftedWindow.startSeconds -= audioSubtract
            shiftedWindow.endSeconds -= audioSubtract

            newNotesCallback(Pair(shiftedWindow, notes))

            val uniqueNotes = notes.toSet()
            val midiWindowNotes =
                if (midiWindows.size > windowCount) midiWindows[windowCount].toSet() else setOf()
            val union = uniqueNotes.union(midiWindowNotes)
            val intersection = uniqueNotes.intersect(midiWindowNotes)
            logDebug("union: $union and intersection: $intersection")

            percentagePerWindow = if (union.isNotEmpty()) {
                ((intersection.size * 1.0 / union.size) * 100)
            } else {
                0.0
            }
            finalScore += percentagePerWindow

            last10WindowAverages.addLast(percentagePerWindow)
            last10WindowSum += percentagePerWindow
            if (last10WindowAverages.size > 10) {
                val removedElement = last10WindowAverages.removeFirst()
                last10WindowSum -= removedElement
            }
            windowCount++
            if (windowCount % 22 == 0) {
                logDebug("Intermediate score: ${roundToDecimals(last10WindowSum / 10, 2)}")
                intermediateScoreCallback(roundToDecimals(last10WindowSum / 10, 2))
            }
        }
        finalScore /= midiWindows.size
        logDebug("Final score is ${roundToDecimals(finalScore, 2)}")
    }

    private fun eliminateNotesInSingleWindow(windowNotes: Sequence<Pair<Window, List<String>>>): Sequence<Pair<Window, List<String>>> =
        sequence {
            for (list in windowNotes.windowed(3, 1)) {
                yield(
                    Pair(
                        list[1].first,
                        list[1].second.filter {
                            list[0].second.contains(it) || list[2].second.contains(
                                it
                            )
                        })
                )
            }
        }

    fun stopRecording() {
        logInformation("stopRecording() entered")
        androidRecorder.stop()
    }

    fun receiveFinalScore(): Double {
        logInformation("receiveFinalScore() entered")
        return roundToDecimals(finalScore, 2)
    }
}