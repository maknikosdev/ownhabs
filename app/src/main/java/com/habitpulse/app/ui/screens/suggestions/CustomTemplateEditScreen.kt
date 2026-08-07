package com.habitpulse.app.ui.screens.suggestions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitpulse.app.data.local.entity.FrequencyPeriod
import com.habitpulse.app.data.local.entity.GoalType
import com.habitpulse.app.data.repository.CustomCategoryRepository
import com.habitpulse.app.domain.FrequencyCalculator
import com.habitpulse.app.ui.navigation.SimpleViewModelFactory

private val ICONS = listOf("✅", "💧", "📖", "🏃", "🧘", "🥗", "😴", "💊", "✍️", "🎯", "🚭", "💰", "🐾", "🧹", "📞", "🌱")
private val COLORS = listOf("#2FB6C0", "#9ED037", "#F2B705", "#E85D5D", "#8E7CC3", "#3B82F6")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTemplateEditScreen(
    repository: CustomCategoryRepository,
    categoryId: String,
    templateId: String?,
    onDone: () -> Unit
) {
    val viewModel: CustomTemplateEditViewModel = viewModel(
        factory = SimpleViewModelFactory { CustomTemplateEditViewModel(repository, categoryId, templateId) }
    )
    val state by viewModel.state.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Διαγραφή πρότασης;") },
            text = { Text("Θα αφαιρεθεί μόνιμα από την κατηγορία σου. Δεν επηρεάζει τις συνήθειες που έχεις ήδη δημιουργήσει από αυτήν.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(onDone) }) { Text("Διαγραφή") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Ακύρωση") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (templateId == null) "Νέα Πρόταση" else "Επεξεργασία Πρότασης") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω") }
                },
                actions = {
                    if (templateId != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Διαγραφή")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Αυτή είναι μια πρόταση, όχι μια ενεργή συνήθεια — θα εμφανίζεται στη λίστα προτάσεων όταν πατάς «+».",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                value = state.title,
                onValueChange = { v -> viewModel.update { it.copy(title = v) } },
                label = { Text("Τίτλος") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.description,
                onValueChange = { v -> viewModel.update { it.copy(description = v) } },
                label = { Text("Περιγραφή (προαιρετικό)") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Εικονίδιο", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ICONS) { icon ->
                    FilterChip(
                        selected = state.icon == icon,
                        onClick = { viewModel.update { it.copy(icon = icon) } },
                        label = { Text(icon) }
                    )
                }
            }

            Text("Χρώμα", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(COLORS) { hex ->
                    val selected = state.colorHex == hex
                    Box(
                        modifier = Modifier
                            .size(if (selected) 40.dp else 32.dp)
                            .selectable(selected = selected, onClick = { viewModel.update { it.copy(colorHex = hex) } })
                    ) {
                        Surface(
                            color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex)),
                            shape = CircleShape,
                            modifier = Modifier.fillMaxSize()
                        ) {}
                    }
                }
            }

            Text("Τύπος Στόχου", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.selectable(
                        selected = state.goalType == GoalType.BOOLEAN,
                        onClick = { viewModel.update { it.copy(goalType = GoalType.BOOLEAN) } }
                    )
                ) {
                    RadioButton(selected = state.goalType == GoalType.BOOLEAN, onClick = null)
                    Text("Ναι / Όχι")
                }
                Spacer(Modifier.width(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.selectable(
                        selected = state.goalType == GoalType.NUMERIC,
                        onClick = { viewModel.update { it.copy(goalType = GoalType.NUMERIC) } }
                    )
                ) {
                    RadioButton(selected = state.goalType == GoalType.NUMERIC, onClick = null)
                    Text("Ποσοτικός Στόχος")
                }
            }

            if (state.goalType == GoalType.NUMERIC) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.targetValue,
                        onValueChange = { v -> viewModel.update { it.copy(targetValue = v) } },
                        label = { Text("Τιμή Στόχου") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.unit,
                        onValueChange = { v -> viewModel.update { it.copy(unit = v) } },
                        label = { Text("Μονάδα") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text("Προτεινόμενη Συχνότητα", style = MaterialTheme.typography.titleMedium)
            val maxTimes = viewModel.maxTimesFor(state.frequencyPeriod)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    FrequencyPeriod.DAILY to "Ημέρα",
                    FrequencyPeriod.WEEKLY to "Εβδομάδα",
                    FrequencyPeriod.MONTHLY to "Μήνας"
                ).forEach { (period, label) ->
                    FilterChip(
                        modifier = Modifier.weight(1f),
                        selected = state.frequencyPeriod == period,
                        onClick = {
                            viewModel.update {
                                it.copy(frequencyPeriod = period, timesPerPeriod = it.timesPerPeriod.coerceIn(1, viewModel.maxTimesFor(period)))
                            }
                        },
                        label = { Text(label) }
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Πόσες φορές:", style = MaterialTheme.typography.bodyMedium)
                IconButton(onClick = { viewModel.update { it.copy(timesPerPeriod = (it.timesPerPeriod - 1).coerceIn(1, maxTimes)) } }) {
                    Text("−", style = MaterialTheme.typography.titleLarge)
                }
                Text("${state.timesPerPeriod}", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { viewModel.update { it.copy(timesPerPeriod = (it.timesPerPeriod + 1).coerceIn(1, maxTimes)) } }) {
                    Text("+", style = MaterialTheme.typography.titleLarge)
                }
            }
            Text(
                FrequencyCalculator.describe(
                    com.habitpulse.app.data.local.entity.HabitEntity(
                        title = "", frequencyPeriod = state.frequencyPeriod, timesPerPeriod = state.timesPerPeriod
                    )
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { viewModel.save(onDone) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.title.isNotBlank()
            ) {
                Text(if (templateId == null) "Προσθήκη στην Κατηγορία" else "Αποθήκευση Αλλαγών")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
