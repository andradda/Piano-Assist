package com.digital.pianoassist.feature_songs.domain.use_cases

import com.digital.pianoassist.feature_songs.domain.model.Recording
import com.digital.pianoassist.feature_songs.domain.model.Song
import com.digital.pianoassist.feature_songs.domain.repository.RecordingRepository

class GetRecordingsUseCase(
    private val recordingRepository: RecordingRepository
) {
    suspend operator fun invoke(song: Song): List<Recording>? {
        return recordingRepository.getAllRecordingsForSong(song)
    }
}