package com.digital.pianoassist.feature_songs.presentation.recording_screen.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Parcelable
import androidx.core.app.ActivityCompat
import com.digital.pianoassist.logDebug
import com.digital.pianoassist.logInformation
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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
    val bufferSizeRecording =
        AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

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
            bufferSizeRecording
        )
    }

    override suspend fun start(context: Context, outputFile: File) {
        logInformation("enter start()")
        recorder = createRecorder(context)
        logDebug("recorder = $recorder")

        if (recorder!!.state != AudioRecord.STATE_INITIALIZED) {
            logDebug("start() recorder doesn't have the state initialized")
            return
        }

        recorder!!.startRecording()
        isRecording = true
        logInformation("startRecording() recorder started recording")
        writeDataToAudioFile(outputFile)
    }

    private fun writeDataToAudioFile(outputFile: File) {
        logInformation("enter writeDataToAudioFile()")
        val outputStream = FileOutputStream(outputFile)

        // Wrap the FileOutputStream with a DataOutputStream
        val dataOutputStream = DataOutputStream(outputStream)

        // buffer - the min amount of data(samples) that the recorder can read at once
        val buffer = FloatArray(bufferSizeRecording)

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

    override fun stop() {
        logInformation("enter stop()")
        logDebug("recorder = $recorder")
        isRecording = false
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}