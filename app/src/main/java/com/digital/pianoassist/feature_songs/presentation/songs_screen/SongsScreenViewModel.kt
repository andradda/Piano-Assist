package com.digital.pianoassist.feature_songs.presentation.songs_screen

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.digital.pianoassist.feature_songs.domain.model.Song
import com.digital.pianoassist.feature_songs.domain.use_cases.UseCases
import com.digital.pianoassist.feature_songs.domain.util.OrderType
import com.digital.pianoassist.feature_songs.domain.util.SongOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
The view model are directly coupled to the ui
The job of the view model in clean architecture is not business logic as it was in plain mvvm,
but to make use of the use cases (which contain the business logic)
- so the view-model just takes the result of the use cases and does something on the ui
- it needs to put the data in a state that is relevant for the ui so the ui can observe on that state
 */
@HiltViewModel
class SongsScreenViewModel @Inject constructor(
    private val useCases: UseCases
) : ViewModel() {

    // SongsScreenState - wrapper class for all the states we want into a single state object
    private val _state = mutableStateOf(SongsScreenState())
    /*
    The immutable state variable has a reference to the mutable _state so anytime the _state will
    change the state will too, so it is just like having a single object state but to make sure the
    _state can be only modified by the view-model and read by the public immutable state (by other
    classes - UI)
     */

    val state: State<SongsScreenState> = _state

    /*
    The getSongsJob is a reference to the coroutine job responsible for fetching songs. By keeping
    track of the ongoing job, you can control when to cancel it and avoid unnecessary concurrency issues
     */
    private var getSongsJob: Job? = null

    init { // this is called when the class is instantiated so it will have initial values on the screen
        getSongs(SongOrder.Title(OrderType.Descending))
    }

    private val _last30DaysAverageScore = mutableDoubleStateOf(0.0)
    val last30DaysAverageScore: State<Double> = _last30DaysAverageScore

    fun onEvent(event: SongsScreenEvent) {
        when (event) {
            is SongsScreenEvent.Order -> {
                if (state.value.songOrder::class == event.songsOrder::class &&
                    state.value.songOrder.orderType == event.songsOrder.orderType
                ) {
                    return
                }
                getSongs(event.songsOrder)
            }

            is SongsScreenEvent.ToggleOrderSection -> {
                _state.value = state.value.copy(
                    isOrderSectionVisible = !state.value.isOrderSectionVisible
                )
            }
        }
    }

    // whenever we call getSongs function we want to cancel the old coroutine
    private fun getSongs(songOrder: SongOrder) { // asynchronous execution while fetching data
        /*
        Before we get a new flow, a new coroutine we can cancel the current job and we assign the
        new job to getSongs job.

        Before starting a new asynchronous operation (fetching songs), the function first cancels
        any ongoing operation (if exists). This is essential to avoid race conditions and ensure t
        hat only one fetch operation is active at a time. By cancelling the previous job, you prevent
        potential conflicts or inconsistencies in the UI caused by outdated or incomplete data.
         */
        getSongsJob?.cancel()
        getSongsJob = useCases.getSongsUseCase(songOrder) // asynchronous code
            .onEach { songs ->    // each emission of the flow will be a new List<Song>
                _state.value = state.value.copy(
                    songs = songs,
                    songOrder = songOrder
                )
            }
            .launchIn(viewModelScope) // launchIn returns a Job
    }

    fun receiveLast30DaysAverageScore(song: Song) {
        viewModelScope.launch {
            val recordings = useCases.getRecordingsUseCase(song)
            val averageScore = recordings?.map { it.score }?.average()
            if (averageScore != null) {
                _last30DaysAverageScore.doubleValue = averageScore
            }
        }
    }
}