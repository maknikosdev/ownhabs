package com.ownhabs.app.ui.screens.badges

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ownhabs.app.data.repository.HabitRepository
import com.ownhabs.app.ui.navigation.SimpleViewModelFactory
import com.ownhabs.app.ui.strings.LocalStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgesScreen(repository: HabitRepository, onBack: () -> Unit) {
    val strings = LocalStrings.current
    val viewModel: BadgesViewModel = viewModel(factory = SimpleViewModelFactory { BadgesViewModel(repository) })
    val items by viewModel.items.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.badgesTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = strings.back) }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.badge.id }) { item ->
                val unlocked = item.unlockedFor.isNotEmpty()
                Card {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            item.badge.icon,
                            style = MaterialTheme.typography.headlineMedium,
                            color = if (unlocked) Color.Unspecified else Color.Gray.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.badge.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (unlocked) MaterialTheme.colorScheme.onSurface else Color.Gray
                            )
                            Text(item.badge.description, style = MaterialTheme.typography.bodyMedium)
                            if (unlocked) {
                                item.unlockedFor.forEach { (habit, ts) ->
                                    Text(
                                        "✓ ${habit.title} — ${dateFormat.format(Date(ts))}",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            } else {
                                Text(strings.badgesLocked, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}
