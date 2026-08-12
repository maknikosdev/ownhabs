package com.habitpulse.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.habitpulse.app.data.local.entity.GoalType
import com.habitpulse.app.data.local.entity.HabitEntity
import com.habitpulse.app.ui.strings.AppStrings
import java.util.Locale

/**
 * Διάλογος καταγραφής: ο χρήστης επιλέγει ρητά αν μια συνήθεια έγινε πλήρως, εν μέρει
 * ή καθόλου — με slider για γρήγορη προσέγγιση, ΚΑΙ πεδίο κειμένου για ακριβή τιμή
 * (π.χ. "509.4"), αντί να είναι αναγκασμένος να σέρνει μόνο τη μπάρα.
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
    val isBoolean = habit.goalType == GoalType.BOOLEAN

    fun formatNumber(v: Double): String =
        if (v == v.toLong().toDouble()) v.toLong().toString()
        else String.format(Locale.US, "%.1f", v)

    var valueText by remember { mutableStateOf(formatNumber(currentValue)) }
    var sliderFraction by remember { mutableStateOf((currentValue / target).toFloat().coerceIn(0f, 1f)) }

    fun applyFraction(f: Float) {
        sliderFraction = f.coerceIn(0f, 1f)
        valueText = formatNumber(sliderFraction * target)
    }

    fun applyTypedValue(text: String) {
        valueText = text
        val parsed = text.replace(',', '.').toDoubleOrNull()
        if (parsed != null) {
            sliderFraction = (parsed / target).toFloat().coerceIn(0f, 1f)
        }
    }

    val finalValue = valueText.replace(',', '.').toDoubleOrNull() ?: (sliderFraction * target)

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
                        onClick = { applyFraction(0f) },
                        label = { Text(strings.quickLogNotDone) },
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(
                        onClick = { applyFraction(0.5f) },
                        label = { Text(strings.quickLogPartial) },
                        modifier = Modifier.weight(1f)
                    )
                    AssistChip(
                        onClick = { applyFraction(1f) },
                        label = { Text(strings.quickLogDoneFull) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Slider(
                    value = sliderFraction,
                    onValueChange = { applyFraction(it) },
                    valueRange = 0f..1f
                )

                if (isBoolean) {
                    val label = when {
                        finalValue <= 0.0 -> strings.quickLogNotDone
                        finalValue >= target -> strings.quickLogDoneFull
                        else -> "${strings.quickLogPartial} (${(sliderFraction * 100).toInt()}%)"
                    }
                    Text(label, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                } else {
                    // Ακριβής τιμή, επεξεργάσιμη απευθείας — όχι μόνο μέσω του slider.
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = valueText,
                            onValueChange = { applyTypedValue(it) },
                            label = { Text(strings.quickLogTodayLabel) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Text("/ ${formatNumber(target)} ${habit.unit}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(finalValue.coerceAtLeast(0.0)) }) { Text(strings.quickLogSave) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        }
    )
}
