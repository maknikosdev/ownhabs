package com.habitpulse.app.domain

import com.habitpulse.app.data.local.entity.HabitLogEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class StreakResult(val current: Int, val best: Int)

/**
 * Streaks υπολογίζονται σε ξεχωριστές ημερολογιακές ημέρες που έχουν τουλάχιστον μία
 * καταγραφή, Ή που έχουν χρησιμοποιήσει streak freeze (βλ. StreakFreezeEntity) — ένα
 * freeze "καλύπτει" τη μέρα σαν να είχε γίνει η συνήθεια, χωρίς να μετράει σαν πραγματική
 * ολοκλήρωση πουθενά αλλού (badges/σύνολο).
 */
object StreakCalculator {

    fun calculate(
        logs: List<HabitLogEntity>,
        frozenDates: Set<LocalDate> = emptySet(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): StreakResult {
        val loggedDays = logs
            .map { Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() }
            .toSet()

        val days = (loggedDays + frozenDates).toSortedSet().toList()
        if (days.isEmpty()) return StreakResult(0, 0)

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
