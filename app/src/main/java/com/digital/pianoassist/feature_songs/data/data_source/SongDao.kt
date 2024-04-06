package com.digital.pianoassist.feature_songs.data.data_source

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import com.digital.pianoassist.feature_songs.domain.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM song")
    fun getAllSongs(): Flow<List<Song>>

    @Query("SELECT * FROM song WHERE id = :id")
    suspend fun getSongById(id: Int): Song?

    @Update
    suspend fun updateSong(song: Song)

    // also filtering should go here
}