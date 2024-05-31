package com.digital.pianoassist.feature_songs.presentation.recording_screen.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Parcelable
import androidx.core.app.ActivityCompat
import com.digital.pianoassist.feature_songs.presentation.recording_screen.fft.AudioRecorderWindowReader
import com.digital.pianoassist.feature_songs.presentation.recording_screen.fft.FourierTransformer
import com.digital.pianoassist.feature_songs.presentation.recording_screen.fft.HpsCalculator
import com.digital.pianoassist.feature_songs.presentation.recording_screen.fft.MidiWindowProcessor
import com.digital.pianoassist.feature_songs.presentation.recording_screen.fft.NoteFinder
import com.digital.pianoassist.feature_songs.presentation.recording_screen.fft.Window
import com.digital.pianoassist.feature_songs.presentation.recording_screen.fft.WindowOverlapHalf
import com.digital.pianoassist.logDebug
import com.digital.pianoassist.logInformation
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

@Parcelize
class AndroidAudioRecorder : AudioRecorder, Parcelable {
    @IgnoredOnParcel
    private var recorder: AudioRecord? = null

    @IgnoredOnParcel
    val audioSource = MediaRecorder.AudioSource.MIC

    @IgnoredOnParcel
    val sampleRate = 44100

    @IgnoredOnParcel
    val nrHpsHarmonics = 3

    @IgnoredOnParcel
    val hpsThreshold = 40.0

    @IgnoredOnParcel
    val fftMagnitudeThresholdDb = -60.0

    @IgnoredOnParcel
    val fftMagnitudeReleaseThresholdDb = -90.0

    @IgnoredOnParcel
    private lateinit var midiWindows: MutableList<MutableList<String>>

    @IgnoredOnParcel
    val channelConfig = AudioFormat.CHANNEL_IN_MONO

    @IgnoredOnParcel
    val audioFormat = AudioFormat.ENCODING_PCM_FLOAT

    @IgnoredOnParcel
    val windowSize = 4096

    @IgnoredOnParcel
    val nrSeconds = 60

    @IgnoredOnParcel
    val recorderBufferSupplySizeBytes = nrSeconds * sampleRate * 4

    @IgnoredOnParcel
    private lateinit var fftTransformer: FourierTransformer

    @IgnoredOnParcel
    private lateinit var frequencies: DoubleArray

    @IgnoredOnParcel
    private lateinit var noteFinder: NoteFinder

    @IgnoredOnParcel
    private lateinit var recorderWindowReader: WindowOverlapHalf

    @IgnoredOnParcel
    var isRecording = true
    private fun createRecorder(context: Context): AudioRecord? {
        logInformation("enter createRecorder()")
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // Return null because we cannot create the recorder without permission
            logDebug("Creating recorder returned null")
            return null
        }
        logInformation("createRecorder() Returned the AudioRecord instance")
        return AudioRecord(
            audioSource,
            sampleRate,
            channelConfig,
            audioFormat,
            recorderBufferSupplySizeBytes
        )
    }

    override suspend fun start(context: Context, outputFile: File, midiInputStream: InputStream?) {
        logInformation("enter start()")
        recorder = createRecorder(context)
        logDebug("recorder = $recorder")

        val midiWindowProcessor = MidiWindowProcessor(windowSize, sampleRate)
        midiWindows = midiInputStream?.let { midiWindowProcessor.findMidiNotesInWindows(it) }!!

        if (recorder?.state != AudioRecord.STATE_INITIALIZED) {
            logDebug("start() recorder doesn't have the state initialized")
            return
        }
        fftTransformer = FourierTransformer()
        frequencies = FourierTransformer.calculateFrequencies(sampleRate, windowSize)
        noteFinder = NoteFinder(
            frequencies,
            windowSize,
            nrHpsHarmonics,
            hpsThreshold,
            fftMagnitudeThresholdDb,
            fftMagnitudeReleaseThresholdDb
        )

        recorder?.startRecording()
        logInformation("startRecording() recorder started recording")
        // writeDataToAudioFile(outputFile)
        processData()
    }

    private fun writeDataToAudioFile(outputFile: File) {
        logInformation("enter writeDataToAudioFile()")
        val outputStream = FileOutputStream(outputFile)

        // Wrap the FileOutputStream with a DataOutputStream
        val dataOutputStream = DataOutputStream(outputStream)

        // buffer - the min amount of data(samples) that the recorder can read at once
        val buffer = FloatArray(recorderBufferSupplySizeBytes)
        isRecording = true
        while (isRecording) {
            val readSize = recorder!!.read(buffer, 0, buffer.size, AudioRecord.READ_BLOCKING)
            logDebug("just read $readSize bytes")
            if (readSize > 0) {
                logDebug("wrote to the .raw file $readSize bytes")
                for (index in 0 until readSize) {
                    dataOutputStream.writeFloat(buffer[index])
                }
            } else {
                isRecording = false
            }
        }
        try {
            logDebug("trying to flush")
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            logDebug("IOException while closing outputStream")
            e.printStackTrace()
        }
    }

    private fun processData() {
        logInformation("enter processData()")
        recorderWindowReader = recorder?.let {
            AudioRecorderWindowReader(
                it,
                windowSize,
                windowSize * 1.0 / sampleRate // dt = number of seconds per window
            )
        }?.let { WindowOverlapHalf(it) }!!

//        for (window: Window in recorderWindowReader.iterateWindows()) {
//            val hammingWindow = window.hamming()
//            val fftMagnitude = fftTransformer.transform(hammingWindow)
//            val hps = HpsCalculator(nrHpsHarmonics, fftMagnitude).calculate()
//            val notes = noteFinder.findNotes(hps, fftMagnitude)
//            println("${String.format("%.4f", window.startSeconds)} : $notes")
//        }
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
        var intermmediateScore: Double
        var percentagePerWindow: Double
        var finalScore = 0.0

        val last10WindowAverages = ArrayDeque<Double>()
        var last10WindowSum = 0.0
        for ((window, notes) in eliminateNotesInSingleWindow(windowNotes)) {
            if (notes.isEmpty()) continue
            if (audioSubtract == 0.0) {
                audioSubtract = window.startSeconds
            }
            if (windowCount >= midiWindows.size) {
                windowCount++
                continue
            }
            val uniqueNotes = notes.toSet()
            val midiWindowNotes = midiWindows[windowCount].toSet()
            val union = uniqueNotes.union(midiWindowNotes)
            val intersection = uniqueNotes.intersect(midiWindowNotes)
            percentagePerWindow = ((intersection.size * 1.0 / union.size) * 100)
            finalScore += percentagePerWindow

            // logDebug("COMPARE $uniqueNotes with midi $midiWindowNotes")

            last10WindowAverages.addLast(percentagePerWindow)
            last10WindowSum += percentagePerWindow
            if (last10WindowAverages.size > 10) {
                val removedElement = last10WindowAverages.removeFirst()
                last10WindowSum -= removedElement
            }

            windowCount++
            // At each second (each 22 windows) the intermediate percentage score wil be displayed
            if (windowCount % 22 == 0) {
                println("Intermediate score: ${last10WindowSum / 10}")
            }


        }
        finalScore /= midiWindows.size
        println("Final score is $finalScore")
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

    override fun stop() {
        logInformation("enter stop()")
        logDebug("recorder = $recorder")
        recorder?.stop()
        recorder?.release()
        recorder = null
        isRecording = false
    }
}