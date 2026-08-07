package com.habitpulse.app.domain

import com.habitpulse.app.data.local.entity.HabitLogEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class StreakResult(val current: Int, val best: Int)

/**
 * Streaks are calculated on distinct calendar days that have at least one log.
 * (Frequency-per-week habits are treated as "any completed day counts"; a more
 * advanced version could skip non-scheduled days, but this keeps the engine simple
 * and predictable for the user.)
 */
object StreakCalculator {

    fun calculate(logs: List<HabitLogEntity>, zoneId: ZoneId = ZoneId.systemDefault()): StreakResult {
        if (logs.isEmpty()) return StreakResult(0, 0)

        val days = logs
            .map { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }
            .toSortedSet()
            .toList()

        var best = 1
        var run = 1
        for (i in 1 until days.size) {
            if (days[i] == days[i - 1].plusDays(1)) {
                run += 1
            } else {
                run = 1
            }
            if (run > best) best = run
        }

        // current streak: walk backwards from today (or yesterday) while consecutive
        val today = LocalDate.now(zoneId)
        var current = 0
        var cursor = today
        val daySet = days.toHashSet()

        // allow the streak to still be "alive" if today hasn't been logged yet but yesterday was
        if (!daySet.contains(today)) {
            cursor = today.minusDays(1)
        }
        while (daySet.contains(cursor)) {
            current += 1
            cursor = cursor.minusDays(1)
        }

        return StreakResult(current = current, best = best)
    }
}
