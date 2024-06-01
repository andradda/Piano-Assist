package com.digital.pianoassist.feature_songs.domain.fft

class Notes {

    companion object {
        val pianoKeysMap = mapOf<String, Double>(
            "A0" to 27.5,
            "A#0" to 29.14,
            "B0" to 30.87,
            "C1" to 32.7,
            "C#1" to 34.65,
            "D1" to 36.71,
            "D#1" to 38.89,
            "E1" to 41.2,
            "F1" to 43.65,
            "F#1" to 46.25,
            "G1" to 49.0,
            "G#1" to 51.91,
            "A1" to 55.0,
            "A#1" to 58.27,
            "B1" to 61.74,
            "C2" to 65.41,
            "C#2" to 69.3,
            "D2" to 73.42,
            "D#2" to 77.78,
            "E2" to 82.41,
            "F2" to 87.31,
            "F#2" to 92.5,
            "G2" to 98.0,
            "G#2" to 103.83,
            "A2" to 110.0,
            "A#2" to 116.54,
            "B2" to 123.47,
            "C3" to 130.81,
            "C#3" to 138.59,
            "D3" to 146.83,
            "D#3" to 155.56,
            "E3" to 164.81,
            "F3" to 174.61,
            "F#3" to 185.0,
            "G3" to 196.0,
            "G#3" to 207.65,
            "A3" to 220.0,
            "A#3" to 233.08,
            "B3" to 246.94,
            "C4" to 261.63,
            "C#4" to 277.18,
            "D4" to 293.66,
            "D#4" to 311.13,
            "E4" to 329.63,
            "F4" to 349.23,
            "F#4" to 369.99,
            "G4" to 392.0,
            "G#4" to 415.3,
            "A4" to 440.0,
            "A#4" to 466.16,
            "B4" to 493.88,
            "C5" to 523.25,
            "C#5" to 554.37,
            "D5" to 587.33,
            "D#5" to 622.25,
            "E5" to 659.25,
            "F5" to 698.46,
            "F#5" to 739.99,
            "G5" to 783.99,
            "G#5" to 830.61,
            "A5" to 880.0,
            "A#5" to 932.33,
            "B5" to 987.77,
            "C6" to 1046.5,
            "C#6" to 1108.73,
            "D6" to 1174.66,
            "D#6" to 1244.51,
            "E6" to 1318.51,
            "F6" to 1396.91,
            "F#6" to 1479.98,
            "G6" to 1567.98,
            "G#6" to 1661.22,
            "A6" to 1760.0,
            "A#6" to 1864.66,
            "B6" to 1975.53,
            "C7" to 2093.0,
            "C#7" to 2217.46,
            "D7" to 2349.32,
            "D#7" to 2489.02,
            "E7" to 2637.02,
            "F7" to 2793.83,
            "F#7" to 2959.96,
            "G7" to 3135.96,
            "G#7" to 3322.44,
            "A7" to 3520.0,
            "A#7" to 3729.31,
            "B7" to 3951.07,
            "C8" to 4186.01
        )

        val midiToPianoKeyMap = mapOf(
            21 to "A0",
            22 to "A#0",
            23 to "B0",
            24 to "C1",
            25 to "C#1",
            26 to "D1",
            27 to "D#1",
            28 to "E1",
            29 to "F1",
            30 to "F#",
            31 to "G1",
            32 to "G#1",
            33 to "A1",
            34 to "A#1",
            35 to "B1",
            36 to "C2",
            37 to "C#2",
            38 to "D2",
            39 to "D#2",
            40 to "E2",
            41 to "F2",
            42 to "F#2",
            43 to "G2",
            44 to "G#2",
            45 to "A2",
            46 to "A#2",
            47 to "B2",
            48 to "C3",
            49 to "C#3",
            50 to "D3",
            51 to "D#3",
            52 to "E3",
            53 to "F3",
            54 to "F#3",
            55 to "G3",
            56 to "G#3",
            57 to "A3",
            58 to "A#3",
            59 to "B3",
            60 to "C4",
            61 to "C#4",
            62 to "D4",
            63 to "D#4",
            64 to "E4",
            65 to "F4",
            66 to "F#4",
            67 to "G4",
            68 to "G#4",
            69 to "A4",
            70 to "A#4",
            71 to "B4",
            72 to "C5",
            73 to "C#5",
            74 to "D5",
            75 to "D#5",
            76 to "E5",
            77 to "F5",
            78 to "F#5",
            79 to "G5",
            80 to "G#5",
            81 to "A5",
            82 to "A#5",
            83 to "B5",
            84 to "C6",
            85 to "C#6",
            86 to "D6",
            87 to "D#6",
            88 to "E6",
            89 to "F6",
            90 to "F#6",
            91 to "G6",
            92 to "G#6",
            93 to "A6",
            94 to "A#6",
            95 to "B6",
            96 to "C7",
            97 to "C#7",
            98 to "D7",
            99 to "D#7",
            100 to "E7",
            101 to "F7",
            102 to "F#7",
            103 to "G7",
            104 to "G#7",
            105 to "A7",
            106 to "A#7",
            107 to "B7",
            108 to "C8",
            109 to "C#8",
            110 to "D8",
            111 to "D#8",
            112 to "E8",
            113 to "F8",
            114 to "F#8",
            115 to "G8",
            116 to "G#8",
            117 to "A8",
            118 to "A#8",
            119 to "B8",
            120 to "C9",
            121 to "C#9",
            122 to "D9",
            123 to "D#9",
            124 to "E9",
            125 to "F9",
            126 to "F#9",
            127 to "G9"
        )
    }
}