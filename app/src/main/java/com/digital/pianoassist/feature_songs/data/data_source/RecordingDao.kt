package com.digital.pianoassist.feature_songs.data.data_source

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.digital.pianoassist.feature_songs.domain.model.Recording

@Dao
interface RecordingDao {
    @Insert
    suspend fun insertRecording(recording: Recording)

    // * 1000 to convert from s to ms (date field is in ms)
    @Query("SELECT * FROM Recording WHERE songId = :id AND date >= strftime('%s', 'now', '-30 days') * 1000")
    suspend fun getAllRecordingsOfSongLast30Days(id: Int): List<Recording>
}