package com.habitpulse.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitpulse.app.data.local.entity.HabitEntity
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.domain.FrequencyCalculator
import com.habitpulse.app.domain.PeriodProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class HomeHabitUiState(
    val habit: HabitEntity,
    val todayValue: Double,       // άθροισμα καταγραφών σήμερα (χρήσιμο για NUMERIC μετρητή)
    val currentStreak: Int,
    val progress: PeriodProgress  // πρόοδος μέσα στην τρέχουσα περίοδο (ημέρα/εβδομάδα/μήνας)
)

class HomeViewModel(private val repository: HabitRepository, private val appContext: android.content.Context) : ViewModel() {

    private val _uiState = MutableStateFlow<List<HomeHabitUiState>>(emptyList())
    val uiState: StateFlow<List<HomeHabitUiState>> = _uiState.asStateFlow()
    private val zone = ZoneId.systemDefault()

    init {
        viewModelScope.launch {
            repository.observeActiveHabits().collect { habits ->
                refresh(habits)
            }
        }
    }

    private suspend fun refresh(habits: List<HabitEntity>) {
        val today = LocalDate.now(zone)

        val states = habits.map { habit ->
            val (start, end) = FrequencyCalculator.periodRange(habit, today, zone)
            val periodStartMillis = start.atStartOfDay(zone).toInstant().toEpochMilli()
            val periodEndMillis = end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

            val logsInPeriod = repository.getLogsInRange(habit.id, periodStartMillis, periodEndMillis)
            val progress = FrequencyCalculator.calculateProgress(habit, logsInPeriod, zone)

            val todayStartMillis = today.atStartOfDay(zone).toInstant().toEpochMilli()
            val todayEndMillis = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
            val todayLogs = logsInPeriod.filter { it.timestamp in todayStartMillis..todayEndMillis }
            val todayValue = todayLogs.sumOf { it.value }

            val streak = repository.getStreak(habit.id)
            HomeHabitUiState(habit, todayValue, streak.current, progress)
        }
        _uiState.value = states
    }

    /** Καταγράφει μια ολοκλήρωση (ή +delta για ποσοτικές συνήθειες) και ανανεώνει την πρόοδο/σερί. */
    fun logCompletion(habit: HabitEntity, amount: Double, onBadgesUnlocked: (List<String>) -> Unit = {}) {
        viewModelScope.launch {
            val unlocked = repository.logHabit(habit, amount)
            refresh(_uiState.value.map { it.habit })
            com.habitpulse.app.widget.HabitWidgetProvider.requestUpdate(appContext)
            if (unlocked.isNotEmpty()) {
                onBadgesUnlocked(unlocked.map { it.title })
            }
        }
    }

    /** Ορίζει ρητά τη σημερινή τιμή (Έγινε πλήρως / Εν μέρει / Δεν έγινε) από το QuickLogDialog. */
    fun setTodayValue(habit: HabitEntity, value: Double, onBadgesUnlocked: (List<String>) -> Unit = {}) {
        viewModelScope.launch {
            val unlocked = repository.setTodayValue(habit, value)
            refresh(_uiState.value.map { it.habit })
            com.habitpulse.app.widget.HabitWidgetProvider.requestUpdate(appContext)
            if (unlocked.isNotEmpty()) {
                onBadgesUnlocked(unlocked.map { it.title })
            }
        }
    }

    /** Αρχειοθέτηση: η συνήθεια κρύβεται από την Αρχική αλλά διατηρεί το ιστορικό της. */
    fun archiveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.archiveHabit(habit.id)
            com.habitpulse.app.widget.HabitWidgetProvider.requestUpdate(appContext)
        }
    }

    /** Μόνιμη διαγραφή: αφαιρεί τη συνήθεια ΚΑΙ όλο το ιστορικό καταγραφών της (cascade). */
    fun deleteHabitPermanently(habit: HabitEntity) {
        viewModelScope.launch {
            repository.deleteHabitPermanently(habit.id)
            com.habitpulse.app.widget.HabitWidgetProvider.requestUpdate(appContext)
        }
    }
}
