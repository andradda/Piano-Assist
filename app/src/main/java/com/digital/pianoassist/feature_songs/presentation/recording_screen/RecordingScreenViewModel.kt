package com.digital.pianoassist.feature_songs.presentation.recording_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digital.pianoassist.feature_songs.domain.model.Song
import com.digital.pianoassist.feature_songs.domain.use_cases.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class RecordingScreenViewModel @Inject constructor(
    private val useCases: UseCases,
    // A bundle contains the navigation args, instead of putting them as parameters to the view model
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var currentSongId: Int? = null // we assign this as a navigation argument

    /*
    Depending on what fits better, we can either use a single class with all the states or individual
    state objects. !Careful with the single state class that combines multiple states, if one is changed,
    the whole UI wil be modified.
    Private mutable and public immutable state is a common pattern for mvvm to ensure separation of
    concerns. Also, using immutable states also helps ensure thread safety.
     */
    private val _songTitle = mutableStateOf("")
    val songTitle: State<String> = _songTitle

    private val _isRecordingState = mutableStateOf(false)
    val isRecordingState: State<Boolean> = _isRecordingState

    private var _intermediateScore = mutableDoubleStateOf(0.0)
    val intermediateScore: State<Double> = _intermediateScore

    private var currentSelectedSong: Song? = null


    init {
        savedStateHandle.get<Int>("songId")?.let { songId ->
            if (songId != -1) { // if it was clicked on a song
                viewModelScope.launch {  // we want to launch a new coroutine to get the song by id
                    useCases.getSongUseCase(songId)?.also { song ->
                        currentSongId = song.id

                        // Update the mutable value, so the immutable one will also change and
                        // it will recompose the recording Screen
                        _songTitle.value = song.title
                        currentSelectedSong = song
                    }
                }
                viewModelScope.launch {
                    // If a song was clicked, the MIDI file should be already prepared for recording
                    val midiInputStream = getMidiStream(songId)
                    if (midiInputStream != null) {
                        useCases.performRecordingUseCase.createMidiModel(midiInputStream)
                    }
                }
            }
        }
    }

    private suspend fun getMidiStream(songId: Int): InputStream? {
        return viewModelScope.async {
            useCases.getMidiStreamUseCase(songId)
        }.await()
    }

    fun onEvent(event: RecordingScreenEvent) {
        when (event) {
            is RecordingScreenEvent.StartRecording -> {
                _isRecordingState.value = !_isRecordingState.value
                useCases.performRecordingUseCase.startRecording() { intermediateScore ->
                    _intermediateScore.doubleValue = intermediateScore
                    println("Intermediate score received from the UC is $intermediateScore")
                }
            }

            is RecordingScreenEvent.StopRecording -> {
                _isRecordingState.value = !_isRecordingState.value
                useCases.performRecordingUseCase.stopRecording()
                val finalScore = useCases.performRecordingUseCase.receiveFinalScore()
                println("finalScore received = $finalScore")
                viewModelScope.launch {
                    currentSelectedSong?.let {
                        println("$finalScore > ${it.maxScore}")
                        if (finalScore > it.maxScore) {
                            println("$finalScore > ${it.maxScore}")
                            useCases.updateMaxScoreUseCase(it, finalScore.toInt())
                            TODO("PRAGMA wal_autocheckpoint for automatically update in the original database")
                        }
                    }
                }
            }
        }
    }
}