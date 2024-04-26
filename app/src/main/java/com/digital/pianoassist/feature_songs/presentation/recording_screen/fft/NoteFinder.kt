package com.digital.pianoassist.feature_songs.presentation.recording_screen.fft

import kotlin.math.abs
import kotlin.math.pow

class NoteFinder(frequencies: DoubleArray, windowSize: Int) {
    private val hpsThreshold = 1.0
    private val fftMagnitudeThresholdDb = -70.0

    // 20 * log10(magnitude / windowSize) < ThresholdDb
    private val fftMagnitudeThreshold = 10.0.pow(fftMagnitudeThresholdDb / 20) * windowSize
    private val frequencyNotes = Array(frequencies.size) { "" }

    init {
        // each frequency is taken and it calculated based on which is it's corresponding bin
        for (i in frequencies.indices) {
            var diff = Double.MAX_VALUE
            for (pair in pianoKeysMap) {
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
                if (hps[i] < hpsThreshold) continue
                if (fftMagnitude[i] < fftMagnitudeThreshold) continue

                notes.add(frequencyNotes[i])
            }
        }

        return notes
    }

    companion object {
        val pianoKeysMap = mapOf(
            "A0" to 27.5,
            "Bb0" to 29.14,
            "B0" to 30.87,
            "C1" to 32.7,
            "Db1" to 34.65,
            "D1" to 36.71,
            "Eb1" to 38.89,
            "E1" to 41.2,
            "F1" to 43.65,
            "Gb1" to 46.25,
            "G1" to 49.0,
            "Ab1" to 51.91,
            "A1" to 55.0,
            "Bb1" to 58.27,
            "B1" to 61.74,
            "C2" to 65.41,
            "Db2" to 69.3,
            "D2" to 73.42,
            "Eb2" to 77.78,
            "E2" to 82.41,
            "F2" to 87.31,
            "Gb2" to 92.5,
            "G2" to 98.0,
            "Ab2" to 103.83,
            "A2" to 110.0,
            "Bb2" to 116.54,
            "B2" to 123.47,
            "C3" to 130.81,
            "Db3" to 138.59,
            "D3" to 146.83,
            "Eb3" to 155.56,
            "E3" to 164.81,
            "F3" to 174.61,
            "Gb3" to 185.0,
            "G3" to 196.0,
            "Ab3" to 207.65,
            "A3" to 220.0,
            "Bb3" to 233.08,
            "B3" to 246.94,
            "C4" to 261.63,
            "Db4" to 277.18,
            "D4" to 293.66,
            "Eb4" to 311.13,
            "E4" to 329.63,
            "F4" to 349.23,
            "Gb4" to 369.99,
            "G4" to 392.0,
            "Ab4" to 415.3,
            "A4" to 440.0,
            "Bb4" to 466.16,
            "B4" to 493.88,
            "C5" to 523.25,
            "Db5" to 554.37,
            "D5" to 587.33,
            "Eb5" to 622.25,
            "E5" to 659.25,
            "F5" to 698.46,
            "Gb5" to 739.99,
            "G5" to 783.99,
            "Ab5" to 830.61,
            "A5" to 880.0,
            "Bb5" to 932.33,
            "B5" to 987.77,
            "C6" to 1046.5,
            "Db6" to 1108.73,
            "D6" to 1174.66,
            "Eb6" to 1244.51,
            "E6" to 1318.51,
            "F6" to 1396.91,
            "Gb6" to 1479.98,
            "G6" to 1567.98,
            "Ab6" to 1661.22,
            "A6" to 1760.0,
            "Bb6" to 1864.66,
            "B6" to 1975.53,
            "C7" to 2093.0,
            "Db7" to 2217.46,
            "D7" to 2349.32,
            "Eb7" to 2489.02,
            "E7" to 2637.02,
            "F7" to 2793.83,
            "Gb7" to 2959.96,
            "G7" to 3135.96,
            "Ab7" to 3322.44,
            "A7" to 3520.0,
            "Bb7" to 3729.31,
            "B7" to 3951.07,
            "C8" to 4186.01
        )
    }
}