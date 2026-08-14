package com.ownhabs.app.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ownhabs.app.data.local.entity.HabitLogEntity
import com.ownhabs.app.data.repository.HabitRepository
import com.ownhabs.app.ui.components.HeatmapGrid
import com.ownhabs.app.ui.navigation.SimpleViewModelFactory
import com.ownhabs.app.ui.strings.AppStrings
import com.ownhabs.app.ui.strings.LocalStrings
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
    val strings = LocalStrings.current
    val viewModel: HistoryViewModel = viewModel(
        factory = SimpleViewModelFactory { HistoryViewModel(repository, habitId) }
    )
    val state by viewModel.state.collectAsState()
    val freezeMessage by viewModel.freezeMessage.collectAsState()
    var editingDate by remember { mutableStateOf<LocalDate?>(null) }
    var editingLog by remember { mutableStateOf<HabitLogEntity?>(null) }

    val habit = state.habit
    val color = habit?.let {
        runCatching { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(it.colorHex)) }
            .getOrDefault(androidx.compose.ui.graphics.Color(0xFF2FB6C0))
    } ?: androidx.compose.ui.graphics.Color(0xFF2FB6C0)

    val freezeMessageText = freezeMessage?.let { msgTypeToText(it, strings) }
    freezeMessage?.let { msg ->
        LaunchedEffect(msg) {
            kotlinx.coroutines.delay(2600)
            viewModel.clearFreezeMessage()
        }
    }

    if (editingDate != null && habit != null) {
        val alreadyFrozen = state.frozenDates.contains(editingDate)
        val hasLogThatDay = editingLog != null
        val freezesLeft = (state.freezesAvailableThisMonth - state.freezesUsedThisMonth).coerceAtLeast(0)

        LogEditDialog(
            date = editingDate!!,
            habit = habit,
            strings = strings,
            existing = editingLog,
            canFreeze = !hasLogThatDay && !alreadyFrozen && freezesLeft > 0 && editingDate!! != LocalDate.now(),
            freezesLeft = freezesLeft,
            isFrozen = alreadyFrozen,
            onDismiss = { editingDate = null; editingLog = null },
            onSave = { value, notes ->
                viewModel.addOrEditLog(editingDate!!, value, notes, editingLog)
                editingDate = null; editingLog = null
            },
            onDelete = editingLog?.let { log -> { viewModel.deleteLog(log); editingDate = null; editingLog = null } },
            onFreeze = {
                viewModel.applyStreakFreeze(editingDate!!)
                editingDate = null; editingLog = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habit?.title ?: strings.historyFallbackTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = strings.back) }
                }
            )
        },
        snackbarHost = {
            freezeMessageText?.let { msg ->
                Snackbar(modifier = Modifier.padding(12.dp)) { Text(msg) }
            }
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
                    StatChip("🔥 ${strings.historyCurrentStreak}", "${state.streak.current}")
                    StatChip("🏆 ${strings.historyBestStreak}", "${state.streak.best}")
                    StatChip("✅ ${strings.historyTotal}", "${state.totalCompletions}")
                }
            }

            item {
                Card {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AcUnit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(strings.historyStreakFreezeTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                strings.historyStreakFreezeDesc,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Text(
                            strings.historyStreakFreezeAvailable(
                                (state.freezesAvailableThisMonth - state.freezesUsedThisMonth).coerceAtLeast(0),
                                state.freezesAvailableThisMonth
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Column {
                    Text(strings.historyHeatmapTitle, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    HeatmapGrid(
                        dailyIntensity = state.dailyIntensity,
                        frozenDates = state.frozenDates,
                        baseColor = color,
                        onDayClick = { date ->
                            editingLog = state.logs.firstOrNull {
                                Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == date
                            }
                            editingDate = date
                        }
                    )
                    Text(
                        strings.historyHeatmapHint,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            item {
                Text(strings.historyRecentLogs, style = MaterialTheme.typography.titleMedium)
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
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = null) }
        }
    }
}

private fun msgTypeToText(type: FreezeMsgType, strings: AppStrings): String = when (type) {
    FreezeMsgType.APPLIED -> strings.freezeMsgApplied
    FreezeMsgType.ALREADY_LOGGED -> strings.freezeMsgAlreadyLogged
    FreezeMsgType.ALREADY_FROZEN -> strings.freezeMsgAlreadyFrozen
    FreezeMsgType.LIMIT_REACHED -> strings.freezeMsgLimitReached
}

@Composable
private fun LogEditDialog(
    date: LocalDate,
    habit: com.ownhabs.app.data.local.entity.HabitEntity,
    strings: AppStrings,
    existing: HabitLogEntity?,
    canFreeze: Boolean,
    freezesLeft: Int,
    isFrozen: Boolean,
    onDismiss: () -> Unit,
    onSave: (Double, String) -> Unit,
    onDelete: (() -> Unit)?,
    onFreeze: () -> Unit
) {
    var value by remember { mutableStateOf((existing?.value ?: habit.targetValue).toString()) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(formatter.format(date)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isFrozen) {
                    Text(strings.historyFrozenDayNotice, style = MaterialTheme.typography.bodyMedium)
                } else {
                    OutlinedTextField(
                        value = value,
                        onValueChange = { value = it },
                        label = { Text(if (habit.goalType.name == "BOOLEAN") strings.historyValueLabelBoolean else strings.historyValueLabelNumeric(habit.unit)) }
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(strings.historyNotesLabel) }
                    )
                    if (canFreeze) {
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(onClick = onFreeze, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.AcUnit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(strings.historyUseFreezeButton(freezesLeft))
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isFrozen) {
                TextButton(onClick = { onSave(value.toDoubleOrNull() ?: 0.0, notes) }) { Text(strings.save) }
            } else {
                TextButton(onClick = onDismiss) { Text(strings.close) }
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = null); Text(" " + strings.delete) }
                }
                if (!isFrozen) TextButton(onClick = onDismiss) { Text(strings.cancel) }
            }
        }
    )
}
