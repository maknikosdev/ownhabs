package com.ownhabs.app.ui.screens.recap

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ownhabs.app.data.repository.HabitRepository
import com.ownhabs.app.domain.RecapData
import com.ownhabs.app.ui.navigation.SimpleViewModelFactory
import com.ownhabs.app.ui.util.shareRecapImage
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearRecapScreen(
    repository: HabitRepository,
    year: Int = LocalDate.now().year,
    onBack: () -> Unit
) {
    val viewModel: YearRecapViewModel = viewModel(
        factory = SimpleViewModelFactory { YearRecapViewModel(repository, year) }
    )
    val data by viewModel.data.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Το Έτος σου σε Pixels") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Πίσω") }
                },
                actions = {
                    if (data != null) {
                        IconButton(onClick = {
                            scope.launch {
                                val bitmap = graphicsLayer.toImageBitmap()
                                shareRecapImage(context, bitmap, "ownhabs_recap_$year.png")
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Κοινοποίηση")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val current = data
        if (current == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Μόνο ό,τι βρίσκεται μέσα σε αυτό το Box "τραβιέται" στην εικόνα κοινοποίησης.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        graphicsLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(graphicsLayer)
                    }
            ) {
                RecapShareCard(year = year, recap = current)
            }

            RecapStatsList(recap = current)
        }
    }
}

@Composable
private fun RecapShareCard(year: Int, recap: RecapData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF070C15))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("OwnHabs · $year", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Text(
            "${(recap.completionRate * 100).toInt()}% μέση συνέπεια",
            color = Color(0xFF6ED15A),
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.titleMedium
        )

        YearPixelGrid(dailyRatio = recap.dailyRatio, year = year)

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            MiniStat("🔥 ${recap.bestStreak}", "καλύτερο σερί")
            MiniStat("✅ ${recap.totalCompletions}", "καταγραφές")
            MiniStat("🏆 ${recap.badgesUnlockedCount}", "badges")
        }

        if (recap.mostConsistentHabitTitle != null) {
            Text(
                "Πιο συνεπής συνήθεια: ${recap.mostConsistentHabitTitle} (${(recap.mostConsistentHabitRate * 100).toInt()}%)",
                color = Color(0xFF8A93A6),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String) {
    Column {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, color = Color(0xFF8A93A6), style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun YearPixelGrid(dailyRatio: Map<LocalDate, Float>, year: Int) {
    val yearStart = LocalDate.of(year, 1, 1)
    val weekFields = WeekFields.of(Locale.getDefault())
    val gridStart = yearStart.minusDays(((yearStart.dayOfWeek.value - weekFields.firstDayOfWeek.value + 7) % 7).toLong())
    val weeks = 53

    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (w in 0 until weeks) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                for (d in 0 until 7) {
                    val date = gridStart.plusWeeks(w.toLong()).plusDays(d.toLong())
                    val inYear = date.year == year
                    val ratio = dailyRatio[date] ?: 0f
                    val color = when {
                        !inYear -> Color.Transparent
                        ratio <= 0f -> Color(0xFF1B2740)
                        else -> Color(0xFF6ED15A).copy(alpha = 0.25f + 0.75f * ratio)
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecapStatsList(recap: RecapData) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Λεπτομέρειες", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        StatRow("Ενεργές συνήθειες", "${recap.activeHabitsCount}")
        StatRow("Σύνολο καταγραφών", "${recap.totalCompletions}")
        StatRow("Καλύτερο σερί", if (recap.bestStreakHabitTitle != null) "${recap.bestStreak} ημέρες («${recap.bestStreakHabitTitle}»)" else "—")
        StatRow("Badges που ξεκλειδώθηκαν", "${recap.badgesUnlockedCount}")
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
