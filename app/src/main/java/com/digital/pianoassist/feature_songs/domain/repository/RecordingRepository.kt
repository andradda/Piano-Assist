package com.digital.pianoassist.feature_songs.domain.repository

import com.digital.pianoassist.feature_songs.domain.model.Recording
import com.digital.pianoassist.feature_songs.domain.model.Song

interface RecordingRepository {

    suspend fun insertRecording(recording: Recording)
    suspend fun getAllRecordingsForSong(song: Song): List<Recording>?

}