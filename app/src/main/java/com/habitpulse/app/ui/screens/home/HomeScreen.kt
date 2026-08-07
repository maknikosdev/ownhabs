package com.habitpulse.app.ui.screens.home

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitpulse.app.data.local.entity.GoalType
import com.habitpulse.app.data.local.entity.HabitEntity
import com.habitpulse.app.data.local.entity.FrequencyPeriod
import com.habitpulse.app.data.repository.HabitRepository
import com.habitpulse.app.domain.FrequencyCalculator
import com.habitpulse.app.ui.navigation.SimpleViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: HabitRepository,
    onAddHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    onOpenHistory: (String) -> Unit,
    onOpenBadges: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = SimpleViewModelFactory { HomeViewModel(repository) })
    val habits by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var celebrationBadges by remember { mutableStateOf<List<String>>(emptyList()) }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }

    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text("Μόνιμη διαγραφή;") },
            text = { Text("Θα διαγραφούν η «${habitToDelete!!.title}» και όλο το ιστορικό καταγραφών της. Η ενέργεια δεν αναιρείται.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHabitPermanently(habitToDelete!!)
                    habitToDelete = null
                }) { Text("Διαγραφή") }
            },
            dismissButton = { TextButton(onClick = { habitToDelete = null }) { Text("Ακύρωση") } }
        )
    }

    if (celebrationBadges.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { celebrationBadges = emptyList() },
            confirmButton = {
                TextButton(onClick = { celebrationBadges = emptyList() }) { Text("Ωραία!") }
            },
            title = { Text("🎉 Νέο Badge!") },
            text = { Text(celebrationBadges.joinToString(", ")) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HabitPulse", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenBadges) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Badges")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ρυθμίσεις")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHabit) {
                Icon(Icons.Default.Add, contentDescription = "Προσθήκη Συνήθειας")
            }
        }
    ) { padding ->
        if (habits.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Δεν έχεις προσθέσει ακόμα καμία συνήθεια.\nΠάτησε + για να ξεκινήσεις.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habits, key = { it.habit.id }) { state ->
                    HabitCard(
                        state = state,
                        onTap = {
                            val vibrator = context.getSystemService(Vibrator::class.java)
                            vibrator?.let {
                                if (it.hasVibrator()) it.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                            }
                            val delta = if (state.habit.goalType == GoalType.BOOLEAN) 1.0 else state.habit.targetValue / 8.0
                            viewModel.logCompletion(state.habit, delta) { badges -> celebrationBadges = badges }
                        },
                        onOpenHistory = { onOpenHistory(state.habit.id) },
                        onEdit = { onEditHabit(state.habit.id) },
                        onArchive = { viewModel.archiveHabit(state.habit) },
                        onDelete = { habitToDelete = state.habit }
                    )
                }
            }
        }
    }
}

@Composable
private fun HabitCard(
    state: HomeHabitUiState,
    onTap: () -> Unit,
    onOpenHistory: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val habit = state.habit
    val color = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }.getOrDefault(Color(0xFF2FB6C0))
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpenHistory() },
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(habit.icon, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(habit.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = when {
                        habit.frequencyPeriod != FrequencyPeriod.DAILY ->
                            "${state.progress.completedUnits}/${state.progress.requiredUnits} φορές ${periodLabel(habit.frequencyPeriod)}" +
                                if (habit.goalType == GoalType.NUMERIC) " · σήμερα ${state.todayValue.toInt()}/${habit.targetValue.toInt()} ${habit.unit}" else ""
                        habit.goalType == GoalType.BOOLEAN ->
                            if (state.progress.metToday) "Ολοκληρώθηκε ✓" else "Εκκρεμεί"
                        else -> "${state.todayValue.toInt()} / ${habit.targetValue.toInt()} ${habit.unit}"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (state.currentStreak > 0) {
                    Text("🔥 ${state.currentStreak} ημέρες σερί", style = MaterialTheme.typography.labelSmall)
                }
                if (habit.frequencyPeriod != FrequencyPeriod.DAILY) {
                    Box(Modifier.padding(top = 6.dp)) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth(state.progress.fraction)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.width(4.dp))
            FilledIconButton(
                onClick = onTap,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = color)
            ) {
                Icon(
                    if (state.progress.metToday) Icons.Default.EmojiEvents else Icons.Default.Add,
                    contentDescription = "Καταγραφή"
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Περισσότερα")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Επεξεργασία") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = { menuExpanded = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Αρχειοθέτηση") },
                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                        onClick = { menuExpanded = false; onArchive() }
                    )
                    DropdownMenuItem(
                        text = { Text("Μόνιμη Διαγραφή") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

private fun periodLabel(period: FrequencyPeriod): String = when (period) {
    FrequencyPeriod.WEEKLY -> "αυτή την εβδομάδα"
    FrequencyPeriod.MONTHLY -> "αυτόν τον μήνα"
    FrequencyPeriod.DAILY -> "σήμερα"
}
