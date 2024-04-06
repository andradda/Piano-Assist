package com.digital.pianoassist.feature_songs.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.digital.pianoassist.feature_songs.domain.model.Recording
import com.digital.pianoassist.feature_songs.domain.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recording")
    fun getAllRecordings(): Flow<List<Recording>>

    @Insert
    suspend fun insertRecording(recording: Recording)

    //@Query("SELECT * FROM recording WHERE ")
    //fun getAllRecordingsOfSong(song: Song): Flow<List<Recording>>

    // also filtering should go here
}