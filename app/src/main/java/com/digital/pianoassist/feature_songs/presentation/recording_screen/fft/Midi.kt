package com.digital.pianoassist.feature_songs.presentation.recording_screen.fft

import com.digital.pianoassist.logInformation
import com.leff.midi.MidiFile
import com.leff.midi.MidiTrack
import com.leff.midi.event.ChannelEvent
import com.leff.midi.event.MidiEvent
import com.leff.midi.event.NoteOff
import com.leff.midi.event.NoteOn
import com.leff.midi.event.meta.Tempo
import java.io.InputStream

class TickDurationCalculator {
    var lastTime = 0.0
    var lastTempoChangeTick = 0L
    var lastTempo = 0.0

    fun add(tick: Long, tickDurationSeconds: Double) {
        println("\tSet tempo at tick $tick $tickDurationSeconds ")
        lastTime = getSeconds(tick)
        lastTempoChangeTick = tick
        lastTempo = tickDurationSeconds
    }

    fun getSeconds(tick: Long): Double {
        return (tick - lastTempoChangeTick) * lastTempo + lastTime
    }
}


class MidiNote(val note: String, val startSeconds: Double, val endSeconds: Double)

fun readMIDI(input: InputStream): Sequence<MidiNote> = sequence {
    val tickDurationCalculator = TickDurationCalculator()
    val notesStart = HashMap<String, Double>()

    try {
        val sequence = MidiFile(input)
        val ticksPerQuarterNote = sequence.resolution // in this case PPQ (ticks per beat)
        val defaultTempo = 500000 * 1.2 // this is if there are no set_tempo events (microseconds)
        tickDurationCalculator.add(0, defaultTempo / 1000000.0 / ticksPerQuarterNote)

        val sortedEvents = mutableListOf<MidiEvent>()

        for (track: MidiTrack in sequence.tracks) {
            for (event: MidiEvent in track.events) {
                sortedEvents.add(event)
            }
        }
        sortedEvents.sortBy { it.tick }

        for (event in sortedEvents) {
            if (event is Tempo) {
                val tempo = event.mpqn
                tickDurationCalculator.add(
                    event.tick,
                    tempo / 1000000.0 / ticksPerQuarterNote
                )
            }
            if (event is ChannelEvent) {
                val seconds = tickDurationCalculator.getSeconds(event.tick)
                if (event is NoteOn) {
                    val pressedKey = event.noteValue
                    logInformation("ON key ${Notes.midiToPianoKeyMap[pressedKey]} at tick=${event.tick}  $seconds")
                    val note = Notes.midiToPianoKeyMap[pressedKey]!!
                    notesStart[note] = seconds
                } else if (event is NoteOff) {
                    val releasedKey = event.noteValue
                    val note = Notes.midiToPianoKeyMap[releasedKey]!!
                    yield(MidiNote(note, notesStart[note]!!, seconds))
                    logInformation("OFF key ${Notes.midiToPianoKeyMap[releasedKey]} at tick=${event.tick} $seconds")
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
