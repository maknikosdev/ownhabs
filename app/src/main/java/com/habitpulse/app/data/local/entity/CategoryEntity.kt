package com.habitpulse.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Κατηγορία που δημιουργεί ο ΧΡΗΣΤΗΣ (π.χ. "Κατοικίδιο", "Σπίτι", "Δουλειά").
 * Οι ενσωματωμένες κατηγορίες του HabitTemplates.kt (Υγεία, Παραγωγικότητα, Καθημερινότητα)
 * παραμένουν hardcoded/read-only και ΔΕΝ αποθηκεύονται εδώ.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String = "📁",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
