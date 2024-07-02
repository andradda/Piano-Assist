package com.digital.pianoassist.feature_songs.presentation.recording_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digital.pianoassist.feature_songs.domain.fft.MidiNote
import com.digital.pianoassist.feature_songs.domain.fft.Window
import com.digital.pianoassist.feature_songs.domain.model.Song
import com.digital.pianoassist.feature_songs.domain.use_cases.UseCases
import com.digital.pianoassist.logDebug
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.util.Timer
import javax.inject.Inject
import kotlin.concurrent.timerTask

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

    private var _finalScore = mutableDoubleStateOf(0.0)
    val finalScore: State<Double> = _finalScore

    private var _currentRecordingTime = mutableDoubleStateOf(0.0)
    val currentRecordingTime: State<Double> = _currentRecordingTime
    private var _recordingStartTime: Long = 0
    private var _recordingTimer: Timer? = null

    private var currentSelectedSong: Song? = null

    private val _midiNotes = MutableStateFlow<List<MidiNote>>(emptyList())
    val midiNotes: StateFlow<List<MidiNote>> = _midiNotes

    private val _newNotes = MutableStateFlow<Pair<Window, List<String>>?>(null)
    val newNotes: StateFlow<Pair<Window, List<String>>?> = _newNotes

    // CompletableDeferred is the equivalent of a Future/Promise in Kotlin
    private var recordingComplete: CompletableDeferred<Unit>? = null


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
                        _midiNotes.value =
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

    private fun updateIntermediateScore(score: Double) {
        viewModelScope.launch {
            _intermediateScore.doubleValue = score
        }
    }

    private fun addNewNotes(window: Window, notes: List<String>) {
        viewModelScope.launch {
            _newNotes.value = Pair(window, notes)
        }
        if (_recordingTimer == null) {
            _recordingStartTime = System.currentTimeMillis()
            _recordingTimer = Timer()
            _recordingTimer!!.scheduleAtFixedRate(timerTask {
                viewModelScope.launch {
                    _currentRecordingTime.doubleValue =
                        (System.currentTimeMillis() - _recordingStartTime) / 1000.0
                }
            }, 0, 200)
        }
    }

    fun onEvent(event: RecordingScreenEvent) {
        when (event) {
            is RecordingScreenEvent.StartRecording -> {
                _isRecordingState.value = !_isRecordingState.value
                recordingComplete = CompletableDeferred<Unit>()
                useCases.performRecordingUseCase.startRecording(
                    intermediateScoreCallback = { score ->
                        updateIntermediateScore(score)
                        println("Intermediate score received from the UC is $score")
                    },
                    newNotesCallback = { (window, notes) ->
                        logDebug(
                            "newNotesCallback " + window.startSeconds + " " + notes.joinToString(
                                ","
                            )
                        )
                        addNewNotes(window, notes)
                    },
                    recordingComplete = recordingComplete!!
                )
            }

            is RecordingScreenEvent.StopRecording -> {
                _recordingTimer?.cancel()
                _isRecordingState.value = !_isRecordingState.value
                useCases.performRecordingUseCase.stopRecording()
                _midiNotes.value = emptyList()
                viewModelScope.launch {
                    recordingComplete?.await() // Wait for recording to complete so you can call the receiveScore function
                    _finalScore.doubleValue = useCases.performRecordingUseCase.receiveFinalScore()
                    println("finalScore received = $finalScore")
                    currentSelectedSong?.let {
                        useCases.addRecordingUseCase(currentSongId!!, finalScore.value.toInt())
                        println("$finalScore > ${it.maxScore}")
                        if (_finalScore.doubleValue > it.maxScore) {
                            logDebug("$finalScore > ${it.maxScore}")
                            useCases.updateMaxScoreUseCase(it, _finalScore.doubleValue.toInt())
                        }
                    }
                }
            }
        }
    }
}