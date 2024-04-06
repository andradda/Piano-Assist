package com.digital.pianoassist.feature_songs.presentation.songs_screen

import com.digital.pianoassist.feature_songs.domain.util.SongOrder

sealed class SongsScreenEvent {
    data class Order(val songsOrder: SongOrder) : SongsScreenEvent()
    object ToggleOrderSection : SongsScreenEvent()
}