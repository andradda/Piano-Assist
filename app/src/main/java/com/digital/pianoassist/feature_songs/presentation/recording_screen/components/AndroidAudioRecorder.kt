package com.digital.pianoassist.feature_songs.presentation.recording_screen.components

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Parcelable
import com.digital.pianoassist.logDebug
import com.digital.pianoassist.logInformation
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.FileOutputStream

@Parcelize
class AndroidAudioRecorder(
    //private val context: Context
) : AudioRecorder, Parcelable {
    private var recorder: MediaRecorder? = null;


    /*
    Starting from Android 12 (API level 31) there have been changes in how 'MediaRecorder' is
    instantiate due to privacy and security enhancements. Prior to this version, the 'MediaRecorder'
    wouldn't need any arguments.
     */
    private fun createRecorder(context: Context): MediaRecorder {
        logInformation("enter createRecorder()")
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()

    }

    override fun start(context: Context, outputFile: File) {
        logInformation("enter start()")
        logDebug("recorder = ${recorder}")
        createRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) // smaller size, low quality
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare() // prepare to record
            start()

            recorder = this // global variable update
            logDebug("recorder = ${recorder}")
        }
    }

    override fun stop() {
        logInformation("enter stop()")
        logDebug("recorder = ${recorder}")
        recorder?.stop()
        recorder?.reset()
        recorder = null
    }
}