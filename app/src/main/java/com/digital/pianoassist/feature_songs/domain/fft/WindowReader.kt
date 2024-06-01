package com.digital.pianoassist.feature_songs.domain.fft

interface WindowReader {
    fun iterateWindows(): Sequence<Window>
}