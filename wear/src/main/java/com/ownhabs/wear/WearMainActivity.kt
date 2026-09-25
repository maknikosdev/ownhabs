package com.ownhabs.wear

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ownhabs.wear.health.StepsSetup

class WearMainActivity : ComponentActivity() {

    private val viewModel: WearHabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RequestActivityRecognitionOnce()
                WearHomeScreen(viewModel)
            }
        }
    }
}

/**
 * Το ACTIVITY_RECOGNITION είναι η άδεια που χρειάζεται το Health Services για να μοιραστεί
 * δεδομένα βημάτων (DataType.STEPS_DAILY). Μόλις δοθεί, καταχωρούμε το background listener
 * που στέλνει τα βήματα στο τηλέφωνο· χωρίς αυτήν, η εφαρμογή συνεχίζει κανονικά — απλά
 * χωρίς τη λειτουργία βημάτων.
 */
@Composable
private fun RequestActivityRecognitionOnce() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) StepsSetup.registerPassiveListener(context) }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACTIVITY_RECOGNITION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (granted) {
            StepsSetup.registerPassiveListener(context)
        } else {
            launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }
}

@Composable
private fun WearHomeScreen(viewModel: WearHabitViewModel) {
    val habits by viewModel.habits.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val listState = rememberScalingLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070C15)),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> Text("Φόρτωση…", color = Color(0xFF8A93A6))
            habits.isEmpty() -> Text(
                "Άνοιξε το OwnHabs\nστο τηλέφωνό σου",
                color = Color(0xFF8A93A6),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
            else -> ScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp, start = 8.dp, end = 8.dp)
            ) {
                item {
                    Text("OwnHabs", color = Color.White, fontWeight = FontWeight.Bold)
                }
                items(habits, key = { it.id }) { habit ->
                    HabitRow(habit = habit, onTap = { viewModel.toggleHabit(habit.id) })
                }
            }
        }
    }
}

@Composable
private fun HabitRow(habit: WearHabit, onTap: () -> Unit) {
    val color = runCatching { Color(android.graphics.Color.parseColor(habit.colorHex)) }
        .getOrDefault(Color(0xFF2FB6C0))

    Button(
        onClick = onTap,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (habit.done) color.copy(alpha = 0.35f) else Color(0xFF121B2A)
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.size(28.dp).clip(CircleShape).background(color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(habit.icon)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = habit.title,
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
            Text(if (habit.done) "✓" else "○", color = if (habit.done) color else Color(0xFF8A93A6))
        }
    }
}
