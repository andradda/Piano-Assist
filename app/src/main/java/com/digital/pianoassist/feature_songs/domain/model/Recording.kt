package com.digital.pianoassist.feature_songs.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("songId")]
)
data class Recording(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val songId: Int,
    val score: Int
)

class InvalidRecordingException(message: String) : Exception(message)