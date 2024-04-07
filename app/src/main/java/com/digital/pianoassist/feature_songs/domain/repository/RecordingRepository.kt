package com.digital.pianoassist.feature_songs.domain.repository

import com.digital.pianoassist.feature_songs.domain.model.Recording
import kotlinx.coroutines.flow.Flow

interface RecordingRepository {
    fun getAllRecordings(): Flow<List<Recording>>

    suspend fun insertRecording(recording: Recording)
}