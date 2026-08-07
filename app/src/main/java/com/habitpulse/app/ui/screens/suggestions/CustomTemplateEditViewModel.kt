package com.habitpulse.app.ui.screens.suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.habitpulse.app.data.local.entity.CustomTemplateEntity
import com.habitpulse.app.data.local.entity.FrequencyPeriod
import com.habitpulse.app.data.local.entity.GoalType
import com.habitpulse.app.data.repository.CustomCategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomTemplateFormState(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val icon: String = "✅",
    val colorHex: String = "#2FB6C0",
    val goalType: GoalType = GoalType.BOOLEAN,
    val targetValue: String = "1",
    val unit: String = "",
    val frequencyPeriod: FrequencyPeriod = FrequencyPeriod.DAILY,
    val timesPerPeriod: Int = 1,
    val isLoading: Boolean = true
)

class CustomTemplateEditViewModel(
    private val repository: CustomCategoryRepository,
    private val categoryId: String,
    private val templateId: String?
) : ViewModel() {

    private val _state = MutableStateFlow(CustomTemplateFormState(isLoading = templateId != null))
    val state: StateFlow<CustomTemplateFormState> = _state.asStateFlow()

    init {
        if (templateId != null) {
            viewModelScope.launch {
                repository.getTemplate(templateId)?.let { t ->
                    _state.value = CustomTemplateFormState(
                        id = t.id, title = t.title, description = t.description, icon = t.icon,
                        colorHex = t.colorHex, goalType = t.goalType, targetValue = t.targetValue.toString(),
                        unit = t.unit, frequencyPeriod = t.frequencyPeriod, timesPerPeriod = t.timesPerPeriod,
                        isLoading = false
                    )
                } ?: run { _state.value = _state.value.copy(isLoading = false) }
            }
        } else {
            _state.value = _state.value.copy(isLoading = false)
        }
    }

    fun update(transform: (CustomTemplateFormState) -> CustomTemplateFormState) {
        _state.value = transform(_state.value)
    }

    fun maxTimesFor(period: FrequencyPeriod): Int = when (period) {
        FrequencyPeriod.DAILY -> 10
        FrequencyPeriod.WEEKLY -> 7
        FrequencyPeriod.MONTHLY -> 31
    }

    fun save(onSaved: () -> Unit) {
        val s = _state.value
        if (s.title.isBlank()) return
        viewModelScope.launch {
            repository.saveTemplate(
                CustomTemplateEntity(
                    id = s.id ?: java.util.UUID.randomUUID().toString(),
                    categoryId = categoryId,
                    title = s.title.trim(),
                    description = s.description.trim(),
                    icon = s.icon,
                    colorHex = s.colorHex,
                    goalType = s.goalType,
                    targetValue = s.targetValue.toDoubleOrNull() ?: 1.0,
                    unit = s.unit.trim(),
                    frequencyPeriod = s.frequencyPeriod,
                    timesPerPeriod = s.timesPerPeriod.coerceIn(1, maxTimesFor(s.frequencyPeriod))
                )
            )
            onSaved()
        }
    }

    fun delete(onDone: () -> Unit) {
        val id = _state.value.id ?: return
        viewModelScope.launch {
            repository.deleteTemplate(id)
            onDone()
        }
    }
}
