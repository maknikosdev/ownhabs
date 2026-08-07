package com.habitpulse.app.data.repository

import com.habitpulse.app.data.local.dao.CategoryDao
import com.habitpulse.app.data.local.dao.CustomTemplateDao
import com.habitpulse.app.data.local.entity.CategoryEntity
import com.habitpulse.app.data.local.entity.CustomTemplateEntity
import kotlinx.coroutines.flow.Flow

class CustomCategoryRepository(
    private val categoryDao: CategoryDao,
    private val templateDao: CustomTemplateDao
) {
    // --- Κατηγορίες (Create/Rename/Delete) ---
    fun observeCategories(): Flow<List<CategoryEntity>> = categoryDao.observeAll()

    suspend fun createCategory(name: String, icon: String = "📁"): CategoryEntity {
        val category = CategoryEntity(name = name.trim(), icon = icon)
        categoryDao.upsert(category)
        return category
    }

    suspend fun renameCategory(categoryId: String, newName: String, newIcon: String? = null) {
        val existing = categoryDao.getById(categoryId) ?: return
        categoryDao.upsert(existing.copy(name = newName.trim(), icon = newIcon ?: existing.icon))
    }

    /** Διαγράφει την κατηγορία ΚΑΙ όλα τα custom templates μέσα της (cascade). */
    suspend fun deleteCategory(categoryId: String) = categoryDao.deleteById(categoryId)

    // --- Custom templates (Create/Edit/Delete μέσα σε μια κατηγορία) ---
    fun observeTemplatesForCategory(categoryId: String): Flow<List<CustomTemplateEntity>> =
        templateDao.observeForCategory(categoryId)

    fun observeAllCustomTemplates(): Flow<List<CustomTemplateEntity>> = templateDao.observeAll()

    suspend fun getTemplate(id: String): CustomTemplateEntity? = templateDao.getById(id)

    suspend fun saveTemplate(template: CustomTemplateEntity) = templateDao.upsert(template)

    suspend fun deleteTemplate(id: String) = templateDao.deleteById(id)
}
