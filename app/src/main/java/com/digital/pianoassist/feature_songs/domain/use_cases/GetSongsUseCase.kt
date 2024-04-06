package com.digital.pianoassist.feature_songs.domain.use_cases

import com.digital.pianoassist.feature_songs.domain.model.Song
import com.digital.pianoassist.feature_songs.domain.repository.SongRepository
import com.digital.pianoassist.feature_songs.domain.util.OrderType
import com.digital.pianoassist.feature_songs.domain.util.SongOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
The use cases should only have one function that is public and can be called from the outside
The view model calls the use case
 */
class GetSongsUseCase(
    private val songRepository: SongRepository
) {
    /*
    invoke() allows to use instances of the class as if they were functions
    This is useful for code readability, flexibility - the class can be called like a function or
    like a traditional object-oriented style. The class can have multiple functions as well,
    not just the invoke()
    eq.:
    val myInstance = MyCallableClass()
    myInstance.doSomething() // Calling other methods
    myInstance() // Equivalent to myInstance.invoke()
     */
    // GetSongsUseCase() is equivalent to GetSongsUseCase.invoke()
    operator fun invoke(
        songOrder: SongOrder = SongOrder.Title(OrderType.Ascending) // the default value
    ): Flow<List<Song>> {
        return songRepository.getAllSongs().map { songs ->
            when (songOrder.orderType) {
                is OrderType.Ascending -> {
                    when (songOrder) {
                        is SongOrder.Title -> songs.sortedBy { it.title.lowercase() }
                    }
                }

                is OrderType.Descending -> {
                    when (songOrder) {
                        is SongOrder.Title -> songs.sortedByDescending { it.title.lowercase() }
                    }
                }
            }
        }
    }
}