package com.habitpulse.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habitpulse.app.HabitPulseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as HabitPulseApp
        CoroutineScope(Dispatchers.IO).launch {
            app.container.repository.observeActiveHabits()
            // Re-schedule each active habit's reminder, if one is set.
            val habits = app.container.repository.getAllHabitsOnce()
            habits.filter { it.reminderHour != null }.forEach {
                ReminderScheduler.schedule(context, it)
            }
        }
    }
}
