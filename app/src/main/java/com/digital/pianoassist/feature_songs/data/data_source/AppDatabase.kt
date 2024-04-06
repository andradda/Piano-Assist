package com.digital.pianoassist.feature_songs.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.digital.pianoassist.feature_songs.domain.model.Recording
import com.digital.pianoassist.feature_songs.domain.model.Song

@Database(
    entities = [Song::class, Recording::class],
    version = 1
)
abstract class AppDatabase: RoomDatabase() {
    abstract val songDao: SongDao
    abstract val recordingDao: RecordingDao

    companion object {
        const val DATABASE_NAME = "piano_db"
    }
}