package com.habitpulse.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habitpulse.app.data.local.entity.GoalType
import com.habitpulse.app.data.local.entity.HabitEntity
import com.habitpulse.app.ui.strings.AppStrings

/**
 * Διάλογος καταγραφής: ο χρήστης επιλέγει ρητά αν μια συνήθεια έγινε πλήρως, εν μέρει
 * (με ακριβή τιμή μέσω slider) ή καθόλου — αντί για σιωπηλά, σωρευτικά taps.
 */
@Composable
fun QuickLogDialog(
    habit: HabitEntity,
    currentValue: Double,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    val target = habit.targetValue.takeIf { it > 0 } ?: 1.0
    var fraction by remember {
        mutableStateOf((currentValue / target).toFloat().coerceIn(0f, 1.2f))
    }

    val displayValue = (fraction * target)
    val isBoolean = habit.goalType == GoalType.BOOLEAN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(habit.icon)
                Spacer(Modifier.width(8.dp))
                Text(strings.quickLogTitle)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    AssistChip(
                        onClick = { fraction = 0f },
                        label = { Text(strings.quickLogNotDone) },
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(
                        onClick = { fraction = 0.5f },
                        label = { Text(strings.quickLogPartial) },
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(
                        onClick = { fraction = 1f },
                        label = { Text(strings.quickLogDoneFull) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Slider(
                    value = fraction,
                    onValueChange = { fraction = it },
                    valueRange = 0f..1.2f
                )

                val label = if (isBoolean) {
                    when {
                        fraction <= 0f -> strings.quickLogNotDone
                        fraction >= 1f -> strings.quickLogDoneFull
                        else -> "${strings.quickLogPartial} (${(fraction * 100).toInt()}%)"
                    }
                } else {
                    "${strings.quickLogTodayLabel}: ${"%.1f".format(displayValue)} / ${"%.0f".format(target)} ${habit.unit}"
                }
                Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(displayValue.coerceAtLeast(0.0)) }) { Text(strings.quickLogSave) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
