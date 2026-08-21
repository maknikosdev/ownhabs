package com.ownhabs.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ownhabs.app.MainActivity
import com.ownhabs.app.OwnHabsApp
import com.ownhabs.app.domain.RecapCalculator
import com.ownhabs.app.domain.RecapData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class MonthlyRecapReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as OwnHabsApp
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val zone = ZoneId.systemDefault()
                val lastMonth = LocalDate.now(zone).minusMonths(1).withDayOfMonth(1)
                val monthEnd = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())

                val habits = app.container.repository.getAllHabitsOnce()
                val logs = app.container.repository.getAllLogsOnce()
                val unlocked = app.container.repository.getAllUnlockedOnce()

                val recap = RecapCalculator.calculate(
                    periodStart = lastMonth,
                    periodEndRequested = monthEnd,
                    habits = habits,
                    logsByHabit = logs.groupBy { it.habitId },
                    badgesUnlockedCount = unlocked.size,
                    zone = zone
                )

                showNotification(context, recap, lastMonth)
            } finally {
                // Αλυσίδα: μόλις πυροδοτηθεί, προγραμματίζει αμέσως τον επόμενο μήνα.
                MonthlyRecapScheduler.scheduleNext(context)
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, recap: RecapData, month: LocalDate) {
        val monthName = month.month.getDisplayName(TextStyle.FULL, Locale("el", "GR"))
            .replaceFirstChar { it.uppercase() }

        val text = buildString {
            append("$monthName: ${(recap.completionRate * 100).toInt()}% συνέπεια")
            if (recap.bestStreakHabitTitle != null && recap.bestStreak > 0) {
                append(" · καλύτερο σερί «${recap.bestStreakHabitTitle}» (${recap.bestStreak} ημέρες)")
            }
            if (recap.badgesUnlockedCount > 0) {
                append(" · ${recap.badgesUnlockedCount} badges")
            }
        }

        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingContent = android.app.PendingIntent.getActivity(
            context, 9002, contentIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, OwnHabsApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Μηνιαία Σύνοψη — OwnHabs")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingContent)
            .build()

        NotificationManagerCompat.from(context).notify(9002, notification)
    }
}
