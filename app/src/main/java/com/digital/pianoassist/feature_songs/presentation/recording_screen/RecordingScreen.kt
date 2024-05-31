package com.digital.pianoassist.feature_songs.presentation.recording_screen

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.digital.pianoassist.feature_songs.presentation.recording_screen.components.AndroidAudioRecorder
import com.digital.pianoassist.logDebug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RecordingScreen(
    navController: NavController,
    viewModel: RecordingScreenViewModel = hiltViewModel()
) {

    val titleState = viewModel.songTitle.value
    val midiStream = viewModel.inputStream.value

    val recorder by rememberSaveable {
        mutableStateOf(AndroidAudioRecorder())
    }

    var isRecording by rememberSaveable { mutableStateOf(false) }

    /*TODO("proper permission handling")*/
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
                        if (isRecording) {
                            recorder.stop()
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
                .background(Color.LightGray)
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(50.dp))
            Text(text = titleState)
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 50.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    // Check permission
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
                    if (isRecording) {
                        logDebug("onClick: recorder stop")
                        recorder.stop()

                    } else {
                        val audioFile = File(appContext.cacheDir, "audio.raw")
                        audioFile.createNewFile()
                        println("onClick: start recording")
                        CoroutineScope(Dispatchers.IO).launch {
                            logDebug("Coroutine started the recorder")
                            recorder.start(appContext, audioFile, midiStream)
                        }

                    }
                    isRecording = !isRecording

                },
                colors = buttonColors(containerColor = if (isRecording) Color.Red else Color.DarkGray)
            ) {
                Text(text = if (isRecording) "Stop recording" else "Start recording")
            }
        }
    }
}
