package com.ownhabs.app.wear

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WearStepsState(
    val steps: Int,
    val updatedAt: Long,     // System.currentTimeMillis() στο ρολόι, τη στιγμή της μέτρησης
    val isFresh: Boolean     // true αν η μέτρηση ήρθε πρόσφατα — χρησιμοποιείται σαν ένδειξη "ενεργή σύνδεση"
)

/**
 * Κρατά στη μνήμη (και σε SharedPreferences, ώστε να επιβιώνει επανεκκίνηση της εφαρμογής)
 * τη τελευταία γνωστή μέτρηση βημάτων που έστειλε το smartwatch. Ενημερώνεται από το
 * PhoneWearListenerService όταν φτάνει νέο DataItem στο path "/ownhabs/steps" — το ρολόι
 * το στέλνει στο παρασκήνιο κάθε φορά που το Health Services API αναφέρει νέο σωρευτικό
 * σύνολο βημάτων ημέρας. Η Αρχική Οθόνη διαβάζει απευθείας από εδώ (StateFlow), στο ίδιο
 * πνεύμα με το υπόλοιπο lightweight manual-DI του project — καμία σύνδεση internet/cloud,
 * μόνο Bluetooth μέσω του Data Layer API.
 */
object WearStepsStore {
    private const val PREFS = "wear_steps"
    private const val KEY_STEPS = "steps"
    private const val KEY_UPDATED_AT = "updatedAt"

    // Πόσο "φρέσκια" πρέπει να είναι η τελευταία μέτρηση ώστε να τη θεωρούμε ένδειξη ότι
    // το ρολόι είναι ακόμα κοντά/συνδεδεμένο. Το Health Services δεν στέλνει σταθερά σε
    // τακτά διαστήματα, οπότε ένα γενναιόδωρο παράθυρο αποφεύγει να κρύβεται η κάρτα άδικα.
    private const val FRESH_WINDOW_MS = 30 * 60 * 1000L // 30 λεπτά

    private val _state = MutableStateFlow<WearStepsState?>(null)
    val state: StateFlow<WearStepsState?> = _state.asStateFlow()

    private var loadedFromDisk = false

    /** Φορτώνει την τελευταία αποθηκευμένη μέτρηση μία φορά ανά διεργασία εφαρμογής. */
    fun init(context: Context) {
        if (loadedFromDisk) return
        loadedFromDisk = true
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val steps = prefs.getInt(KEY_STEPS, -1)
        val updatedAt = prefs.getLong(KEY_UPDATED_AT, 0L)
        if (steps >= 0 && updatedAt > 0L) {
            _state.value = WearStepsState(steps, updatedAt, isFresh(updatedAt))
        }
    }

    fun update(context: Context, steps: Int, updatedAt: Long) {
        loadedFromDisk = true
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_STEPS, steps)
            .putLong(KEY_UPDATED_AT, updatedAt)
            .apply()
        _state.value = WearStepsState(steps, updatedAt, isFresh(updatedAt))
    }

    private fun isFresh(updatedAt: Long) = System.currentTimeMillis() - updatedAt < FRESH_WINDOW_MS
}
