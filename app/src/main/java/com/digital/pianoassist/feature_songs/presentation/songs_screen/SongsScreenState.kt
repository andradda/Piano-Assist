package com.digital.pianoassist.feature_songs.presentation.songs_screen

import com.digital.pianoassist.feature_songs.domain.model.Song
import com.digital.pianoassist.feature_songs.domain.util.OrderType
import com.digital.pianoassist.feature_songs.domain.util.SongOrder

// this is a class will everything we want to listen on (as parameters)
// this state we keep in out view model so it also survives screen rotations
// this is a wrapper class on all the states we have - a giant ui screen state object
data class SongsScreenState(
    val songs: List<Song> = emptyList(),
    val songOrder: SongOrder = SongOrder.Title(OrderType.Ascending),
    val isOrderSectionVisible: Boolean = false // initially we don't show that section
)
