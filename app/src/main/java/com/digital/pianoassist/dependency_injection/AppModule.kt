package com.digital.pianoassist.dependency_injection

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.digital.pianoassist.feature_songs.data.data_source.AppDatabase
import com.digital.pianoassist.feature_songs.data.repository.RecordingRepositoryImpl
import com.digital.pianoassist.feature_songs.data.repository.SongRepositoryImpl
import com.digital.pianoassist.feature_songs.domain.repository.RecordingRepository
import com.digital.pianoassist.feature_songs.domain.repository.SongRepository
import com.digital.pianoassist.feature_songs.domain.use_cases.AddRecordingUseCase
import com.digital.pianoassist.feature_songs.domain.use_cases.GetMidiStreamUseCase
import com.digital.pianoassist.feature_songs.domain.use_cases.GetSongUseCase
import com.digital.pianoassist.feature_songs.domain.use_cases.GetSongsUseCase
import com.digital.pianoassist.feature_songs.domain.use_cases.PerformRecordingUseCase
import com.digital.pianoassist.feature_songs.domain.use_cases.UpdateMaxScoreUseCase
import com.digital.pianoassist.feature_songs.domain.use_cases.UseCases
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
This is for to define all the dependency injections with Dagger Hilt
You can either have one di per feature or per whole project, it is up to you

Put in a 'Module' all the dependencies we want ot provide with a given lifetime (for eq Singleton)

Each method annotated with @Provides is responsible for creating and returning a single instance of
a dependency.

!!OBS: For unit testing we only need another dependency_injection module for the testing
and change to provide fake repo instead of impl
and so we don't need to change anything in the repo, use cases, view models etc no where
only in this module
 */

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(app: Application): Context {
        return app.applicationContext
    }

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun provideSongRepository(db: AppDatabase): SongRepository {
        return SongRepositoryImpl(db.songDao)
    }

    @Provides
    @Singleton
    fun provideRecordingRepository(db: AppDatabase): RecordingRepository {
        return RecordingRepositoryImpl(db.recordingDao)
    }

    @Provides
    @Singleton
    fun provideUseCases(
        songRepository: SongRepository,
        recordingRepository: RecordingRepository,
        context: Context
    ): UseCases {
        return UseCases(
            getSongsUseCase = GetSongsUseCase(songRepository),
            addRecordingUseCase = AddRecordingUseCase(songRepository, recordingRepository),
            getSongUseCase = GetSongUseCase(songRepository),
            getMidiStreamUseCase = GetMidiStreamUseCase(songRepository),
            performRecordingUseCase = PerformRecordingUseCase(context),
            updateMaxScoreUseCase = UpdateMaxScoreUseCase(songRepository)
        )
    }
}