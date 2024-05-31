package com.digital.pianoassist.feature_songs.presentation.recording_screen.fft

class WindowOverlapHalf(private val reader: WindowReader) : WindowReader {
    override fun iterateWindows(): Sequence<Window> = sequence {
        var previousWindow: Window? = null

        for (window in reader.iterateWindows()) {
            val size = window.data.size
            if (previousWindow == null) {
                previousWindow =
                    Window(size, window.startSeconds, (window.endSeconds + window.startSeconds) / 2)
                window.data.copyInto(previousWindow.data)
                yield(previousWindow.clone())
            } else {
                previousWindow.data.copyInto(previousWindow.data, 0, size / 2, size)
                window.data.copyInto(previousWindow.data, size / 2, 0, size / 2)
                previousWindow.startSeconds =
                    (previousWindow.startSeconds + window.startSeconds) / 2
                previousWindow.endSeconds = window.startSeconds;
                yield(previousWindow.clone())

                window.data.copyInto(previousWindow.data)
                previousWindow.startSeconds = window.startSeconds
                previousWindow.endSeconds = (window.startSeconds + window.endSeconds) / 2
                yield(previousWindow.clone())
            }
        }
    }
}