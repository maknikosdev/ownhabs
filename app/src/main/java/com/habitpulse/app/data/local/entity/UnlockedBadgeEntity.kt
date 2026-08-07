package com.habitpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "unlocked_badges",
    foreignKeys = [
        ForeignKey(
            entity = BadgeEntity::class,
            parentColumns = ["id"],
            childColumns = ["badgeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("badgeId"), Index("habitId")]
)
data class UnlockedBadgeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val badgeId: String,
    val habitId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)
