package com.digital.pianoassist.feature_songs.data.repository

import com.digital.pianoassist.feature_songs.data.data_source.RecordingDao
import com.digital.pianoassist.feature_songs.domain.model.Recording
import com.digital.pianoassist.feature_songs.domain.repository.RecordingRepository
import kotlinx.coroutines.flow.Flow

class RecordingRepositoryImpl(
    private val dao: RecordingDao
):RecordingRepository {
    override fun getAllRecordings(): Flow<List<Recording>> {
        return dao.getAllRecordings()
    }

    override suspend fun insertRecording(recording: Recording) {
        dao.insertRecording(recording)
    }
}