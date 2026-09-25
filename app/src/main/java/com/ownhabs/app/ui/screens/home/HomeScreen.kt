package com.ownhabs.app.ui.screens.home

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ownhabs.app.data.local.entity.GoalType
import com.ownhabs.app.data.local.entity.HabitEntity
import com.ownhabs.app.data.local.entity.FrequencyPeriod
import com.ownhabs.app.data.repository.HabitRepository
import com.ownhabs.app.domain.FrequencyCalculator
import com.ownhabs.app.ui.navigation.SimpleViewModelFactory
import com.ownhabs.app.ui.strings.AppStrings
import com.ownhabs.app.ui.strings.LocalStrings
import com.ownhabs.app.ui.components.QuickLogDialog
import com.ownhabs.app.wear.WearStepsStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: HabitRepository,
    onAddHabit: () -> Unit,
    onEditHabit: (String) -> Unit,
    onOpenHistory: (String) -> Unit,
    onOpenBadges: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenYearRecap: () -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(factory = SimpleViewModelFactory { HomeViewModel(repository, context.applicationContext) })
    val habits by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { WearStepsStore.init(context.applicationContext) }
    val stepsState by WearStepsStore.state.collectAsState()

    var celebrationBadges by remember { mutableStateOf<List<String>>(emptyList()) }
    var habitToDelete by remember { mutableStateOf<HabitEntity?>(null) }
    var habitToQuickLog by remember { mutableStateOf<HomeHabitUiState?>(null) }

    if (habitToQuickLog != null) {
        QuickLogDialog(
            habit = habitToQuickLog!!.habit,
            currentValue = habitToQuickLog!!.todayValue,
            strings = strings,
            onDismiss = { habitToQuickLog = null },
            onConfirm = { value ->
                viewModel.setTodayValue(habitToQuickLog!!.habit, value) { badges -> celebrationBadges = badges }
                habitToQuickLog = null
            }
        )
    }

    if (habitToDelete != null) {
        AlertDialog(
            onDismissRequest = { habitToDelete = null },
            title = { Text(strings.homeDeleteConfirmTitle) },
            text = { Text(strings.homeDeleteConfirmText(habitToDelete!!.title)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHabitPermanently(habitToDelete!!)
                    habitToDelete = null
                }) { Text(strings.homeDeleteConfirmButton) }
            },
            dismissButton = { TextButton(onClick = { habitToDelete = null }) { Text(strings.cancel) } }
        )
    }

    if (celebrationBadges.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { celebrationBadges = emptyList() },
            confirmButton = {
                TextButton(onClick = { celebrationBadges = emptyList() }) { Text("🎉") }
            },
            title = { Text(if (com.ownhabs.app.ui.strings.LocalLang.current == com.ownhabs.app.ui.strings.Lang.EL) "🎉 Νέο Badge!" else "🎉 New Badge!") },
            text = { Text(celebrationBadges.joinToString(", ")) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.appName, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenYearRecap) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Year in Pixels")
                    }
                    IconButton(onClick = onOpenBadges) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = strings.navBadges)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = strings.navSettings)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddHabit) {
                Icon(Icons.Default.Add, contentDescription = strings.add)
            }
        }
    ) { padding ->
        if (habits.isEmpty() && stepsState == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(strings.homeEmptyTitle, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (stepsState != null && stepsState!!.isFresh) {
                    item(key = "wear_steps_card") {
                        WearStepsCard(state = stepsState!!, strings = strings)
                    }
                }
                if (habits.isEmpty()) {
                    item(key = "empty_state") {
                        Box(Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                            Text(strings.homeEmptyTitle, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
                items(habits, key = { it.habit.id }) { state ->
                    HabitCard(
                        state = state,
                        strings = strings,
                        onTap = {
                            val vibrator = context.getSystemService(Vibrator::class.java)
                            runCatching {
                                vibrator?.let {
                                    if (it.hasVibrator()) it.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
                                }
                            }
                            habitToQuickLog = state
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

/** Μικρή κάρτα στην κορυφή της Αρχικής με τα σημερινά βήματα, όπως τα έστειλε το ρολόι. */
@Composable
private fun WearStepsCard(state: com.ownhabs.app.wear.WearStepsState, strings: AppStrings) {
    val minutesAgo = ((System.currentTimeMillis() - state.updatedAt) / 60000L).toInt()
    val freshnessLabel = if (minutesAgo <= 1) strings.homeStepsUpdatedJustNow else strings.homeStepsUpdatedMinutesAgo(minutesAgo)

    Card(shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF9ED037).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsWalk, contentDescription = null, tint = Color(0xFF9ED037))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(strings.homeStepsCardTitle, style = MaterialTheme.typography.labelMedium)
                Text(strings.homeStepsCount(state.steps), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(freshnessLabel, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun HabitCard(
    state: HomeHabitUiState,
    strings: AppStrings,
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
                            "${state.progress.completedUnits}/${state.progress.requiredUnits} ${periodLabel(habit.frequencyPeriod, strings)}" +
                                if (habit.goalType == GoalType.NUMERIC) " · ${strings.homeTimesToday} ${state.todayValue.toInt()}/${habit.targetValue.toInt()} ${habit.unit}" else ""
                        habit.goalType == GoalType.BOOLEAN ->
                            if (state.progress.metToday) strings.homeCompleted else strings.homePending
                        else -> "${state.todayValue.toInt()} / ${habit.targetValue.toInt()} ${habit.unit}"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                if (state.currentStreak > 0) {
                    Text("🔥 ${state.currentStreak} ${strings.homeStreakDays}", style = MaterialTheme.typography.labelSmall)
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
                    contentDescription = strings.add
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(strings.homeMenuEdit) },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = { menuExpanded = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text(strings.homeMenuArchive) },
                        leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) },
                        onClick = { menuExpanded = false; onArchive() }
                    )
                    DropdownMenuItem(
                        text = { Text(strings.homeMenuDelete) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}

private fun periodLabel(period: FrequencyPeriod, strings: AppStrings): String = when (period) {
    FrequencyPeriod.WEEKLY -> strings.homeTimesThisWeek
    FrequencyPeriod.MONTHLY -> strings.homeTimesThisMonth
    FrequencyPeriod.DAILY -> strings.homeTimesToday
}
