package com.ownhabs.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Ένα "streak freeze" καλύπτει μια χαμένη ημέρα ώστε να ΜΗΝ σπάσει το τρέχον σερί.
 * Δεν μετράει σαν πραγματική ολοκλήρωση (δεν επηρεάζει badges/total), απλά προστατεύει
 * τη συνέχεια. Περιορισμός: FREEZES_PER_MONTH ανά συνήθεια ανά ημερολογιακό μήνα.
 */
@Entity(
    tableName = "streak_freezes",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("habitId"), Index("dateEpochDay")]
)
data class StreakFreezeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val dateEpochDay: Long, // LocalDate.toEpochDay()
    val appliedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val FREEZES_PER_MONTH = 1
    }
}
