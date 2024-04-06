package com.digital.pianoassist.feature_songs.domain.util

sealed class OrderType {
    object Ascending: OrderType()
    object Descending: OrderType()
}