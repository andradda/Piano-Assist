package com.digital.pianoassist.feature_songs.domain.util

sealed class SongOrder(val orderType: OrderType) { // here we have the different ways to order the list of songs
    class Title(orderType: OrderType): SongOrder(orderType)

    // create a copy function so we can pass the new order type
    // we keep the song order but we change the order type
    fun copy(newOrderType: OrderType): SongOrder {
        return when(this) {
            is Title -> Title(newOrderType)
        }
    }
}