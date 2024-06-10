package com.digital.pianoassist.feature_songs.presentation.recording_screen.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import com.digital.pianoassist.feature_songs.domain.fft.MidiNote
import com.digital.pianoassist.feature_songs.domain.fft.Notes
import com.digital.pianoassist.feature_songs.domain.fft.Window
import com.digital.pianoassist.logInformation
import kotlin.math.max
import kotlin.math.min

class PianoPlotter(
    private val bitmap: Bitmap,
    private val plot: Rect,
    private val font: Typeface,
    private val plotSeconds: Double
) {
    private val width = bitmap.width
    private val height = bitmap.height
    private val x0 = plot.left
    private val y0 = plot.top
    private val x1 = plot.right
    private val y1 = plot.bottom

    private val noteIndexes = ArrayList<String>()
    private val noteIndexMap = HashMap<String, Int>()
    private var minNoteIndex = Notes.pianoKeysMap.size
    private var maxNoteIndex = 0
    private val previousNotes = HashSet<String>()
    private val midiNoteOccurrences = HashMap<String, ArrayList<Pair<Double, Double>>>()
    private val fftNoteOccurrences = HashMap<String, ArrayList<Pair<Double, Double>>>()

    init {
        for ((note, _) in Notes.pianoKeysMap.asSequence().sortedBy { it.value }) {
            noteIndexMap[note] = noteIndexes.size
            noteIndexes.add(note)
        }
    }

    /*
    This function draws over the bitmap the new notes from the piano performance
    taking into account the range established by the midi notes on the bitmap
     */
    fun add(window: Window, notes: List<String>): ArrayList<String> {
        logInformation("add fftNotes entered")
        val notesOutsideOfRange = ArrayList<String>()
        for (note in notes) {
            val index = noteIndexMap[note] ?: 0

            // TODO("send the notesOutsideOfRange to the screen to display them in the TextField")
            if (index < minNoteIndex || index > maxNoteIndex) {
                // The note is outside of the visible range of the screen
                notesOutsideOfRange.add(note)
                continue
            }

            if (!fftNoteOccurrences.containsKey(note)) {
                fftNoteOccurrences[note] = ArrayList()
            }
            val occurrencesList = fftNoteOccurrences[note]!!
            if (!previousNotes.contains(note)) {
                previousNotes.add(note)
                occurrencesList.add(Pair(window.startSeconds, window.endSeconds))
            } else {
                occurrencesList[occurrencesList.lastIndex] =
                    Pair(occurrencesList[occurrencesList.lastIndex].first, window.endSeconds)
            }
        }
        previousNotes.removeAll { note -> !notes.contains(note) }
        return notesOutsideOfRange
    }

    fun add(note: MidiNote) {
        val index = noteIndexMap[note.note] ?: 0
        minNoteIndex = min(minNoteIndex, index)
        maxNoteIndex = max(maxNoteIndex, index)

        if (!midiNoteOccurrences.containsKey(note.note)) {
            midiNoteOccurrences[note.note] = ArrayList()
        }
        val occurrencesList = midiNoteOccurrences[note.note]!!
        occurrencesList.add(Pair(note.startSeconds, note.endSeconds))
    }

    fun drawMidiPlot() {
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            typeface = font
            color = Color.parseColor("#F1D3B2")
            isAntiAlias = true
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        plotAxis(canvas)
        plotMidiNotes(canvas)
    }


    private fun plotAxis(canvas: Canvas) { // Drawing axes an labels for time and notes
        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 29f // Increase this value to make the font larger
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val fontY = (height * 0.985).toInt()

        for (i in 0 until (plotSeconds + 1).toInt()) {
            val x = x0 + plot.width() * i / plotSeconds
            canvas.drawLine( // Draw vertical line for the time
                x.toFloat(), y1.toFloat(), x.toFloat(), (y1 + height * 0.01).toFloat(), paint
            )


            // Place the time labels
            val bounds = Rect()
            textPaint.getTextBounds("$i", 0, "$i".length, bounds)
            canvas.drawText("$i", (x - bounds.width() / 2).toFloat(), fontY.toFloat(), textPaint)

            val g2 = Paint().apply {
                color = Color.GRAY
            }
            canvas.drawLine(x.toFloat(), y0.toFloat(), x.toFloat(), y1.toFloat(), g2)
        }

        val nrNotes = maxNoteIndex - minNoteIndex + 1
        for (i in 0 until nrNotes) {
            val noteHeight = plot.height() / nrNotes
            val y = y1 - i * noteHeight // Calculate y-coordinate for each horizontal line
            canvas.drawLine(x0.toFloat(), y.toFloat(), x1.toFloat(), y.toFloat(), paint)
            val note = noteIndexes[i + minNoteIndex]

            val bounds = Rect()
            textPaint.getTextBounds(note, 0, note.length, bounds)
            canvas.drawText(
                note,
                (x0 / 2 - bounds.width() / 2).toFloat(),
                (y - noteHeight / 2 + bounds.height() / 2).toFloat(),
                textPaint
            )
        }

        canvas.drawRect(Rect(x0, y0, x1, y1), paint) // Draw the outer rectangle (margins)
    }

    private fun plotMidiNotes(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.parseColor("#A42A04")// #A43820
            style = Paint.Style.FILL
        }
        for ((note, occurrences) in midiNoteOccurrences) {
            val noteIndex = noteIndexMap[note] ?: 0
            val nrNotes = maxNoteIndex - minNoteIndex + 1
            val noteHeight = plot.height() / nrNotes
            val y = y1 - (noteIndex - minNoteIndex) * noteHeight

            for ((start, end) in occurrences) {
                val startX = start / plotSeconds * plot.width() + x0
                val endX = end / plotSeconds * plot.width() + x0

                canvas.drawRect(
                    startX.toFloat(), (y - noteHeight).toFloat(), endX.toFloat(), y.toFloat(), paint
                )
            }
        }
    }

    fun drawFFTNotes() {
        val canvas = Canvas(bitmap)
        plotFFTNotes(canvas)
    }

    private fun plotFFTNotes(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.argb(40, 137, 196, 244)
            style = Paint.Style.FILL
        }
        for ((note, occurrences) in fftNoteOccurrences) {
            val noteIndex = noteIndexMap[note] ?: 0
            val nrNotes = maxNoteIndex - minNoteIndex + 1
            val noteHeight = plot.height() / nrNotes
            val y = y1 - (noteIndex - minNoteIndex) * noteHeight
            for ((start, end) in occurrences) {

                val startX = start / plotSeconds * plot.width() + x0
                val endX = end / plotSeconds * plot.width() + x0
                canvas.drawRect(
                    (startX).toFloat(),
                    (y - noteHeight).toFloat(),
                    (endX).toFloat(),
                    y.toFloat(),
                    paint
                )
            }
        }
    }
}
