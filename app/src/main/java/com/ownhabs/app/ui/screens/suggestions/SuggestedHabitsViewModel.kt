package com.ownhabs.app.ui.screens.suggestions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownhabs.app.data.local.entity.CategoryEntity
import com.ownhabs.app.data.local.entity.CustomTemplateEntity
import com.ownhabs.app.data.repository.CustomCategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CustomCategoryUiState(
    val category: CategoryEntity,
    val templates: List<CustomTemplateEntity>
)

class SuggestedHabitsViewModel(private val repository: CustomCategoryRepository) : ViewModel() {

    private val _customCategories = MutableStateFlow<List<CustomCategoryUiState>>(emptyList())
    val customCategories: StateFlow<List<CustomCategoryUiState>> = _customCategories.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeCategories(),
                repository.observeAllCustomTemplates()
            ) { categories, templates ->
                categories.map { cat -> CustomCategoryUiState(cat, templates.filter { it.categoryId == cat.id }) }
            }.collect { _customCategories.value = it }
        }
    }

    fun createCategory(name: String, icon: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.createCategory(name, icon) }
    }

    fun renameCategory(id: String, name: String, icon: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repository.renameCategory(id, name, icon) }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch { repository.deleteCategory(id) }
    }

    fun deleteTemplate(id: String) {
        viewModelScope.launch { repository.deleteTemplate(id) }
    }
}
