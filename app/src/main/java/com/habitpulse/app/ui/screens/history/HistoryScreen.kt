package com.habitpulse.app.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitpulse.app.data.local.entity.HabitLogEntity
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.ui.components.HeatmapGrid
import com.habitpulse.app.ui.navigation.SimpleViewModelFactory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    repository: HabitRepository,
    habitId: String,
    onBack: () -> Unit
) {
    val viewModel: HistoryViewModel = viewModel(
        factory = SimpleViewModelFactory { HistoryViewModel(repository, habitId) }
    )
    val state by viewModel.state.collectAsState()
    var editingDate by remember { mutableStateOf<LocalDate?>(null) }
    var editingLog by remember { mutableStateOf<HabitLogEntity?>(null) }

    val habit = state.habit
    val color = habit?.let {
        runCatching { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it.colorHex)) }
            .getOrDefault(androidx.compose.ui.graphics.Color(0xFF2FB6C0))
    } ?: androidx.compose.ui.graphics.Color(0xFF2FB6C0)

    if (editingDate != null && habit != null) {
        LogEditDialog(
            date = editingDate!!,
            habit = habit,
            existing = editingLog,
            onDismiss = { editingDate = null; editingLog = null },
            onSave = { value, notes ->
                viewModel.addOrEditLog(editingDate!!, value, notes, editingLog)
                editingDate = null; editingLog = null
            },
            onDelete = editingLog?.let { log -> { viewModel.deleteLog(log); editingDate = null; editingLog = null } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit?.title ?: "Ιστορικό") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω") }
                }
            )
        }
    ) { padding ->
        if (habit == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatChip("🔥 Τρέχον Σερί", "${state.streak.current}")
                    StatChip("🏆 Καλύτερο Σερί", "${state.streak.best}")
                    StatChip("✅ Σύνολο", "${state.totalCompletions}")
                }
            }

            item {
                Column {
                    Text("Πλέγμα Συνέπειας (52 εβδομάδες)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    HeatmapGrid(
                        dailyIntensity = state.dailyIntensity,
                        baseColor = color,
                        onDayClick = { date ->
                            editingLog = state.logs.firstOrNull {
                                Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == date
                            }
                            editingDate = date
                        }
                    )
                    Text(
                        "Πάτησε σε οποιαδήποτε ημέρα για να προσθέσεις, αλλάξεις ή διαγράψεις μια καταγραφή.",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            item {
                Text("Πρόσφατες Καταγραφές", style = MaterialTheme.typography.titleMedium)
            }

            items(state.logs, key = { it.id }) { log ->
                LogRow(
                    log = log,
                    unit = habit.unit,
                    onEdit = {
                        editingLog = log
                        editingDate = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                )
            }
        }
    }
}

@Composable
private fun RowScope.StatChip(label: String, value: String) {
    Card(modifier = Modifier.weight(1f)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun LogRow(log: HabitLogEntity, unit: String, onEdit: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }
    val dateTime = Instant.ofEpochMilli(log.timestamp).atZone(ZoneId.systemDefault())
    Card {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(formatter.format(dateTime), style = MaterialTheme.typography.bodyMedium)
                Text("${log.value} $unit".trim(), style = MaterialTheme.typography.labelSmall)
                if (log.notes.isNotBlank()) {
                    Text(log.notes, style = MaterialTheme.typography.labelSmall)
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Επεξεργασία") }
        }
    }
}

@Composable
private fun LogEditDialog(
    date: LocalDate,
    habit: com.habitpulse.app.data.local.entity.HabitEntity,
    existing: HabitLogEntity?,
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit,
    onDelete: (() -> Unit)?
) {
    var value by remember { mutableStateOf((existing?.value ?: habit.targetValue).toString()) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(formatter.format(date)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(if (habit.goalType.name == "BOOLEAN") "1 = ολοκληρώθηκε, 0 = όχι" else "Τιμή (${habit.unit})") }
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Σημειώσεις") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(value.toDoubleOrNull() ?: 0.0, notes) }) { Text("Αποθήκευση") }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null); Text(" Διαγραφή") }
                }
                TextButton(onClick = onDismiss) { Text("Ακύρωση") }
            }
        }
    )
}
