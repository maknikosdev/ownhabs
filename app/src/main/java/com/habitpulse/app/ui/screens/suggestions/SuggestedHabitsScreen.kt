package com.habitpulse.app.ui.screens.suggestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitpulse.app.data.local.entity.CategoryEntity
import com.habitpulse.app.data.local.entity.CustomTemplateEntity
import com.habitpulse.app.data.repository.CustomCategoryRepository
import com.habitpulse.app.data.templates.HabitTemplate
import com.habitpulse.app.data.templates.HabitTemplates
import com.habitpulse.app.domain.FrequencyCalculator
import com.habitpulse.app.ui.navigation.SimpleViewModelFactory
import com.habitpulse.app.ui.strings.AppStrings
import com.habitpulse.app.ui.strings.LocalStrings
import com.habitpulse.app.ui.strings.LocalLang

private val CATEGORY_ICONS = listOf("📁", "🐾", "🏠", "💼", "🎨", "⚽", "🚗", "💡", "🌍", "🎓")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestedHabitsScreen(
    categoryRepository: CustomCategoryRepository,
    onPickTemplate: (HabitTemplate) -> Unit,
    onPickCustomTemplate: (CustomTemplateEntity) -> Unit,
    onCustomHabit: () -> Unit,
    onAddTemplateToCategory: (categoryId: String) -> Unit,
    onEditTemplate: (categoryId: String, templateId: String) -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val lang = LocalLang.current
    val viewModel: SuggestedHabitsViewModel = viewModel(
        factory = SimpleViewModelFactory { SuggestedHabitsViewModel(categoryRepository) }
    )
    val customCategories by viewModel.customCategories.collectAsState()
    val builtInGrouped = HabitTemplates.groupedByCategory()

    var categoryDialog by remember { mutableStateOf<CategoryEntity?>(null) } // null id => creating new
    var showCategoryDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var openMenuForCategory by remember { mutableStateOf<String?>(null) }
    var openMenuForTemplate by remember { mutableStateOf<String?>(null) }

    if (showCategoryDialog) {
        CategoryEditDialog(
            existing = categoryDialog,
            strings = strings,
            onDismiss = { showCategoryDialog = false },
            onConfirm = { name, icon ->
                if (categoryDialog == null) viewModel.createCategory(name, icon)
                else viewModel.renameCategory(categoryDialog!!.id, name, icon)
                showCategoryDialog = false
            }
        )
    }

    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text(strings.categoryDeleteConfirmTitle) },
            text = { Text(strings.categoryDeleteConfirmText(categoryToDelete!!.name)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteCategory(categoryToDelete!!.id); categoryToDelete = null }) { Text(strings.delete) }
            },
            dismissButton = { TextButton(onClick = { categoryToDelete = null }) { Text(strings.cancel) } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.suggestionsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = strings.back) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Column(Modifier.padding(16.dp)) {
                    Text(strings.suggestionsIntro, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = onCustomHabit, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.suggestionsCustomButton)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { categoryDialog = null; showCategoryDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.suggestionsNewCategory)
                    }
                }
            }

            if (customCategories.isNotEmpty()) {
                item {
                    Text(
                        strings.suggestionsMyCategories,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                customCategories.forEach { catState ->
                    item(key = "cat-${catState.category.id}") {
                        CategoryHeaderRow(
                            category = catState.category,
                            strings = strings,
                            menuOpen = openMenuForCategory == catState.category.id,
                            onToggleMenu = { openMenuForCategory = if (openMenuForCategory == catState.category.id) null else catState.category.id },
                            onDismissMenu = { openMenuForCategory = null },
                            onRename = { categoryDialog = catState.category; showCategoryDialog = true; openMenuForCategory = null },
                            onDelete = { categoryToDelete = catState.category; openMenuForCategory = null },
                            onAddTemplate = { onAddTemplateToCategory(catState.category.id) }
                        )
                    }
                    if (catState.templates.isEmpty()) {
                        item(key = "cat-empty-${catState.category.id}") {
                            Text(
                                strings.templatesEmptyInCategory,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                            )
                        }
                    }
                    items(catState.templates, key = { it.id }) { template ->
                        CustomTemplateRow(
                            template = template,
                            strings = strings,
                            menuOpen = openMenuForTemplate == template.id,
                            onClick = { onPickCustomTemplate(template) },
                            onToggleMenu = { openMenuForTemplate = if (openMenuForTemplate == template.id) null else template.id },
                            onDismissMenu = { openMenuForTemplate = null },
                            onEdit = { onEditTemplate(catState.category.id, template.id); openMenuForTemplate = null },
                            onDelete = { viewModel.deleteTemplate(template.id); openMenuForTemplate = null }
                        )
                    }
                }
            }

            item {
                Text(
                    strings.suggestionsReadyMade,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            builtInGrouped.forEach { (category, templates) ->
                item {
                    Text(
                        category.label(lang),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                items(templates, key = { it.id }) { template ->
                    BuiltInTemplateRow(template = template, lang = lang, onClick = { onPickTemplate(template) })
                }
            }
        }
    }
}

@Composable
private fun CategoryHeaderRow(
    category: CategoryEntity,
    strings: AppStrings,
    menuOpen: Boolean,
    onToggleMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onAddTemplate: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(category.icon, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(8.dp))
        Text(category.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        IconButton(onClick = onAddTemplate) { Icon(Icons.Default.Add, contentDescription = strings.categoryAddTemplateTooltip) }
        Box {
            IconButton(onClick = onToggleMenu) { Icon(Icons.Default.MoreVert, contentDescription = null) }
            DropdownMenu(expanded = menuOpen, onDismissRequest = onDismissMenu) {
                DropdownMenuItem(
                    text = { Text(strings.categoryRename) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = onRename
                )
                DropdownMenuItem(
                    text = { Text(strings.categoryDelete) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun CustomTemplateRow(
    template: CustomTemplateEntity,
    strings: AppStrings,
    menuOpen: Boolean,
    onClick: () -> Unit,
    onToggleMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = runCatching { Color(android.graphics.Color.parseColor(template.colorHex)) }.getOrDefault(Color(0xFF2FB6C0))

    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Text(template.icon)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(template.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text(
                    buildString {
                        if (template.description.isNotBlank()) append(template.description + " · ")
                        if (template.goalType.name == "NUMERIC") append("${template.targetValue.toInt()} ${template.unit} · ")
                        append(FrequencyCalculator.describe(
                            com.habitpulse.app.data.local.entity.HabitEntity(
                                title = "", frequencyPeriod = template.frequencyPeriod, timesPerPeriod = template.timesPerPeriod
                            )
                        ))
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Box {
                IconButton(onClick = onToggleMenu) { Icon(Icons.Default.MoreVert, contentDescription = null) }
                DropdownMenu(expanded = menuOpen, onDismissRequest = onDismissMenu) {
                    DropdownMenuItem(text = { Text(strings.edit) }, leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }, onClick = onEdit)
                    DropdownMenuItem(text = { Text(strings.delete) }, leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }, onClick = onDelete)
                }
            }
        }
    }
}

@Composable
private fun BuiltInTemplateRow(template: HabitTemplate, lang: com.habitpulse.app.ui.strings.Lang, onClick: () -> Unit) {
    val color = runCatching { Color(android.graphics.Color.parseColor(template.colorHex)) }.getOrDefault(Color(0xFF2FB6C0))

    Card(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Text(template.icon)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(template.title(lang), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                Text(
                    buildString {
                        append(template.description(lang))
                        if (template.goalType.name == "NUMERIC") append(" · ${template.targetValue.toInt()} ${template.unit}")
                        append(" · " + FrequencyCalculator.describe(
                            com.habitpulse.app.data.local.entity.HabitEntity(
                                title = "", frequencyPeriod = template.frequencyPeriod, timesPerPeriod = template.timesPerPeriod
                            )
                        ))
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun CategoryEditDialog(
    existing: CategoryEntity?,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: String) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var icon by remember { mutableStateOf(existing?.icon ?: CATEGORY_ICONS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) strings.categoryNewTitle else strings.categoryRenameTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(strings.categoryNameLabel) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CATEGORY_ICONS.forEach { ic ->
                        FilterChip(selected = icon == ic, onClick = { icon = ic }, label = { Text(ic) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, icon) }, enabled = name.isNotBlank()) {
                Text(if (existing == null) strings.categoryCreateButton else strings.save)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}
