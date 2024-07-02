package com.digital.pianoassist.feature_songs.presentation.songs_screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.digital.pianoassist.feature_songs.presentation.songs_screen.components.OrderSection
import com.digital.pianoassist.feature_songs.presentation.songs_screen.components.SongItem
import com.digital.pianoassist.feature_songs.presentation.util.Screen

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SongsScreen(
    navController: NavController,
    songsViewModel: SongsScreenViewModel = hiltViewModel(),
) {
    // the public read-only state that just helps the UI observe changes
    val state = songsViewModel.state.value
    val last30DaysAverage = songsViewModel.last30DaysAverageScore
    val last30DaysScores = songsViewModel.last30DaysScores.value
    val maxScore = songsViewModel.maxScore

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFE7C49C))
                .padding(16.dp)

        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Songs",
                    style = MaterialTheme.typography.headlineLarge
                )
                IconButton(
                    onClick = {
                        songsViewModel.onEvent(SongsScreenEvent.ToggleOrderSection)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Sort Section"
                    )
                }
            }
            // The animation for the sorting action
            AnimatedVisibility(
                visible = state.isOrderSectionVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                OrderSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    songOrder = state.songOrder,
                    onOrderChange = {
                        songsViewModel.onEvent(SongsScreenEvent.Order(it))
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.songs) { song ->
                    SongItem(
                        song = song,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { // when we click on a item we navigate to the recording screen
                                navController.navigate(
                                    Screen.RecordingScreen.route + "?songId=${song.id}"
                                )
                            },
                        onInfoClick = { song_it ->
                            songsViewModel.receiveLast30DaysAverageScore(
                                song_it
                            )
                            songsViewModel.receiveMaxScore(song_it)
                        },
                        last30DaysAverageScore = last30DaysAverage,
                        last30DaysScores = last30DaysScores,
                        maxScore = maxScore
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

}