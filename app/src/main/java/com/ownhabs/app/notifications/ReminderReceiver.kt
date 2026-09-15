package com.ownhabs.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ownhabs.app.MainActivity
import com.ownhabs.app.OwnHabsApp
import com.ownhabs.app.data.local.entity.GoalType
import com.ownhabs.app.ui.strings.LanguagePreference
import com.ownhabs.app.ui.strings.stringsFor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * "Έξυπνη" υπενθύμιση: πριν στείλει ειδοποίηση, ελέγχει αν η συνήθεια έχει ήδη
 * ολοκληρωθεί ΠΛΗΡΩΣ σήμερα.
 *   - Αν ναι  -> καμία ειδοποίηση (δεν έχει νόημα να σε ενοχλήσει για κάτι έτοιμο).
 *   - Αν όχι  -> ειδοποίηση κανονικά, με ένδειξη προόδου για ποσοτικές συνήθειες
 *               (π.χ. "Νερό: 1000/2000 ml — έλα να το ολοκληρώσεις").
 * Δουλεύει ανεξάρτητα από το αν η εφαρμογή είναι ανοιχτή, μέσω BroadcastReceiver +
 * AlarmManager (goAsync() για το σύντομο DB query στο παρασκήνιο).
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
        val app = context.applicationContext as OwnHabsApp
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = app.container.repository
                val habit = repository.getHabit(habitId) ?: return@launch

                val zone = ZoneId.systemDefault()
                val today = LocalDate.now(zone)
                val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
                val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
                val total = repository.getLogsForDay(habit.id, dayStart, dayEnd).sumOf { it.value }

                val fullyDone = when (habit.goalType) {
                    GoalType.BOOLEAN -> total > 0
                    GoalType.NUMERIC -> total >= habit.targetValue
                }
                if (fullyDone) return@launch // Ήδη έγινε σήμερα — καμία ενόχληση.

                val strings = stringsFor(LanguagePreference.get(context))
                val contentText = when (habit.goalType) {
                    GoalType.BOOLEAN -> strings.reminderBooleanText(habit.title)
                    GoalType.NUMERIC -> strings.reminderNumericText(
                        habit.title, total.toInt(), habit.targetValue.toInt(), habit.unit
                    )
                }

                showNotification(context, habit.id, contentText)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, habitId: String, contentText: String) {
        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingContent = android.app.PendingIntent.getActivity(
            context, habitId.hashCode(), contentIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, OwnHabsApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("OwnHabs")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingContent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(habitId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_TITLE = "extra_habit_title" // κρατείται για συμβατότητα, δεν χρησιμοποιείται πια
    }
}
