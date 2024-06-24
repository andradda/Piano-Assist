package com.digital.pianoassist.feature_songs.data.repository

import com.digital.pianoassist.feature_songs.data.data_source.RecordingDao
import com.digital.pianoassist.feature_songs.domain.model.Recording
import com.digital.pianoassist.feature_songs.domain.model.Song
import com.digital.pianoassist.feature_songs.domain.repository.RecordingRepository

class RecordingRepositoryImpl(
    private val dao: RecordingDao
) : RecordingRepository {
    override suspend fun insertRecording(recording: Recording) {
        dao.insertRecording(recording)
    }

    override suspend fun getAllRecordingsForSong(song: Song): List<Recording>? {
        return song.id?.let { dao.getAllRecordingsOfSongLast30Days(it) }
    }
}