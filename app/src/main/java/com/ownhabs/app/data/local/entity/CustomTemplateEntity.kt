package com.ownhabs.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Ένα προσαρμοσμένο "πρότυπο" συνήθειας που φτιάχνει ο χρήστης μέσα σε μία δική του
 * κατηγορία (CategoryEntity). Λειτουργεί ακριβώς σαν τα ενσωματωμένα HabitTemplate:
 * εμφανίζεται στη λίστα προτάσεων και με ένα tap προσυμπληρώνει τη φόρμα δημιουργίας
 * συνήθειας — δεν είναι η ίδια η συνήθεια.
 */
@Entity(
    tableName = "custom_templates",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class CustomTemplateEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val categoryId: String,
    val title: String,
    val description: String = "",
    val icon: String = "✅",
    val colorHex: String = "#2FB6C0",
    val goalType: GoalType = GoalType.BOOLEAN,
    val targetValue: Double = 1.0,
    val unit: String = "",
    val frequencyPeriod: FrequencyPeriod = FrequencyPeriod.DAILY,
    val timesPerPeriod: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
