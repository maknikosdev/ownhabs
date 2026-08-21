package com.ownhabs.app.wear

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.ownhabs.app.OwnHabsApp
import com.ownhabs.app.data.local.entity.GoalType
import com.ownhabs.app.widget.HabitWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Τρέχει στο τηλέφωνο και ακούει για μηνύματα από το Wear OS companion. Όταν ο χρήστης
 * πατήσει μια συνήθεια στο ρολόι, φτάνει εδώ ένα μήνυμα με το habitId — το εφαρμόζουμε
 * στην τοπική βάση με ακριβώς την ίδια λογική toggle που χρησιμοποιεί και το home-screen
 * widget, και μετά ξαναστέλνουμε την ενημερωμένη λίστα πίσω στο ρολόι.
 */
class PhoneWearListenerService : WearableListenerService() {

    companion object {
        private const val PATH_TOGGLE_HABIT = "/ownhabs/toggle"
    }

    override fun onMessageReceived(event: MessageEvent) {
        if (event.path != PATH_TOGGLE_HABIT) return
        val habitId = String(event.data, Charsets.UTF_8)
        val app = applicationContext as OwnHabsApp

        CoroutineScope(Dispatchers.IO).launch {
            val repository = app.container.repository
            val habit = repository.getHabit(habitId) ?: return@launch

            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
            val total = repository.getLogsForDay(habit.id, dayStart, dayEnd).sumOf { it.value }
            val alreadyDone = when (habit.goalType) {
                GoalType.BOOLEAN -> total > 0
                GoalType.NUMERIC -> total >= habit.targetValue
            }
            repository.setTodayValue(habit, if (alreadyDone) 0.0 else habit.targetValue)

            HabitWidgetProvider.requestUpdate(applicationContext)
            WearSyncManager.pushTodayHabits(applicationContext, repository)
        }
    }
}
