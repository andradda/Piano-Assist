package com.digital.pianoassist.feature_songs.presentation.util

sealed class Screen(val route: String) {
    object SongsScreen : Screen("songs_screen")
    object RecordingScreen : Screen("recording_screen")
}