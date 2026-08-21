package com.ownhabs.app.ui.screens.recap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownhabs.app.data.repository.HabitRepository
import com.ownhabs.app.domain.RecapCalculator
import com.ownhabs.app.domain.RecapData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class YearRecapViewModel(
    private val repository: HabitRepository,
    private val year: Int
) : ViewModel() {

    private val _data = MutableStateFlow<RecapData?>(null)
    val data: StateFlow<RecapData?> = _data.asStateFlow()

    init {
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val habits = repository.getAllHabitsOnce()
            val logs = repository.getAllLogsOnce()
            val unlocked = repository.getAllUnlockedOnce()

            _data.value = RecapCalculator.calculate(
                periodStart = LocalDate.of(year, 1, 1),
                periodEndRequested = LocalDate.of(year, 12, 31),
                habits = habits,
                logsByHabit = logs.groupBy { it.habitId },
                badgesUnlockedCount = unlocked.size,
                zone = zone
            )
        }
    }
}
