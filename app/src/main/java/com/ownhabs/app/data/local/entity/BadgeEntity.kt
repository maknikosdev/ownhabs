package com.ownhabs.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class BadgeCriterion { STREAK, GOAL_EXCEEDED, TOTAL_LOGS }

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val criterion: BadgeCriterion,
    val targetValue: Double
)

// Seed catalogue matching the spec: Overachiever, Consistent Flame, Record Breaker, Centurion
object DefaultBadges {
    fun seed(): List<BadgeEntity> = listOf(
        BadgeEntity("overachiever", "Overachiever", "Κατέγραψες τιμή μεγαλύτερη από τον στόχο σου.", "🚀", BadgeCriterion.GOAL_EXCEEDED, 0.0),
        BadgeEntity("flame_7", "Consistent Flame x7", "Σερί 7 συνεχόμενων ημερών.", "🔥", BadgeCriterion.STREAK, 7.0),
        BadgeEntity("flame_30", "Consistent Flame x30", "Σερί 30 συνεχόμενων ημερών.", "🔥", BadgeCriterion.STREAK, 30.0),
        BadgeEntity("flame_90", "Consistent Flame x90", "Σερί 90 συνεχόμενων ημερών.", "🔥", BadgeCriterion.STREAK, 90.0),
        BadgeEntity("flame_365", "Consistent Flame x365", "Σερί 365 συνεχόμενων ημερών.", "🔥", BadgeCriterion.STREAK, 365.0),
        BadgeEntity("record_breaker", "Record Breaker", "Ξεπέρασες το προσωπικό σου ρεκόρ σερί.", "🏆", BadgeCriterion.STREAK, -1.0),
        BadgeEntity("centurion", "Centurion", "100 συνολικές ολοκληρώσεις σε μια συνήθεια.", "💯", BadgeCriterion.TOTAL_LOGS, 100.0)
    )
}
