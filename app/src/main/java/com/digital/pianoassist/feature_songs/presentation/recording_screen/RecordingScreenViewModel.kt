package com.digital.pianoassist.feature_songs.presentation.recording_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digital.pianoassist.feature_songs.domain.use_cases.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
     */
    // hte private mutable and public immutable state is a common pattern for mvvm to ensure separation
    // of concerns
    // the private mutable state is only for hte model view to control and modify
    // immutable state for external observers - read-only view for the observers (such as ui)
    // it is immutable to ensure that the external components cannot modify the state directly
    // using immutbale states also helps ensure thread safety
    private val _songTitle = mutableStateOf("")
    val songTitle: State<String> = _songTitle

    init {
        savedStateHandle.get<Int>("songId")?.let { songId ->
            if (songId != -1) { // if it was clicked on a song
                viewModelScope.launch {
                    // we want to launch a new coroutine to get the song by id
                    useCases.getSongUseCase(songId)?.also { song ->
                        currentSongId = song.id

                        // here we should also display whatever is related to the song (eq. title)
                        // using substring instead of copy!!!
                        _songTitle.value = songTitle.value.substring(songTitle.value.length)
                    }
                }
            }
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