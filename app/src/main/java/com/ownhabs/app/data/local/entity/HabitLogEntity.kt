package com.ownhabs.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "habit_logs",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index("timestamp")]
)
data class HabitLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val timestamp: Long,
    val value: Double,
    val notes: String = ""
)
