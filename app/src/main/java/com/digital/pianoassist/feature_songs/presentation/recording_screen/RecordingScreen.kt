package com.digital.pianoassist.feature_songs.presentation.recording_screen

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.Typeface
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.digital.pianoassist.feature_songs.domain.fft.MidiNote
import com.digital.pianoassist.feature_songs.domain.fft.Window
import com.digital.pianoassist.feature_songs.presentation.recording_screen.components.FinalScoreView
import com.digital.pianoassist.feature_songs.presentation.recording_screen.components.PianoPlotter
import com.digital.pianoassist.feature_songs.presentation.recording_screen.components.ScoreCircle
import com.digital.pianoassist.logDebug
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RecordingScreen(
    navController: NavController,
    viewModel: RecordingScreenViewModel = hiltViewModel()
) {

    val titleState = viewModel.songTitle.value

    val isRecordingState = viewModel.isRecordingState.value
    var hasStoppedRecording by remember { mutableStateOf(false) }
    val intermediateScore = viewModel.intermediateScore.value
    val currentRecordingTime = viewModel.currentRecordingTime.value

    val midiNotes by viewModel.midiNotes.collectAsState()
    val newNotes by viewModel.newNotes.collectAsState()

    var outOfRangeNotes by remember { mutableStateOf<List<String>>(emptyList()) }

    val finalScore = viewModel.finalScore.value


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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFC0A483)
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = it.calculateTopPadding())
                .background(Color(0xFFE7C49C))
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
                    PianoPlotView(
                        midiNotes,
                        newNotes,
                        currentRecordingTime,
                        onOutOfRangeNotesUpdated = { notes ->
                            outOfRangeNotes = notes
                        })
                } else {
                    FinalScoreView(finalScore = finalScore)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE7C49C))
            ) {
                Row {
                    ScoreCircle(score = intermediateScore)
                    if (outOfRangeNotes.isEmpty()) return@Row
                    Box(
                        modifier = Modifier
                            .padding(top = 25.dp)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Out of Range: ${outOfRangeNotes.joinToString(", ")}")
                    }
                }
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
                            hasStoppedRecording = true
                            viewModel.onEvent(RecordingScreenEvent.StopRecording)

                        } else {
                            viewModel.onEvent(RecordingScreenEvent.StartRecording)
                        }

                    },
                    colors = buttonColors(
                        containerColor = if (isRecordingState) Color.Red else Color(0xFF284675)
                    ),
                    enabled = !hasStoppedRecording
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

class PianoPlotViewState {
    val typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    var midiBitmap: Bitmap? = null
    var rect: Rect? = null
}

@Composable
fun PianoPlotView(
    midiNotes: List<MidiNote>,
    newNotes: Pair<Window, List<String>>?,
    currentRecordingTime: Double,
    onOutOfRangeNotesUpdated: (List<String>) -> Unit
) {
    val state = remember { PianoPlotViewState() }
    val pixelsPerSecond = 200
    val paddingLeft = 50
    val paddingRight = 50
    var pianoPlotter by remember { mutableStateOf<PianoPlotter?>(null) }

    // Draw new notes in a different color
    Canvas(modifier = Modifier.fillMaxSize()) {
        logDebug("Drawing canvas of size " + drawContext.size.toString())

        if (state.midiBitmap == null) {
            val plotSeconds = 30.0
            val width = (plotSeconds * pixelsPerSecond + paddingLeft + paddingRight).toInt()
            val height = drawContext.size.height
            state.rect = Rect(
                paddingLeft,
                (height * 0.05).toInt(),
                width - paddingRight,
                (height * 0.95).toInt()
            )
            state.midiBitmap =
                Bitmap.createBitmap(width, height.toInt(), Bitmap.Config.ARGB_8888)
            pianoPlotter =
                PianoPlotter(state.midiBitmap!!, state.rect!!, state.typeface, plotSeconds)
            midiNotes.forEach { pianoPlotter!!.add(it) }
            pianoPlotter!!.drawMidiPlot()
        }

        if (newNotes != null) {
            val outOfRangeNotes = pianoPlotter?.add(newNotes.first, newNotes.second)
            pianoPlotter?.drawFFTNotes()

            // Update the outOfRangeNotes state in the parent composable
            outOfRangeNotes?.let { onOutOfRangeNotesUpdated(it) }
        }

        // Draw once for left axis
        drawContext.canvas.nativeCanvas.drawBitmap(state.midiBitmap!!, 0f, 0f, null)
        // Draw again for the actual plot

        val scrollPixels = (max(0.0, currentRecordingTime - 2.5) * pixelsPerSecond).toInt()
        drawContext.canvas.nativeCanvas.drawBitmap(
            state.midiBitmap!!,
            Rect(
                paddingLeft + scrollPixels,
                0,
                Math.min(
                    scrollPixels + drawContext.size.width.toInt(),
                    state.midiBitmap!!.width
                ),
                state.midiBitmap!!.height
            ),
            Rect(
                paddingLeft,
                0,
                drawContext.size.width.toInt(),
                drawContext.size.height.toInt()
            ),
            null
        )

    }
}


