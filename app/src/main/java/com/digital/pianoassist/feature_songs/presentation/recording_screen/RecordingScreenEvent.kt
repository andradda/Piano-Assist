package com.digital.pianoassist.feature_songs.presentation.recording_screen

/*
For every single ui action the user can make we have an event in this class
 */
sealed class RecordingScreenEvent {
    data object StartRecording : RecordingScreenEvent()
    data object StopRecording : RecordingScreenEvent()
}