package com.digital.pianoassist.feature_songs.presentation.recording_screen

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.digital.pianoassist.feature_songs.domain.fft.MidiNote
import com.digital.pianoassist.feature_songs.domain.fft.Window
import com.digital.pianoassist.feature_songs.presentation.recording_screen.components.MyCircularProgressIndicator
import com.digital.pianoassist.feature_songs.presentation.recording_screen.components.PianoPlotter
import com.digital.pianoassist.logDebug

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RecordingScreen(
    navController: NavController,
    viewModel: RecordingScreenViewModel = hiltViewModel()
) {

    val titleState = viewModel.songTitle.value

    val isRecordingState = viewModel.isRecordingState.value
    val intermediateScore = viewModel.intermediateScore.value

    val midiNotes by viewModel.midiNotes.collectAsState()
    val newNotes by viewModel.newNotes.collectAsState()


    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("RecordingScreen", "PERMISSION GRANTED")
        } else {
            Log.d("RecordingScreen", "PERMISSION DENIED")
        }
    }
    val appContext = LocalContext.current.applicationContext

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isRecordingState) {
                            viewModel.onEvent(RecordingScreenEvent.StartRecording)
                        }
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Gray, Color.White)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) { Text(text = titleState, style = MaterialTheme.typography.headlineMedium) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Blue),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (midiNotes.isNotEmpty()) {
                    PianoPlotView(midiNotes, newNotes)
                } else {
                    MyCircularProgressIndicator()
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Magenta)
            ) {
                Text(text = intermediateScore.toString())
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                appContext,
                                android.Manifest.permission.RECORD_AUDIO
                            ) -> {
                                logDebug("Recording permission is granted")
                            }

                            else -> {
                                logDebug("Recording permission is not granted")
                                launcher.launch(android.Manifest.permission.RECORD_AUDIO)
                                return@Button
                            }
                        }
                        logDebug("Recording button clicked")
                        if (isRecordingState) {
                            logDebug("onClick: recorder stop")
                            viewModel.onEvent(RecordingScreenEvent.StopRecording)

                        } else {
                            viewModel.onEvent(RecordingScreenEvent.StartRecording)
                        }

                    },
                    colors = buttonColors(containerColor = if (isRecordingState) Color.Red else Color.DarkGray)
                ) {
                    Text(
                        text = if (isRecordingState) "Stop recording" else "Start recording",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
fun PianoPlotView(notes: List<MidiNote>, newNotes: List<Pair<Window, List<String>>>) {
    val typeface = remember { Typeface.create(Typeface.DEFAULT, Typeface.NORMAL) }
    val pianoPlotter = remember { PianoPlotter(1200, 800, typeface) }

    notes.forEach { pianoPlotter.add(it) }

    // Draw new notes in a different color
    Canvas(modifier = Modifier.fillMaxSize()) {
        pianoPlotter.draw(drawContext.canvas.nativeCanvas)
        val paint = Paint().apply {
            color = Color.Blue.hashCode() // Use blue for new notes
            style = Paint.Style.FILL
        }
        newNotes.forEach { (window, notes) ->
            notes.forEach { note ->
                val index = pianoPlotter.noteIndexMap[note] ?: 0
                val nrNotes = pianoPlotter.maxNoteIndex - pianoPlotter.minNoteIndex + 1
                val noteHeight = pianoPlotter.plot.height() / nrNotes
                val y = pianoPlotter.y1 - (index - pianoPlotter.minNoteIndex) * noteHeight
                val startX =
                    window.startSeconds / pianoPlotter.plotSeconds * pianoPlotter.plot.width() + pianoPlotter.x0
                val endX =
                    window.endSeconds / pianoPlotter.plotSeconds * pianoPlotter.plot.width() + pianoPlotter.x0

                drawContext.canvas.nativeCanvas.drawRect(
                    startX.toFloat(),
                    (y - noteHeight).toFloat(),
                    endX.toFloat(),
                    y.toFloat(),
                    paint
                )
            }
        }
    }
}


