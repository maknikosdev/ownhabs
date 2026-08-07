package com.habitpulse.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Πλέγμα 52 εβδομάδων (7 σειρές x 52 στήλες) που δείχνει την ένταση συνέπειας ανά ημέρα,
 * ακριβώς όπως το heatmap του GitHub. Οι παγωμένες ημέρες (streak freeze) δείχνονται με
 * ένα μικρό ❄️ αντί για χρωματισμό έντασης.
 */
@Composable
fun HeatmapGrid(
    dailyIntensity: Map<LocalDate, Float>, // 0f (καμία δραστηριότητα) .. 1f (πλήρης στόχος)
    baseColor: Color,
    onDayClick: (LocalDate) -> Unit,
    frozenDates: Set<LocalDate> = emptySet(),
    weeks: Int = 52
) {
    val today = LocalDate.now()
    val weekFields = WeekFields.of(Locale.getDefault())
    val startOfThisWeek = today.minusDays(((today.dayOfWeek.value - weekFields.firstDayOfWeek.value + 7) % 7).toLong())
    val firstWeekStart = startOfThisWeek.minusWeeks((weeks - 1).toLong())

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        for (w in 0 until weeks) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                for (d in 0 until 7) {
                    val date = firstWeekStart.plusWeeks(w.toLong()).plusDays(d.toLong())
                    val frozen = frozenDates.contains(date)
                    val intensity = if (date.isAfter(today)) -1f else (dailyIntensity[date] ?: 0f)
                    val cellColor = when {
                        frozen -> Color(0xFF3B82F6).copy(alpha = 0.35f)
                        intensity < 0f -> Color.Transparent
                        intensity <= 0f -> Color.LightGray.copy(alpha = 0.25f)
                        else -> baseColor.copy(alpha = 0.25f + 0.75f * intensity.coerceIn(0f, 1f))
                    }
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(cellColor)
                            .then(
                                if (intensity >= 0f) Modifier.clickable { onDayClick(date) } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (frozen) {
                            androidx.compose.material3.Text("❄", fontSize = 7.sp)
                        }
                    }
                }
            }
        }
    }
}
