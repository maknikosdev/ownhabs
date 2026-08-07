package com.habitpulse.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.habitpulse.app.HabitPulseApp
import com.habitpulse.app.MainActivity

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
        val habitTitle = intent.getStringExtra(EXTRA_HABIT_TITLE) ?: "Habit"

        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingContent = android.app.PendingIntent.getActivity(
            context, habitId.hashCode(), contentIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, HabitPulseApp.REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("HabitPulse")
            .setContentText("Ώρα για: $habitTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingContent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(habitId.hashCode(), notification)
    }

    companion object {
        const val EXTRA_HABIT_ID = "extra_habit_id"
        const val EXTRA_HABIT_TITLE = "extra_habit_title"
    }
}
