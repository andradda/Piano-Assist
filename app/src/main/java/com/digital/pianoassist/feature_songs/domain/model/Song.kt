package com.digital.pianoassist.feature_songs.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Song(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val title: String,
    val composer: String,
    val difficulty: Difficulty,
    val maxScore: Int,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) // this would be done anyway, it is just to make sure
    val midiDataSheet: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Song

        return id == other.id
    }

    override fun hashCode(): Int {
        return id ?: 0
    }
}
