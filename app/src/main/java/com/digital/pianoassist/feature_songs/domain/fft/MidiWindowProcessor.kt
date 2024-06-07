package com.digital.pianoassist.feature_songs.domain.fft

import java.io.InputStream

class MidiWindowProcessor(
    windowSize: Int,
    sampleRate: Int
) {
    private val windowDuration: Double

    init {
        this.windowDuration = windowSize * 1.0 / sampleRate
    }

    fun findMidiNotesInWindows(inputStream: InputStream): Pair<List<MidiNote>, MutableList<MutableList<String>>> {
        val midiNotes = mutableListOf<MidiNote>()
        val midiNoteWindows = mutableListOf<MutableList<String>>()
        var firstNoteStartTime: Double? = null
        var totalNotes = 0

        for (note in readMIDI(inputStream)) { // taken one by one from the sequence
            totalNotes++
            if (firstNoteStartTime == null) {
                firstNoteStartTime = note.startSeconds
            }

            val adjustedNote = MidiNote(
                note = note.note,
                startSeconds = note.startSeconds - firstNoteStartTime,
                endSeconds = note.endSeconds - firstNoteStartTime
            )
            midiNotes.add(adjustedNote)
            val firstWindow = (adjustedNote.startSeconds / windowDuration).toInt()
            val lastWindow = (adjustedNote.endSeconds / windowDuration).toInt()

            for (i in firstWindow..lastWindow) {
                while (i >= midiNoteWindows.size) {
                    midiNoteWindows.add(mutableListOf())
                }
                midiNoteWindows[i].add(adjustedNote.note)
            }
        }
        return Pair(midiNotes, midiNoteWindows)
    }
}