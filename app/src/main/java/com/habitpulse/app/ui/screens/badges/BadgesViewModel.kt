package com.habitpulse.app.ui.screens.badges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitpulse.app.data.local.entity.BadgeEntity
import com.habitpulse.app.data.local.entity.HabitEntity
import com.habitpulse.app.data.local.entity.UnlockedBadgeEntity
import com.habitpulse.app.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class BadgeUiItem(
    val badge: BadgeEntity,
    val unlockedFor: List<Pair<HabitEntity, Long>> // (habit, unlockedAt)
)

class BadgesViewModel(private val repository: HabitRepository) : ViewModel() {

    private val _items = MutableStateFlow<List<BadgeUiItem>>(emptyList())
    val items: StateFlow<List<BadgeUiItem>> = _items.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeBadges(),
                repository.observeUnlockedBadges(),
                repository.observeAllHabits()
            ) { badges, unlocked, habits ->
                val habitsById = habits.associateBy { it.id }
                badges.map { badge ->
                    val forThisBadge = unlocked.filter { it.badgeId == badge.id }
                    val pairs = forThisBadge.mapNotNull { u ->
                        habitsById[u.habitId]?.let { it to u.unlockedAt }
                    }
                    BadgeUiItem(badge, pairs)
                }
            }.collect { _items.value = it }
        }
    }
}
