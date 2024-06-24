package com.digital.pianoassist.feature_songs.domain.use_cases

/*
This would be a wrapper class that has a reference to all the use cases
so we don't have to pass every use case to the view model constructor

THIS IS THE CLASS THAT WILL BE INJECTED INTO THE VIEW MODEL
 */
data class UseCases(
    val getSongsUseCase: GetSongsUseCase,
    val getSongUseCase: GetSongUseCase,
    val addRecordingUseCase: AddRecordingUseCase,
    val getMidiStreamUseCase: GetMidiStreamUseCase,
    val performRecordingUseCase: PerformRecordingUseCase,
    val updateMaxScoreUseCase: UpdateMaxScoreUseCase,
    val getRecordingsUseCase: GetRecordingsUseCase
)