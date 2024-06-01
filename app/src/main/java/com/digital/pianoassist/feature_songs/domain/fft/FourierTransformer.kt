package com.digital.pianoassist.feature_songs.domain.fft

import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import kotlin.math.sqrt

class FourierTransformer {
    private val fftTransformer = FastFourierTransformer(DftNormalization.STANDARD)

    fun transform(window: Window): DoubleArray {
        val fftResult = fftTransformer.transform(window.data, TransformType.FORWARD)
        val result = DoubleArray(window.data.size / Nyquist + 1)
        for (i in result.indices) {
            val it = fftResult[i]
            result[i] = sqrt(it.real * it.real + it.imaginary * it.imaginary)
        }
        return result
    }

    companion object {
        const val Nyquist = 2
        fun calculateFrequencies(sampleRate: Int, windowSize: Int): DoubleArray {
            val frequencyResolution = sampleRate * 1.0 / windowSize
            val frequencies = DoubleArray(windowSize / Nyquist + 1) { it * frequencyResolution }
            return frequencies
        }
    }
}