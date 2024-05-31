package com.digital.pianoassist.feature_songs.domain.use_cases

import com.digital.pianoassist.feature_songs.domain.repository.SongRepository
import java.io.InputStream

class GetMidiStreamUseCase(
    private val repository: SongRepository
) {
    suspend operator fun invoke(id: Int): InputStream? {
        return repository.getMidiDataById(id)
    }
}