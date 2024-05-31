package com.digital.pianoassist.feature_songs.presentation.recording_screen.fft

import kotlin.math.abs
import kotlin.math.pow

class NoteFinder(
    frequencies: DoubleArray,
    windowSize: Int,
    private val nrHarmonics: Int,
    private val hpsThreshold: Double,
    private val fftMagnitudeThresholdDb: Double,
    private val fftMagnitudeReleaseThresholdDb: Double
) {
    // 20*log10(magnitude / windowSize) < ThresholdDb
    private val fftMagnitudeThreshold = 10.0.pow(fftMagnitudeThresholdDb / 20) * windowSize
    private val fftMagnitudeReleaseThreshold =
        10.0.pow(fftMagnitudeReleaseThresholdDb / 20) * windowSize
    private val frequencyNotes = Array(frequencies.size) { "" }
    private val octaveErrorThreshold = 0.2
    private var previousWindowNotes: List<String>? = null

    init {
        for (i in frequencies.indices) {
            var diff = Double.MAX_VALUE
            for (pair in Notes.pianoKeysMap) {
                val newDiff = abs(frequencies[i] - pair.value)
                if (newDiff < diff) {
                    diff = newDiff
                    frequencyNotes[i] = pair.key
                }
            }
        }
    }

    fun findNotes(hps: DoubleArray, fftMagnitude: DoubleArray): List<String> {
        val notes = mutableListOf<String>()
        for (i in 1 until (hps.size - 1)) {
            if ((hps[i - 1] < hps[i]) && (hps[i] > hps[i + 1])) {
                // peak
                if (hps[i] < hpsThreshold) {
                    if (previousWindowNotes?.contains(frequencyNotes[i]) == true && fftMagnitude[i] > fftMagnitudeReleaseThreshold) {
                        notes.add(frequencyNotes[i])
                    }
                    continue
                }
                if (fftMagnitude[i] < fftMagnitudeThreshold) continue

                // Check for octave error correction
                val correctedIndex = correctOctaveErrors(hps, i)
                val correctedNote = frequencyNotes[correctedIndex]

                notes.add(correctedNote)
            }
        }
        // Update previous notes
        previousWindowNotes = notes.toList()

        return notes
    }

    private fun correctOctaveErrors(hps: DoubleArray, initialPitchIndex: Int): Int {
        val initialPitchAmplitude = hps[initialPitchIndex]
        val lowerOctaveIndex = initialPitchIndex / 2
        if (lowerOctaveIndex <= 0) return initialPitchIndex

        val lowerOctaveAmplitude = hps[lowerOctaveIndex]
        val amplitudeRatio = lowerOctaveAmplitude / initialPitchAmplitude

        val harmonicsRatioSum = (1..nrHarmonics).sumOf { harmonic ->
            val harmonicIndex = initialPitchIndex * harmonic
            if (harmonicIndex < hps.size) hps[harmonicIndex] / initialPitchAmplitude else 0.0
        }

        if (amplitudeRatio >= octaveErrorThreshold && harmonicsRatioSum / nrHarmonics >= octaveErrorThreshold) {
            return lowerOctaveIndex
        }

        return initialPitchIndex
    }
}