package com.digital.pianoassist.feature_songs.domain.fft

class HpsCalculator(private val nrHarmonics: Int, private val base: DoubleArray) {
    fun calculate(): DoubleArray {
        val hps = base.clone()

        for (h in 1 until nrHarmonics) {
            for (i in base.indices) {
                var sum = 0.0
                for (index in 0..h) {
                    sum += getBase((h + 1) * i + index)
                }
                hps[i] *= sum / (h + 1)
            }
        }
        return hps
    }

    private fun getBase(index: Int): Double {
        if (index >= base.size) {
            return 0.0
        }
        return base[index]
    }
}