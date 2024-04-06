package com.digital.pianoassist.feature_songs.data.repository

import com.digital.pianoassist.feature_songs.data.data_source.SongDao
import com.digital.pianoassist.feature_songs.domain.model.Song
import com.digital.pianoassist.feature_songs.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow

class SongRepositoryImpl(
    private val dao: SongDao
): SongRepository {
    override fun getAllSongs(): Flow<List<Song>> {
        return dao.getAllSongs()
    }

    override suspend fun getSongById(id: Int): Song? {
        return dao.getSongById(id)
    }

    override suspend fun updateSong(song: Song) {
        dao.updateSong(song)
    }
}