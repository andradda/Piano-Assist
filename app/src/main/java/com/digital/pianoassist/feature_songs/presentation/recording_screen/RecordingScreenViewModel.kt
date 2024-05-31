package com.digital.pianoassist.feature_songs.presentation.recording_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digital.pianoassist.feature_songs.domain.use_cases.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class RecordingScreenViewModel @Inject constructor(
    private val useCases: UseCases,
    // kind of a bundle that contains the navigation args, instead of putting them as parameters to the view model
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

    // TODO: temporary, all the fft logic should be moved to use case layer, not UI!!
    private val _inputStream = mutableStateOf<InputStream?>(null)
    val inputStream: State<InputStream?> = _inputStream

    init {
        savedStateHandle.get<Int>("songId")?.let { songId ->
            if (songId != -1) { // if it was clicked on a song
                viewModelScope.launch {
                    // we want to launch a new coroutine to get the song by id
                    useCases.getSongUseCase(songId)?.also { song ->
                        currentSongId = song.id

                        // Update the mutable value, so the immutable one will also change and
                        // it will recompose the recording Screen
                        _songTitle.value = song.title
                    }
                }
                getMidiStream(songId)
            }
        }
    }

    private fun getMidiStream(songId: Int) {
        viewModelScope.launch {
            useCases.getMidiStreamUseCase(songId)?.also { midiStream ->
                _inputStream.value = midiStream
            }
//            midiStream?.let {
//                for (note in readMIDI(it)) {
//                }
//            }
        }
    }

    fun onEvent(event: RecordingScreenEvent) {
        when (event) {
            is RecordingScreenEvent.StartRecording -> {
                /* TODO */
            }
        }
    }
}