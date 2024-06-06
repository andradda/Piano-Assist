package com.digital.pianoassist.feature_songs.domain.use_cases

import com.digital.pianoassist.feature_songs.domain.model.Song
import com.digital.pianoassist.feature_songs.domain.repository.SongRepository

class UpdateMaxScoreUseCase(
    private val repository: SongRepository
) {
    suspend operator fun invoke(song: Song, newScore: Int) {
        val updatedSong = song.copy(maxScore = newScore)
        println("updatedSong = $updatedSong")
        repository.updateSong(updatedSong)
    }
}