package com.habitpulse.app.di

import android.content.Context
import com.habitpulse.app.data.backup.BackupManager
import com.habitpulse.app.data.local.AppDatabase
import com.habitpulse.app.data.repository.CustomCategoryRepository
import com.habitpulse.app.data.repository.HabitRepository

/** Lightweight manual dependency container — avoids pulling in Hilt/Dagger for this scope. */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val repository = HabitRepository(
        habitDao = database.habitDao(),
        habitLogDao = database.habitLogDao(),
        badgeDao = database.badgeDao(),
        streakFreezeDao = database.streakFreezeDao()
    )

    val categoryRepository = CustomCategoryRepository(
        categoryDao = database.categoryDao(),
        templateDao = database.customTemplateDao()
    )

    val backupManager = BackupManager(repository, context.contentResolver)
}
