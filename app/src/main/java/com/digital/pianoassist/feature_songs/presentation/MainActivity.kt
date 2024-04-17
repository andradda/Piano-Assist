package com.digital.pianoassist.feature_songs.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.digital.pianoassist.feature_songs.presentation.recording_screen.RecordingScreen
import com.digital.pianoassist.feature_songs.presentation.songs_screen.SongsScreen
import com.digital.pianoassist.feature_songs.presentation.util.Screen
import com.digital.pianoassist.ui.theme.PianoAssistTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // so we can inject our models with dagger hilt in this activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PianoAssistTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Screen.SongsScreen.route
                    ) {
                        composable(route = Screen.SongsScreen.route) {
                            SongsScreen(navController = navController)
                        }
                        composable(
                            route = Screen.RecordingScreen.route + "?songId={songId}",
                            arguments = listOf(
                                navArgument(
                                    name = "songId"
                                ) {
                                    type = NavType.IntType
                                    defaultValue = -1 // in case there is no song clicked
                                }
                            )
                        ) {
                            RecordingScreen(
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}