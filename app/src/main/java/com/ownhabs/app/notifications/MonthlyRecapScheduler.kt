package com.ownhabs.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.ZoneId

/**
 * Η μηνιαία σύνοψη δεν έχει σταθερό διάστημα (οι μήνες έχουν διαφορετικό αριθμό ημερών),
 * οπότε αντί για setRepeating χρησιμοποιούμε ένα one-shot alarm που, όταν πυροδοτηθεί,
 * προγραμματίζει ξανά τον εαυτό του για τον επόμενο μήνα. Το SharedPreferences timestamp
 * μας επιτρέπει να μην ξαναπρογραμματίζουμε αν είναι ήδη προγραμματισμένο για το μέλλον
 * (π.χ. κάθε φορά που ανοίγει ο χρήστης την εφαρμογή), αλλά ταυτόχρονα να ανακάμπτουμε
 * μετά από επανεκκίνηση συσκευής (τα alarms δεν επιβιώνουν το reboot).
 */
object MonthlyRecapScheduler {
    private const val PREFS = "ownhabs_prefs"
    private const val KEY_NEXT_TRIGGER = "monthly_recap_next_trigger"
    private const val REQUEST_CODE = 9001
    private const val TRIGGER_HOUR = 10

    /** Καλείται στην εκκίνηση της εφαρμογής· δεν κάνει τίποτα αν είναι ήδη προγραμματισμένο. */
    fun ensureScheduled(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val stored = prefs.getLong(KEY_NEXT_TRIGGER, 0L)
        if (stored > System.currentTimeMillis()) return
        scheduleNext(context)
    }

    /** Προγραμματίζει (ή επαναπρογραμματίζει, π.χ. μετά από reboot ή μετά την πυροδότηση) για την 1η του επόμενου μήνα. */
    fun scheduleNext(context: Context) {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val nextTriggerDate = today.withDayOfMonth(1).plusMonths(1)
        val triggerMillis = nextTriggerDate.atTime(TRIGGER_HOUR, 0).atZone(zone).toInstant().toEpochMilli()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MonthlyRecapReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, REQUEST_CODE, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_NEXT_TRIGGER, triggerMillis)
            .apply()
    }
}
