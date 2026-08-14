package com.ownhabs.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.ownhabs.app.data.local.entity.HabitEntity
import java.util.Calendar

/**
 * Schedules a repeating local alarm for a habit's daily reminder time.
 * No external server involved — pure AlarmManager + NotificationManager, per spec.
 */
object ReminderScheduler {

    fun schedule(context: Context, habit: HabitEntity) {
        val hour = habit.reminderHour ?: return
        val minute = habit.reminderMinute ?: return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_HABIT_ID, habit.id)
            putExtra(ReminderReceiver.EXTRA_HABIT_TITLE, habit.title)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, habit.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = nextTriggerMillis(hour, minute)
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun cancel(context: Context, habit: HabitEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, habit.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (trigger.before(now)) trigger.add(Calendar.DAY_OF_YEAR, 1)
        return trigger.timeInMillis
    }
}
