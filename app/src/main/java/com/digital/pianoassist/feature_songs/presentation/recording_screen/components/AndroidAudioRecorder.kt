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
import com.digital.pianoassist.feature_songs.presentation.recording_screen.fft.NoteFinder
import com.digital.pianoassist.feature_songs.presentation.recording_screen.fft.Window
import com.digital.pianoassist.feature_songs.presentation.recording_screen.fft.WindowOverlapHalf
import com.digital.pianoassist.logDebug
import com.digital.pianoassist.logInformation
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
class AndroidAudioRecorder : AudioRecorder, Parcelable {
    @IgnoredOnParcel
    private var recorder: AudioRecord? = null

    @IgnoredOnParcel
    val audioSource = MediaRecorder.AudioSource.MIC

    @IgnoredOnParcel
    val sampleRate = 44100

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

    override suspend fun start(context: Context) {
        logInformation("enter start()")
        recorder = createRecorder(context)
        logDebug("recorder = $recorder")

        if (recorder?.state != AudioRecord.STATE_INITIALIZED) {
            logDebug("start() recorder doesn't have the state initialized")
            return
        }
        fftTransformer = FourierTransformer()
        frequencies = FourierTransformer.calculateFrequencies(sampleRate, windowSize)
        noteFinder = NoteFinder(frequencies, windowSize)

        recorder?.startRecording()
        logInformation("startRecording() recorder started recording")
        processData()
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

        for (window: Window in recorderWindowReader.iterateWindows()) {
            val hammingWindow = window.hamming()
            val fftMagnitude = fftTransformer.transform(hammingWindow)
            val hps = HpsCalculator(3, fftMagnitude).calculate()
            val notes = noteFinder.findNotes(hps, fftMagnitude)
            println("${String.format("%.4f", window.milliseconds)} : $notes")
        }
    }

    override fun stop() {
        logInformation("enter stop()")
        logDebug("recorder = $recorder")
        recorderWindowReader
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}