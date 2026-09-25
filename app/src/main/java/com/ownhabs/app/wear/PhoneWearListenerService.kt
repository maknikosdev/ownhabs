package com.ownhabs.app.wear

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.ownhabs.app.OwnHabsApp
import com.ownhabs.app.data.local.entity.GoalType
import com.ownhabs.app.data.repository.HabitRepository
import com.ownhabs.app.widget.HabitWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Τρέχει στο τηλέφωνο και ακούει για δεδομένα/μηνύματα από το Wear OS companion:
 *  - onMessageReceived: ο χρήστης πάτησε μια συνήθεια στο ρολόι (tap-to-complete).
 *  - onDataChanged: το ρολόι έστειλε νέο σωρευτικό σύνολο βημάτων ημέρας (Health Services).
 */
class PhoneWearListenerService : WearableListenerService() {

    companion object {
        private const val PATH_TOGGLE_HABIT = "/ownhabs/toggle"
        private const val PATH_STEPS = "/ownhabs/steps"
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

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        val stepsEvent = dataEvents.firstOrNull { it.dataItem.uri.path == PATH_STEPS } ?: return
        val dataMap = DataMapItem.fromDataItem(stepsEvent.dataItem).dataMap
        val steps = dataMap.getInt("steps", -1)
        val updatedAt = dataMap.getLong("updatedAt", System.currentTimeMillis())
        if (steps < 0) return

        val app = applicationContext as OwnHabsApp
        WearStepsStore.update(applicationContext, steps, updatedAt)

        CoroutineScope(Dispatchers.IO).launch {
            autoCompleteStepHabits(app.container.repository, steps)
        }
    }

    /**
     * Για κάθε ενεργή, ΠΟΣΟΤΙΚΗ συνήθεια με autoTrackSteps=true: αν τα σημερινά βήματα
     * του ρολογιού έφτασαν ή ξεπέρασαν τον στόχο, και η συνήθεια δεν είναι ήδη
     * καταγεγραμμένη ως ολοκληρωμένη σήμερα, την "κλείνουμε" αυτόματα με την πραγματική
     * τιμή βημάτων (ώστε η Αρχική να δείχνει τον πραγματικό αριθμό, π.χ. 8342/8000).
     * Αν ο χρήστης έχει ήδη καταγράψει κάτι χειροκίνητα που φτάνει τον στόχο, δεν αγγίζουμε τίποτα.
     */
    private suspend fun autoCompleteStepHabits(repository: HabitRepository, steps: Int) {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        var anyUpdated = false
        val habits = repository.observeActiveHabits().first()
        for (habit in habits) {
            if (habit.goalType != GoalType.NUMERIC || !habit.autoTrackSteps) continue
            if (habit.targetValue <= 0.0 || steps < habit.targetValue) continue

            val todayValue = repository.getLogsForDay(habit.id, dayStart, dayEnd).sumOf { it.value }
            if (todayValue >= habit.targetValue) continue // ήδη ολοκληρωμένη σήμερα — μην αγγίξεις χειροκίνητη καταγραφή

            repository.setTodayValue(habit, steps.toDouble())
            anyUpdated = true
        }

        if (anyUpdated) {
            HabitWidgetProvider.requestUpdate(applicationContext)
            WearSyncManager.pushTodayHabits(applicationContext, repository)
        }
    }
}
