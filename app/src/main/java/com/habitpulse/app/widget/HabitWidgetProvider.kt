package com.habitpulse.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.habitpulse.app.HabitPulseApp
import com.habitpulse.app.MainActivity
import com.habitpulse.app.R
import com.habitpulse.app.data.local.entity.GoalType
import com.habitpulse.app.data.local.entity.HabitEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Home-screen widget: δείχνει τις 3 πρώτες ενεργές συνήθειες με κουμπί γρήγορης
 * καταγραφής, χωρίς να χρειάζεται να ανοίξει ο χρήστης καν την εφαρμογή. Απλοποίηση
 * σκόπιμη: κάθε tap καταγράφει "μία φορά" (boolean-style) ανεξαρτήτως τύπου στόχου —
 * για πλήρη ποσοτική καταγραφή, ο χρήστης ανοίγει την εφαρμογή.
 */
class HabitWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_TOGGLE_HABIT = "com.habitpulse.app.widget.ACTION_TOGGLE_HABIT"
        const val EXTRA_HABIT_ID = "extra_habit_id"

        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, HabitWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                val intent = Intent(context, HabitWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE_HABIT) {
            val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
            val app = context.applicationContext as HabitPulseApp
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val habit = app.container.repository.getHabit(habitId)
                    if (habit != null) {
                        val doneToday = isDoneToday(app, habit)
                        val newValue = if (doneToday) 0.0 else habit.targetValue
                        app.container.repository.setTodayValue(habit, newValue)
                    }
                } finally {
                    requestUpdate(context)
                    pendingResult.finish()
                }
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val app = context.applicationContext as HabitPulseApp
        val views = RemoteViews(context.packageName, R.layout.widget_habit_pulse)

        val openAppIntent = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_header, openAppIntent)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = app.container.repository.observeActiveHabits().first().take(3)

                val rowIds = listOf(
                    Pair(R.id.row1, Triple(R.id.row1_icon, R.id.row1_title, R.id.row1_button)),
                    Pair(R.id.row2, Triple(R.id.row2_icon, R.id.row2_title, R.id.row2_button)),
                    Pair(R.id.row3, Triple(R.id.row3_icon, R.id.row3_title, R.id.row3_button))
                )

                for ((index, rowSpec) in rowIds.withIndex()) {
                    val (rowLayoutId, fields) = rowSpec
                    val (iconId, titleId, buttonId) = fields
                    val habit = habits.getOrNull(index)

                    if (habit == null) {
                        views.setViewVisibility(rowLayoutId, View.GONE)
                        continue
                    }
                    views.setViewVisibility(rowLayoutId, View.VISIBLE)
                    views.setTextViewText(iconId, habit.icon)
                    views.setTextViewText(titleId, habit.title)

                    val doneToday = isDoneToday(app, habit)
                    views.setTextViewText(buttonId, if (doneToday) "✓" else "+")
                    views.setInt(
                        buttonId, "setBackgroundResource",
                        if (doneToday) R.drawable.widget_log_button_done_bg
                        else R.drawable.widget_log_button_bg
                    )

                    val toggleIntent = Intent(context, HabitWidgetProvider::class.java).apply {
                        action = ACTION_TOGGLE_HABIT
                        putExtra(EXTRA_HABIT_ID, habit.id)
                    }
                    val togglePendingIntent = PendingIntent.getBroadcast(
                        context, habit.id.hashCode(), toggleIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(buttonId, togglePendingIntent)
                }

                views.setViewVisibility(
                    R.id.widget_empty_text,
                    if (habits.isEmpty()) View.VISIBLE else View.GONE
                )

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun isDoneToday(app: HabitPulseApp, habit: HabitEntity): Boolean {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        val logs = app.container.repository.getLogsForDay(habit.id, dayStart, dayEnd)
        val total = logs.sumOf { it.value }
        return when (habit.goalType) {
            GoalType.BOOLEAN -> total > 0
            GoalType.NUMERIC -> total >= habit.targetValue
        }
    }
}
