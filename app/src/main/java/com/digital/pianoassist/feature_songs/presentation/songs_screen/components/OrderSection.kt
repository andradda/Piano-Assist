package com.digital.pianoassist.feature_songs.presentation.songs_screen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.digital.pianoassist.feature_songs.domain.util.OrderType
import com.digital.pianoassist.feature_songs.domain.util.SongOrder

@Composable
fun OrderSection(
    modifier: Modifier = Modifier,
    songOrder: SongOrder = SongOrder.Title(OrderType.Ascending),
    onOrderChange: (SongOrder) -> Unit // we pass the new order as a callback function to the parent composable
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
                text = "Title",
                selected = songOrder is SongOrder.Title,
                onSelect = { onOrderChange(SongOrder.Title(songOrder.orderType)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        // now second row is for the orderType ASC/DESC
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
                text = "Ascending",
                selected = songOrder.orderType is OrderType.Ascending,

                // we want to keep the current songOrder but change the orderType
                // and we don't want to make the orderType mutable (not good practice)
                // so we create a copy function for SongOrder
                // onSelect = { onOrderChange(SongOrder.Title(songOrder.orderType)) }
                onSelect = {
                    onOrderChange(songOrder.copy(OrderType.Ascending))
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            DefaultRadioButton(
                text = "Descending",
                selected = songOrder.orderType is OrderType.Descending,
                onSelect = { onOrderChange(songOrder.copy(OrderType.Descending)) }
            )
        }
    }
}