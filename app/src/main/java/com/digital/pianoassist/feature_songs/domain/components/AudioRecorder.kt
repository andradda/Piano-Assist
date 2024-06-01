package com.digital.pianoassist.feature_songs.domain.components

import android.content.Context
import com.digital.pianoassist.feature_songs.domain.fft.WindowOverlapHalf

// Implementing through an interface would help for testing purposes
interface AudioRecorder {
    suspend fun start(context: Context)
    fun getWindowReader(): WindowOverlapHalf
    fun stop()
}