package com.digital.pianoassist.feature_songs.presentation.recording_screen.components

import android.content.Context
import java.io.File

// Implementing through an interface would help for testing purposes
interface AudioRecorder {
    suspend fun start(context: Context, outputFile: File)
    fun stop()
}