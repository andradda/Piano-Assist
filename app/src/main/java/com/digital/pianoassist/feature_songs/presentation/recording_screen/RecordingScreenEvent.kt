package com.digital.pianoassist.feature_songs.presentation.recording_screen

import com.digital.pianoassist.feature_songs.domain.model.Song

/*
For every single ui action the user can make we have an event in this class
 */
sealed class RecordingScreenEvent {
    data class StartRecording(val song: Song) : RecordingScreenEvent()
}