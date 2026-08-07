package com.habitpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class GoalType { BOOLEAN, NUMERIC }
enum class HabitStatus { ACTIVE, ARCHIVED }

/** Η περίοδος μέσα στην οποία μετράει ο στόχος συχνότητας (π.χ. "2 φορές" ανά ΤΙ). */
enum class FrequencyPeriod { DAILY, WEEKLY, MONTHLY }

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val icon: String = "✅",
    val colorHex: String = "#2FB6C0",
    val goalType: GoalType = GoalType.BOOLEAN,
    val targetValue: Double = 1.0,
    val unit: String = "",
    // Συχνότητα: π.χ. "2 φορές" (timesPerPeriod) "την εβδομάδα" (frequencyPeriod).
    // Καθαρισμός κατοικίδιου -> WEEKLY, timesPerPeriod = 2 (ή όσες θέλει ο χρήστης).
    val frequencyPeriod: FrequencyPeriod = FrequencyPeriod.DAILY,
    val timesPerPeriod: Int = 1,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val status: HabitStatus = HabitStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
)
