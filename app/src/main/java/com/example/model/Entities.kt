package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "zikr_progress")
data class ZikrProgress(
    @PrimaryKey val zikrId: Int,
    val count: Int
)

@Entity(tableName = "favorites")
data class FavoriteZikr(
    @PrimaryKey val zikrId: Int
)
