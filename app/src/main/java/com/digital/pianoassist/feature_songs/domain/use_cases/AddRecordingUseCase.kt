package com.digital.pianoassist.feature_songs.domain.use_cases

import com.digital.pianoassist.feature_songs.domain.model.InvalidRecordingException
import com.digital.pianoassist.feature_songs.domain.model.Recording
import com.digital.pianoassist.feature_songs.domain.repository.RecordingRepository
import java.util.Date

class AddRecordingUseCase(
    private val recordingRepository: RecordingRepository
) {
    @Throws(InvalidRecordingException::class)
    suspend operator fun invoke(idSong: Int, finalScore: Int) {
        val recording =
            Recording(songId = idSong, score = finalScore, date = Date())
        if (recording.songId.equals(null)) {
            throw InvalidRecordingException("The song id cannot be empty!")
        }
        recordingRepository.insertRecording(recording)
    }
}