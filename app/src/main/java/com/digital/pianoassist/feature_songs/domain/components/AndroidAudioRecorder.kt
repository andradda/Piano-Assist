package com.digital.pianoassist.feature_songs.domain.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import com.digital.pianoassist.feature_songs.domain.fft.AudioRecorderWindowReader
import com.digital.pianoassist.feature_songs.domain.fft.WindowOverlapHalf
import com.digital.pianoassist.logDebug
import com.digital.pianoassist.logInformation

class AndroidAudioRecorder(
    private val sampleRate: Int,
    private val windowSize: Int,
) : AudioRecorder {

    private var recorder: AudioRecord? = null

    private val audioSource = MediaRecorder.AudioSource.MIC

    private val channelConfig = AudioFormat.CHANNEL_IN_MONO

    private val audioFormat = AudioFormat.ENCODING_PCM_FLOAT

    private val nrSeconds = 60

    private val recorderBufferSupplySizeBytes = nrSeconds * sampleRate * 4

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
        logInformation("context=$context")
        recorder = createRecorder(context)
        logDebug("recorder = $recorder")

        if (recorder?.state != AudioRecord.STATE_INITIALIZED) {
            logDebug("start() recorder doesn't have the state initialized")
            return
        }

        recorder?.startRecording()
        logInformation("startRecording() recorder started recording")
        // writeDataToAudioFile(outputFile)
        // processData()
    }

    /*
    private fun writeDataToAudioFile(outputFile: File) {
        logInformation("enter writeDataToAudioFile()")
        val outputStream = FileOutputStream(outputFile)

        // Wrap the FileOutputStream with a DataOutputStream
        val dataOutputStream = DataOutputStream(outputStream)

        // buffer - the min amount of data(samples) that the recorder can read at once
        val buffer = FloatArray(recorderBufferSupplySizeBytes)
        var isRecording = true
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
    */
    /*
    This function will internally create an AudioRecorderWindowReader to pass secretly the recorder instance.
    And it will just return the objects to be used by the UC as iterator.
     */
    override fun getWindowReader(): WindowOverlapHalf {
        logInformation("enter processData()")
        recorderWindowReader = recorder?.let {
            AudioRecorderWindowReader(
                it,
                windowSize,
                windowSize * 1.0 / sampleRate // dt = number of seconds per window
            )
        }?.let { WindowOverlapHalf(it) }!!
        return recorderWindowReader
    }

    override fun stop() {
        logInformation("enter stop()")
        recorder?.stop()
        recorder?.release()
        recorder = null
    }
}