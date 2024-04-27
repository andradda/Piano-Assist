package com.digital.pianoassist.feature_songs.presentation.recording_screen.fft

import android.media.AudioRecord
import com.digital.pianoassist.logDebug

class AudioRecorderWindowReader(
    private val recorder: AudioRecord,
    private val windowSize: Int,
    private val windowMilliseconds: Double
) : WindowReader {

    private var isRecording = true
    private val buffer = FloatArray(windowSize)

    override fun iterateWindows(): Sequence<Window> = sequence {
        var milliseconds = 0.0
        while (isRecording && recorder.state != AudioRecord.STATE_UNINITIALIZED) {
            val window = Window(windowSize, milliseconds)
            milliseconds += windowMilliseconds

            val readSize = try {
                recorder.read(buffer, 0, windowSize, AudioRecord.READ_BLOCKING)
            } catch (e: IllegalStateException) {
                logDebug("Recorder was stopped while waiting to read")
                isRecording = false
            }
            for (i in 0 until readSize as Int) {
                window.data[i] = buffer[i].toDouble()
            }
            logDebug("just read $readSize samples for $window")
            if (readSize > 0) {
                yield(window)
            } else {
                isRecording = false
            }
        }
    }
}
