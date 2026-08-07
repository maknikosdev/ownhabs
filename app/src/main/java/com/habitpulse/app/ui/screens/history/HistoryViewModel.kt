package com.habitpulse.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitpulse.app.data.local.entity.GoalType
import com.habitpulse.app.data.local.entity.HabitEntity
import com.habitpulse.app.data.local.entity.HabitLogEntity
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.domain.StreakResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class HistoryUiState(
    val habit: HabitEntity? = null,
    val logs: List<HabitLogEntity> = emptyList(),
    val dailyIntensity: Map<LocalDate, Float> = emptyMap(),
    val frozenDates: Set<LocalDate> = emptySet(),
    val streak: StreakResult = StreakResult(0, 0),
    val totalCompletions: Int = 0,
    val freezesUsedThisMonth: Int = 0,
    val freezesAvailableThisMonth: Int = com.habitpulse.app.data.local.entity.StreakFreezeEntity.FREEZES_PER_MONTH
)

class HistoryViewModel(
    private val repository: HabitRepository,
    private val habitId: String
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryUiState())
    val state: StateFlow<HistoryUiState> = _state.asStateFlow()
    private val zone = ZoneId.systemDefault()

    private val _freezeMessage = MutableStateFlow<FreezeMsgType?>(null)
    val freezeMessage: StateFlow<FreezeMsgType?> = _freezeMessage.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val habit = repository.getHabit(habitId) ?: return@launch
            val logs = repository.getAllLogs(habitId)
            val streak = repository.getStreak(habitId)
            val freezesUsed = repository.freezesUsedThisMonth(habitId)

            val grouped = logs.groupBy {
                Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate()
            }
            val intensity = grouped.mapValues { (_, dayLogs) ->
                val total = dayLogs.sumOf { it.value }
                when (habit.goalType) {
                    GoalType.BOOLEAN -> if (total > 0) 1f else 0f
                    GoalType.NUMERIC -> (total / habit.targetValue).toFloat().coerceIn(0f, 1f)
                }
            }

            var frozenDates = repository.getFrozenDatesOnce(habitId)

            _state.value = HistoryUiState(
                habit = habit,
                logs = logs.sortedByDescending { it.timestamp },
                dailyIntensity = intensity,
                frozenDates = frozenDates,
                streak = streak,
                totalCompletions = logs.size,
                freezesUsedThisMonth = freezesUsed,
                freezesAvailableThisMonth = com.habitpulse.app.data.local.entity.StreakFreezeEntity.FREEZES_PER_MONTH
            )
        }
    }

    /** Προσθήκη/διόρθωση καταγραφής σε οποιαδήποτε ημερομηνία (π.χ. ξεχασμένη χθεσινή καταγραφή). */
    fun addOrEditLog(date: LocalDate, value: Double, notes: String, existing: HabitLogEntity? = null) {
        val habit = _state.value.habit ?: return
        viewModelScope.launch {
            val timestamp = date.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
            if (existing != null) {
                repository.updateLog(existing.copy(value = value, notes = notes, timestamp = timestamp))
            } else {
                repository.logHabit(habit, value, notes, timestamp)
            }
            refresh()
        }
    }

    fun deleteLog(log: HabitLogEntity) {
        viewModelScope.launch {
            repository.deleteLog(log)
            refresh()
        }
    }

    /** Εφαρμόζει ένα streak freeze ("κάρτα χάρης") σε μια χαμένη ημέρα, αν διαθέσιμο. */
    fun applyStreakFreeze(date: LocalDate) {
        viewModelScope.launch {
            _freezeMessage.value = when (repository.applyStreakFreeze(habitId, date)) {
                is HabitRepository.FreezeResult.Applied -> FreezeMsgType.APPLIED
                is HabitRepository.FreezeResult.AlreadyLogged -> FreezeMsgType.ALREADY_LOGGED
                is HabitRepository.FreezeResult.AlreadyFrozen -> FreezeMsgType.ALREADY_FROZEN
                is HabitRepository.FreezeResult.MonthlyLimitReached -> FreezeMsgType.LIMIT_REACHED
            }
            refresh()
        }
    }

    fun clearFreezeMessage() { _freezeMessage.value = null }
}

enum class FreezeMsgType { APPLIED, ALREADY_LOGGED, ALREADY_FROZEN, LIMIT_REACHED }
