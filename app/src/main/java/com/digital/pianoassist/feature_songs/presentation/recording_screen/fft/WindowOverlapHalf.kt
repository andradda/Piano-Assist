package com.digital.pianoassist.feature_songs.presentation.recording_screen.fft

class WindowOverlapHalf(private val reader: WindowReader) : WindowReader {
    override fun iterateWindows(): Sequence<Window> = sequence {
        var previousWindow: Window? = null

        for (window in reader.iterateWindows()) {
            val size = window.data.size
            if (previousWindow == null) {
                previousWindow = Window(size, window.milliseconds)
                window.data.copyInto(previousWindow.data)
                yield(previousWindow.clone())
            } else {
                previousWindow.data.copyInto(previousWindow.data, 0, size / 2, size)
                window.data.copyInto(previousWindow.data, size / 2, 0, size / 2)
                previousWindow.milliseconds =
                    (previousWindow.milliseconds + window.milliseconds) / 2
                yield(previousWindow.clone())

                window.data.copyInto(previousWindow.data)
                previousWindow.milliseconds = window.milliseconds
                yield(previousWindow.clone())
            }
        }
    }
}