package com.ownhabs.app.ui.screens.addedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownhabs.app.data.local.entity.FrequencyPeriod
import com.ownhabs.app.data.local.entity.GoalType
import com.ownhabs.app.data.local.entity.HabitEntity
import com.ownhabs.app.data.local.entity.HabitStatus
import com.ownhabs.app.data.repository.CustomCategoryRepository
import com.ownhabs.app.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddEditFormState(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val icon: String = "✅",
    val colorHex: String = "#2FB6C0",
    val goalType: GoalType = GoalType.BOOLEAN,
    val targetValue: String = "1",
    val unit: String = "",
    // Συχνότητα: π.χ. "2 φορές" (timesPerPeriod) "την εβδομάδα" (frequencyPeriod) — π.χ. καθαρισμός κατοικίδιου 2x/εβδομάδα.
    val frequencyPeriod: FrequencyPeriod = FrequencyPeriod.DAILY,
    val timesPerPeriod: Int = 1,
    val reminderHour: Int? = null,
    val reminderMinute: Int? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val autoTrackSteps: Boolean = false
)

class AddEditHabitViewModel(
    private val repository: HabitRepository,
    private val categoryRepository: CustomCategoryRepository,
    private val habitId: String?,
    private val templateId: String? = null,      // ενσωματωμένο πρότυπο (HabitTemplates.kt)
    private val customTemplateId: String? = null, // δικό του πρότυπο του χρήστη (Room)
    private val lang: com.ownhabs.app.ui.strings.Lang = com.ownhabs.app.ui.strings.Lang.EL
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditFormState(isLoading = habitId != null))
    val state: StateFlow<AddEditFormState> = _state.asStateFlow()

    init {
        if (habitId != null) {
            viewModelScope.launch {
                repository.getHabit(habitId)?.let { habit ->
                    _state.value = AddEditFormState(
                        id = habit.id,
                        title = habit.title,
                        description = habit.description,
                        icon = habit.icon,
                        colorHex = habit.colorHex,
                        goalType = habit.goalType,
                        targetValue = habit.targetValue.toString(),
                        unit = habit.unit,
                        frequencyPeriod = habit.frequencyPeriod,
                        timesPerPeriod = habit.timesPerPeriod,
                        reminderHour = habit.reminderHour,
                        reminderMinute = habit.reminderMinute,
                        isLoading = false,
                        autoTrackSteps = habit.autoTrackSteps
                    )
                } ?: run { _state.value = _state.value.copy(isLoading = false) }
            }
        } else if (customTemplateId != null) {
            viewModelScope.launch {
                categoryRepository.getTemplate(customTemplateId)?.let { t ->
                    _state.value = AddEditFormState(
                        title = t.title, description = t.description, icon = t.icon, colorHex = t.colorHex,
                        goalType = t.goalType, targetValue = t.targetValue.toString(), unit = t.unit,
                        frequencyPeriod = t.frequencyPeriod, timesPerPeriod = t.timesPerPeriod, isLoading = false
                    )
                } ?: run { _state.value = _state.value.copy(isLoading = false) }
            }
        } else {
            // Νέα συνήθεια: αν ήρθαμε από μια ενσωματωμένη πρόταση (template), προσυμπλήρωσε τη φόρμα.
            val template = templateId?.let { com.ownhabs.app.data.templates.HabitTemplates.byId(it) }
            _state.value = if (template != null) {
                AddEditFormState(
                    title = template.title(lang),
                    description = template.description(lang),
                    icon = template.icon,
                    colorHex = template.colorHex,
                    goalType = template.goalType,
                    targetValue = template.targetValue.toString(),
                    unit = template.unit,
                    frequencyPeriod = template.frequencyPeriod,
                    timesPerPeriod = template.timesPerPeriod,
                    isLoading = false
                )
            } else {
                _state.value.copy(isLoading = false)
            }
        }
    }

    fun update(transform: (AddEditFormState) -> AddEditFormState) {
        _state.value = transform(_state.value)
    }

    /** Επιτρεπτό εύρος "πόσες φορές" ανάλογα με την περίοδο, ώστε το stepper να έχει λογικά όρια. */
    fun maxTimesFor(period: FrequencyPeriod): Int = when (period) {
        FrequencyPeriod.DAILY -> 10
        FrequencyPeriod.WEEKLY -> 7
        FrequencyPeriod.MONTHLY -> 31
    }

    fun save(onSaved: (HabitEntity) -> Unit) {
        val s = _state.value
        if (s.title.isBlank()) return
        viewModelScope.launch {
            val habit = HabitEntity(
                id = s.id ?: java.util.UUID.randomUUID().toString(),
                title = s.title.trim(),
                description = s.description.trim(),
                icon = s.icon,
                colorHex = s.colorHex,
                goalType = s.goalType,
                targetValue = s.targetValue.toDoubleOrNull() ?: 1.0,
                unit = s.unit.trim(),
                frequencyPeriod = s.frequencyPeriod,
                timesPerPeriod = s.timesPerPeriod.coerceIn(1, maxTimesFor(s.frequencyPeriod)),
                reminderHour = s.reminderHour,
                reminderMinute = s.reminderMinute,
                status = HabitStatus.ACTIVE,
                autoTrackSteps = s.goalType == GoalType.NUMERIC && s.autoTrackSteps
            )
            repository.saveHabit(habit)
            _state.value = _state.value.copy(isSaved = true)
            onSaved(habit)
        }
    }

    fun archive(onDone: () -> Unit) {
        val id = _state.value.id ?: return
        viewModelScope.launch {
            repository.archiveHabit(id)
            onDone()
        }
    }

    fun deletePermanently(onDone: () -> Unit) {
        val id = _state.value.id ?: return
        viewModelScope.launch {
            repository.deleteHabitPermanently(id)
            onDone()
        }
    }
}
