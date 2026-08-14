package com.ownhabs.app.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupHabit(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val colorHex: String,
    val goalType: String,
    val targetValue: Double,
    val unit: String,
    val frequencyPeriod: String,
    val timesPerPeriod: Int,
    val reminderHour: Int?,
    val reminderMinute: Int?,
    val status: String,
    val createdAt: Long
)

@Serializable
data class BackupHabitLog(
    val id: String,
    val habitId: String,
    val timestamp: Long,
    val value: Double,
    val notes: String
)

@Serializable
data class BackupUnlockedBadge(
    val id: String,
    val badgeId: String,
    val habitId: String,
    val unlockedAt: Long
)

@Serializable
data class OwnHabsBackup(
    val schemaVersion: Int = 1,
    val exportedAt: Long,
    val habits: List<BackupHabit>,
    val habitLogs: List<BackupHabitLog>,
    val unlockedBadges: List<BackupUnlockedBadge>
)
