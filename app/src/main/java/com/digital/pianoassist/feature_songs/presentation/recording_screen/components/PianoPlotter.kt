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
import kotlin.math.max
import kotlin.math.min

class PianoPlotter(private val width: Int, private val height: Int, private val font: Typeface) {
    private val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(bitmap)
    val x0 = (width * 0.05).toInt()
    private val y0 = (height * 0.05).toInt()
    private val x1 = (width * 0.95).toInt()
    val y1 = (height * 0.95).toInt()
    val plot = Rect(x0, y0, x1, y1)
    var plotSeconds = 0
    private val noteIndexes = ArrayList<String>()
    val noteIndexMap = HashMap<String, Int>()
    var minNoteIndex = Notes.pianoKeysMap.size
    var maxNoteIndex = 0
    private val previousNotes = HashSet<String>()
    private val noteOccurrences = HashMap<String, ArrayList<Pair<Double, Double>>>()

    var maxSeconds = 0.0

    init {
        val paint = Paint().apply {
            typeface = font
            color = Color.WHITE
            isAntiAlias = true
        }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        for ((note, _) in Notes.pianoKeysMap.asSequence().sortedBy { it.value }) {
            noteIndexMap[note] = noteIndexes.size
            noteIndexes.add(note)
        }
    }

    fun add(window: Window, notes: List<String>) {
        maxSeconds = max(maxSeconds, window.endSeconds)
        for (note in notes) {
            val index = noteIndexMap[note] ?: 0
            minNoteIndex = min(minNoteIndex, index)
            maxNoteIndex = max(maxNoteIndex, index)

            if (!noteOccurrences.containsKey(note)) {
                noteOccurrences[note] = ArrayList()
            }
            val occurrencesList = noteOccurrences[note]!!
            if (!previousNotes.contains(note)) {
                previousNotes.add(note)
                occurrencesList.add(Pair(window.startSeconds, window.endSeconds))
            } else {
                occurrencesList[occurrencesList.lastIndex] =
                    Pair(occurrencesList[occurrencesList.lastIndex].first, window.endSeconds)
            }
        }
        previousNotes.removeAll { note -> !notes.contains(note) }
    }

    fun add(note: MidiNote) {
        maxSeconds = max(maxSeconds, note.endSeconds)
        val index = noteIndexMap[note.note] ?: 0
        minNoteIndex = min(minNoteIndex, index)
        maxNoteIndex = max(maxNoteIndex, index)

        if (!noteOccurrences.containsKey(note.note)) {
            noteOccurrences[note.note] = ArrayList()
        }
        val occurrencesList = noteOccurrences[note.note]!!
        occurrencesList.add(Pair(note.startSeconds, note.endSeconds))
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        plotAxis(canvas)
        plotNotes(canvas)
    }

    private fun plotAxis(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 1f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val fontY = (height * 0.985).toInt()

        plotSeconds = maxSeconds.toInt() + 1
        for (i in 0 until (plotSeconds + 1)) {
            val x = x0 + plot.width() * i / plotSeconds
            canvas.drawLine(
                x.toFloat(),
                y1.toFloat(),
                x.toFloat(),
                (y1 + height * 0.01).toFloat(),
                paint
            )
            val bounds = Rect()
            paint.getTextBounds("$i", 0, "$i".length, bounds)
            canvas.drawText("$i", (x - bounds.width() / 2).toFloat(), fontY.toFloat(), paint)

            val g2 = Paint().apply {
                color = Color.GRAY
            }
            canvas.drawLine(x.toFloat(), y0.toFloat(), x.toFloat(), y1.toFloat(), g2)
        }

        val nrNotes = maxNoteIndex - minNoteIndex + 1
        for (i in 0 until nrNotes) {
            val noteHeight = plot.height() / nrNotes
            val y = y1 - i * noteHeight
            canvas.drawLine(x0.toFloat(), y.toFloat(), x1.toFloat(), y.toFloat(), paint)
            val note = noteIndexes[i + minNoteIndex]

            val bounds = Rect()
            paint.getTextBounds(note, 0, note.length, bounds)
            canvas.drawText(
                note,
                (x0 / 2 - bounds.width() / 2).toFloat(),
                (y - noteHeight / 2 + bounds.height() / 2).toFloat(),
                paint
            )
        }

        canvas.drawRect(Rect(x0, y0, x1, y1), paint)
    }

    private fun plotNotes(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.rgb(192, 0, 0)
            style = Paint.Style.FILL
        }
        for ((note, occurrences) in noteOccurrences) {
            val noteIndex = noteIndexMap[note] ?: 0
            val nrNotes = maxNoteIndex - minNoteIndex + 1
            val noteHeight = plot.height() / nrNotes
            val y = y1 - (noteIndex - minNoteIndex) * noteHeight

            for ((start, end) in occurrences) {
                val startX = start / plotSeconds * plot.width() + x0
                val endX = end / plotSeconds * plot.width() + x0

                canvas.drawRect(
                    startX.toFloat(),
                    (y - noteHeight).toFloat(),
                    endX.toFloat(),
                    y.toFloat(),
                    paint
                )
            }
        }
    }
}
