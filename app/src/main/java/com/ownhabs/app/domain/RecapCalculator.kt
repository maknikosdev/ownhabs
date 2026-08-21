package com.ownhabs.app.domain

import com.ownhabs.app.data.local.entity.GoalType
import com.ownhabs.app.data.local.entity.HabitEntity
import com.ownhabs.app.data.local.entity.HabitLogEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Στατιστικά σύνοψης για μια περίοδο (έτος ή μήνας). Ο ίδιος υπολογισμός τροφοδοτεί
 * τόσο το "Year in Pixels" recap όσο και τη μηνιαία ειδοποίηση σύνοψης — απλά με
 * διαφορετικό εύρος ημερομηνιών.
 */
data class RecapData(
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val totalCompletions: Int,
    val completionRate: Float, // μέσος όρος ημερήσιας «κάλυψης» μέσα στην περίοδο, 0..1
    val bestStreak: Int,
    val bestStreakHabitTitle: String?,
    val activeHabitsCount: Int,
    val badgesUnlockedCount: Int,
    val dailyRatio: Map<LocalDate, Float>, // 0..1 ανά ημέρα, για το pixel-grid
    val mostConsistentHabitTitle: String?,
    val mostConsistentHabitRate: Float
)

object RecapCalculator {

    fun calculate(
        periodStart: LocalDate,
        periodEndRequested: LocalDate,
        habits: List<HabitEntity>,
        logsByHabit: Map<String, List<HabitLogEntity>>,
        badgesUnlockedCount: Int,
        zone: ZoneId = ZoneId.systemDefault()
    ): RecapData {
        val today = LocalDate.now(zone)
        // Αν η περίοδος εκτείνεται στο μέλλον (π.χ. τρέχον έτος), σταματάμε στο σήμερα.
        val periodEnd = if (periodEndRequested.isAfter(today)) today else periodEndRequested
        val habitById = habits.associateBy { it.id }

        val metPerDay = mutableMapOf<LocalDate, MutableSet<String>>()
        val perHabitDaysMet = mutableMapOf<String, MutableSet<LocalDate>>()
        var totalCompletions = 0

        for ((habitId, logs) in logsByHabit) {
            val habit = habitById[habitId] ?: continue
            val byDay = logs
                .map { it to Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
                .filter { (_, day) -> !day.isBefore(periodStart) && !day.isAfter(periodEnd) }
                .groupBy({ it.second }, { it.first })

            for ((day, dayLogs) in byDay) {
                val total = dayLogs.sumOf { it.value }
                val met = when (habit.goalType) {
                    GoalType.BOOLEAN -> total > 0
                    GoalType.NUMERIC -> total >= habit.targetValue
                }
                totalCompletions += dayLogs.size
                if (met) {
                    metPerDay.getOrPut(day) { mutableSetOf() }.add(habitId)
                    perHabitDaysMet.getOrPut(habitId) { mutableSetOf() }.add(day)
                }
            }
        }

        val activeHabitsCount = habits.size
        val dailyRatio = mutableMapOf<LocalDate, Float>()
        var d = periodStart
        while (!d.isAfter(periodEnd)) {
            val metCount = metPerDay[d]?.size ?: 0
            dailyRatio[d] = if (activeHabitsCount > 0) (metCount.toFloat() / activeHabitsCount).coerceIn(0f, 1f) else 0f
            d = d.plusDays(1)
        }
        val completionRate = if (dailyRatio.isNotEmpty()) dailyRatio.values.average().toFloat() else 0f

        var bestStreak = 0
        var bestStreakHabitTitle: String? = null
        for ((habitId, logs) in logsByHabit) {
            val inRange = logs.filter {
                val day = Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate()
                !day.isBefore(periodStart) && !day.isAfter(periodEnd)
            }
            val result = StreakCalculator.calculate(inRange, zoneId = zone)
            if (result.best > bestStreak) {
                bestStreak = result.best
                bestStreakHabitTitle = habitById[habitId]?.title
            }
        }

        var mostConsistentTitle: String? = null
        var mostConsistentRate = 0f
        for ((habitId, daysMet) in perHabitDaysMet) {
            val habit = habitById[habitId] ?: continue
            val createdDate = Instant.ofEpochMilli(habit.createdAt).atZone(zone).toLocalDate()
            val effectiveStart = maxOf(createdDate, periodStart)
            val effectiveEnd = periodEnd
            val totalDays = ChronoUnit.DAYS.between(effectiveStart, effectiveEnd).toInt() + 1
            if (totalDays <= 0) continue
            val rate = daysMet.size.toFloat() / totalDays
            if (rate > mostConsistentRate) {
                mostConsistentRate = rate
                mostConsistentTitle = habit.title
            }
        }

        return RecapData(
            periodStart = periodStart,
            periodEnd = periodEnd,
            totalCompletions = totalCompletions,
            completionRate = completionRate,
            bestStreak = bestStreak,
            bestStreakHabitTitle = bestStreakHabitTitle,
            activeHabitsCount = activeHabitsCount,
            badgesUnlockedCount = badgesUnlockedCount,
            dailyRatio = dailyRatio,
            mostConsistentHabitTitle = mostConsistentTitle,
            mostConsistentHabitRate = mostConsistentRate
        )
    }
}
