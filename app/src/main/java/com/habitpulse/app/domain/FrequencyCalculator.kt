package com.habitpulse.app.domain

import com.habitpulse.app.data.local.entity.FrequencyPeriod
import com.habitpulse.app.data.local.entity.GoalType
import com.habitpulse.app.data.local.entity.HabitEntity
import com.habitpulse.app.data.local.entity.HabitLogEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

data class PeriodProgress(
    val completedUnits: Int,   // πόσες φορές (ημέρες) πέτυχε τον στόχο μέσα στην περίοδο
    val requiredUnits: Int,    // πόσες φορές χρειάζεται (timesPerPeriod)
    val periodStart: LocalDate,
    val periodEnd: LocalDate,  // inclusive
    val metToday: Boolean
) {
    val fraction: Float get() = if (requiredUnits <= 0) 0f else (completedUnits.toFloat() / requiredUnits).coerceIn(0f, 1f)
}

object FrequencyCalculator {

    fun periodRange(habit: HabitEntity, today: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Pair<LocalDate, LocalDate> {
        return when (habit.frequencyPeriod) {
            FrequencyPeriod.DAILY -> today to today
            FrequencyPeriod.WEEKLY -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                val start = today.minusDays(((today.dayOfWeek.value - weekFields.firstDayOfWeek.value + 7) % 7).toLong())
                start to start.plusDays(6)
            }
            FrequencyPeriod.MONTHLY -> {
                val start = today.withDayOfMonth(1)
                start to start.plusMonths(1).minusDays(1)
            }
        }
    }

    /** Πόσες ξεχωριστές ημέρες μέσα στο τρέχον παράθυρο "πέτυχαν" τον στόχο (Ναι/Όχι ή Ποσοτικός). */
    fun calculateProgress(
        habit: HabitEntity,
        logsInPeriod: List<HabitLogEntity>,
        zone: ZoneId = ZoneId.systemDefault()
    ): PeriodProgress {
        val today = LocalDate.now(zone)
        val (start, end) = periodRange(habit, today, zone)

        val byDay = logsInPeriod.groupBy { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
        val metDays = byDay.count { (_, dayLogs) ->
            val total = dayLogs.sumOf { it.value }
            when (habit.goalType) {
                GoalType.BOOLEAN -> total > 0
                GoalType.NUMERIC -> total >= habit.targetValue
            }
        }
        val todayLogs = byDay[today].orEmpty()
        val todayTotal = todayLogs.sumOf { it.value }
        val metToday = when (habit.goalType) {
            GoalType.BOOLEAN -> todayTotal > 0
            GoalType.NUMERIC -> todayTotal >= habit.targetValue
        }

        return PeriodProgress(
            completedUnits = metDays,
            requiredUnits = habit.timesPerPeriod.coerceAtLeast(1),
            periodStart = start,
            periodEnd = end,
            metToday = metToday
        )
    }

    /** Ανθρώπινη περιγραφή της συχνότητας, π.χ. "2 φορές / εβδομάδα". */
    fun describe(habit: HabitEntity): String {
        val times = habit.timesPerPeriod
        val timesLabel = if (times == 1) "1 φορά" else "$times φορές"
        val periodLabel = when (habit.frequencyPeriod) {
            FrequencyPeriod.DAILY -> if (times <= 1) "την ημέρα" else "την ημέρα"
            FrequencyPeriod.WEEKLY -> "την εβδομάδα"
            FrequencyPeriod.MONTHLY -> "τον μήνα"
        }
        return "$timesLabel / $periodLabel"
    }
}
