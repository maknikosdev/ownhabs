package com.habitpulse.app.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.runtime.Composable
import com.habitpulse.app.data.backup.BackupManager
import com.habitpulse.app.data.repository.CustomCategoryRepository
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.ui.screens.addedit.AddEditHabitScreen
import com.habitpulse.app.ui.screens.badges.BadgesScreen
import com.habitpulse.app.ui.screens.history.HistoryScreen
import com.habitpulse.app.ui.screens.home.HomeScreen
import com.habitpulse.app.ui.screens.settings.SettingsScreen
import com.habitpulse.app.ui.screens.suggestions.CustomTemplateEditScreen
import com.habitpulse.app.ui.screens.suggestions.SuggestedHabitsScreen

object Routes {
    const val HOME = "home"
    const val SUGGESTIONS = "suggestions"
    const val ADD_EDIT = "add_edit?habitId={habitId}&templateId={templateId}&customTemplateId={customTemplateId}"
    const val HISTORY = "history/{habitId}"
    const val BADGES = "badges"
    const val SETTINGS = "settings"
    const val TEMPLATE_EDIT = "template_edit/{categoryId}?templateId={templateId}"

    fun addEdit(habitId: String? = null, templateId: String? = null, customTemplateId: String? = null): String {
        val params = mutableListOf<String>()
        if (habitId != null) params.add("habitId=$habitId")
        if (templateId != null) params.add("templateId=$templateId")
        if (customTemplateId != null) params.add("customTemplateId=$customTemplateId")
        return if (params.isEmpty()) "add_edit" else "add_edit?${params.joinToString("&")}"
    }
    fun history(habitId: String) = "history/$habitId"
    fun templateEdit(categoryId: String, templateId: String? = null): String {
        val base = "template_edit/$categoryId"
        return if (templateId != null) "$base?templateId=$templateId" else base
    }
}

@Composable
fun HabitPulseNavGraph(
    repository: HabitRepository,
    categoryRepository: CustomCategoryRepository,
    backupManager: BackupManager
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                repository = repository,
                onAddHabit = { navController.navigate(Routes.SUGGESTIONS) },
                onEditHabit = { id -> navController.navigate(Routes.addEdit(habitId = id)) },
                onOpenHistory = { id -> navController.navigate(Routes.history(id)) },
                onOpenBadges = { navController.navigate(Routes.BADGES) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SUGGESTIONS) {
            SuggestedHabitsScreen(
                categoryRepository = categoryRepository,
                onPickTemplate = { template -> navController.navigate(Routes.addEdit(templateId = template.id)) },
                onPickCustomTemplate = { template -> navController.navigate(Routes.addEdit(customTemplateId = template.id)) },
                onCustomHabit = { navController.navigate(Routes.addEdit()) },
                onAddTemplateToCategory = { categoryId -> navController.navigate(Routes.templateEdit(categoryId)) },
                onEditTemplate = { categoryId, templateId -> navController.navigate(Routes.templateEdit(categoryId, templateId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.ADD_EDIT,
            arguments = listOf(
                navArgument("habitId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("templateId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("customTemplateId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")
            val templateId = backStackEntry.arguments?.getString("templateId")
            val customTemplateId = backStackEntry.arguments?.getString("customTemplateId")
            AddEditHabitScreen(
                repository = repository,
                categoryRepository = categoryRepository,
                habitId = habitId,
                templateId = templateId,
                customTemplateId = customTemplateId,
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.TEMPLATE_EDIT,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("templateId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
            val templateId = backStackEntry.arguments?.getString("templateId")
            CustomTemplateEditScreen(
                repository = categoryRepository,
                categoryId = categoryId,
                templateId = templateId,
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.HISTORY,
            arguments = listOf(navArgument("habitId") { type = NavType.StringType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId") ?: return@composable
            HistoryScreen(
                repository = repository,
                habitId = habitId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.BADGES) {
            BadgesScreen(repository = repository, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(backupManager = backupManager, onBack = { navController.popBackStack() })
        }
    }
}
