package com.ownhabs.app.wear

import android.content.Context
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.ownhabs.app.data.local.entity.GoalType
import com.ownhabs.app.data.local.entity.HabitEntity
import com.ownhabs.app.data.repository.HabitRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

/**
 * Σπρώχνει τη σημερινή λίστα ενεργών συνηθειών (id, τίτλος, εικονίδιο, χρώμα, αν έγινε
 * σήμερα) στο Wear OS companion, μέσω του Data Layer API. Ο συγχρονισμός γίνεται
 * αποκλειστικά μέσω Bluetooth ανάμεσα σε τηλέφωνο-ρολόι — καμία σύνδεση internet/cloud.
 * Αν δεν υπάρχει συζευγμένο ρολόι με την εφαρμογή, οι κλήσεις αυτές απλά δεν κάνουν τίποτα.
 */
object WearSyncManager {

    private const val PATH_TODAY_HABITS = "/ownhabs/today_habits"

    suspend fun pushTodayHabits(context: Context, repository: HabitRepository) {
        runCatching {
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

            val habits = repository.observeActiveHabits().first().take(10) // αρκετό για μια μικρή οθόνη ρολογιού

            val dataMaps = habits.map { habit: HabitEntity ->
                val logs = repository.getLogsForDay(habit.id, dayStart, dayEnd)
                val total = logs.sumOf { it.value }
                val done = when (habit.goalType) {
                    GoalType.BOOLEAN -> total > 0
                    GoalType.NUMERIC -> total >= habit.targetValue
                }
                DataMap().apply {
                    putString("id", habit.id)
                    putString("title", habit.title)
                    putString("icon", habit.icon)
                    putString("colorHex", habit.colorHex)
                    putBoolean("done", done)
                }
            }

            val request = PutDataMapRequest.create(PATH_TODAY_HABITS).apply {
                dataMap.putDataMapArrayList("habits", ArrayList(dataMaps))
                dataMap.putLong("updatedAt", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()

            Wearable.getDataClient(context).putDataItem(request)
        }
        // Αν δεν υπάρχει Google Play Services / συζευγμένο ρολόι, αγνοούμε σιωπηλά —
        // η λειτουργία του companion είναι προαιρετική, ποτέ δεν μπλοκάρει την κύρια εφαρμογή.
    }
}
