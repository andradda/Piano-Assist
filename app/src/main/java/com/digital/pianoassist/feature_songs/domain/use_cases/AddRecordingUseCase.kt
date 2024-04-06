package com.digital.pianoassist.feature_songs.domain.use_cases

import com.digital.pianoassist.feature_songs.domain.model.InvalidRecordingException
import com.digital.pianoassist.feature_songs.domain.model.Recording
import com.digital.pianoassist.feature_songs.domain.repository.RecordingRepository
import com.digital.pianoassist.feature_songs.domain.repository.SongRepository
import kotlin.jvm.Throws

class AddRecordingUseCase(
    private val songRepository: SongRepository,
    private val recordingRepository: RecordingRepository
) {
    @Throws(InvalidRecordingException::class)
    suspend operator fun invoke(recording: Recording) {
        if(recording.songId.equals(null)) {
            throw InvalidRecordingException("The song id cannot be empty!")
        }
        recordingRepository.insertRecording(recording)
    }
}