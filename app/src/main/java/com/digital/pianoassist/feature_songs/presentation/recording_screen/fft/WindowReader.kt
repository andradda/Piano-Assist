package com.digital.pianoassist.feature_songs.presentation.recording_screen.fft

interface WindowReader {
    fun iterateWindows(): Sequence<Window>
}