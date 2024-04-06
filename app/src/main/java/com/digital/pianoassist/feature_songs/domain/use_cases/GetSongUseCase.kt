package com.digital.pianoassist.feature_songs.domain.use_cases

import com.digital.pianoassist.feature_songs.domain.model.Song
import com.digital.pianoassist.feature_songs.domain.repository.SongRepository

class GetSongUseCase(
    private val repository: SongRepository
) {
    suspend operator fun invoke(id: Int): Song? {
        return repository.getSongById(id)
    }
}