package com.habitpulse.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.habitpulse.app.di.AppContainer

class HabitPulseApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Habit Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Τοπικές υπενθυμίσεις για τις καθημερινές σου συνήθειες."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "habit_reminders"
    }
}
