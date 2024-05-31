package com.digital.pianoassist.feature_songs.domain.repository

import com.digital.pianoassist.feature_songs.domain.model.Song
import kotlinx.coroutines.flow.Flow
import java.io.InputStream

// The repository gets the data from the Database verifies if the queries/API class are
// correct and forward the data to the use cases (which don't know where the repos gets the data from,
// they only want the data
/*
This interface is good for testing cause we can create fake versions of the repository
we don't want the real databases, apis to run test cases - cuz they should be quick
cause the use cases don't care if the data comes from api, local list, fake repository
they just get the data and do something with it


OBS: The actual implementation of the interface happens on the Data layer
 */
interface SongRepository {

    fun getAllSongs(): Flow<List<Song>>

    suspend fun getSongById(id: Int): Song?

    suspend fun updateSong(song: Song) // to update the maxScore

    suspend fun getMidiDataById(id: Int): InputStream?
}