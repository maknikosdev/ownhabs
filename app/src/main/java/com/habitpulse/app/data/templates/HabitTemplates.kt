package com.habitpulse.app.data.templates

import com.habitpulse.app.data.local.entity.FrequencyPeriod
import com.habitpulse.app.data.local.entity.GoalType

/**
 * Ένα "έτοιμο πρότυπο" συνήθειας που ο χρήστης μπορεί να επιλέξει με ένα tap
 * αντί να συμπληρώσει τη φόρμα από το μηδέν. Καθαρά τοπικά δεδομένα, hardcoded
 * στην εφαρμογή — δεν κατεβαίνουν από κανένα server. Ο χρήστης μπορεί πάντα να
 * αλλάξει τη συχνότητα (frequencyPeriod / timesPerPeriod) πριν ή μετά την αποθήκευση.
 */
data class HabitTemplate(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val colorHex: String,
    val goalType: GoalType,
    val targetValue: Double,
    val unit: String,
    val category: String,
    val frequencyPeriod: FrequencyPeriod = FrequencyPeriod.DAILY,
    val timesPerPeriod: Int = 1
)

object HabitTemplates {

    const val CATEGORY_HEALTH = "Υγεία & Ευεξία"
    const val CATEGORY_PRODUCTIVITY = "Παραγωγικότητα"
    const val CATEGORY_LIFESTYLE = "Καθημερινότητα"

    val all: List<HabitTemplate> = listOf(
        HabitTemplate("water", "Νερό", "Ενυδάτωση κατά τη διάρκεια της ημέρας.", "💧", "#3B82F6", GoalType.NUMERIC, 2000.0, "ml", CATEGORY_HEALTH),
        HabitTemplate("exercise", "Άσκηση", "Οποιαδήποτε φυσική δραστηριότητα.", "🏃", "#E85D5D", GoalType.BOOLEAN, 1.0, "", CATEGORY_HEALTH, FrequencyPeriod.WEEKLY, 3),
        HabitTemplate("steps", "Βήματα", "Καθημερινός στόχος βημάτων.", "👣", "#2FB6C0", GoalType.NUMERIC, 8000.0, "βήματα", CATEGORY_HEALTH),
        HabitTemplate("sleep", "Ύπνος", "Ώρες ύπνου το βράδυ.", "😴", "#8E7CC3", GoalType.NUMERIC, 8.0, "ώρες", CATEGORY_HEALTH),
        HabitTemplate("vitamins", "Βιταμίνες / Φάρμακα", "Καθημερινή λήψη βιταμινών ή φαρμάκων.", "💊", "#9ED037", GoalType.BOOLEAN, 1.0, "", CATEGORY_HEALTH),
        HabitTemplate("healthy_meal", "Υγιεινό Γεύμα", "Ένα ισορροπημένο γεύμα την ημέρα.", "🥗", "#9ED037", GoalType.BOOLEAN, 1.0, "", CATEGORY_HEALTH),

        HabitTemplate("reading", "Διάβασμα", "Λίγες σελίδες κάθε μέρα.", "📖", "#2FB6C0", GoalType.NUMERIC, 20.0, "σελίδες", CATEGORY_PRODUCTIVITY),
        HabitTemplate("deep_work", "Συγκεντρωμένη Δουλειά", "Χρόνος χωρίς περισπασμούς.", "🎯", "#F2B705", GoalType.NUMERIC, 60.0, "λεπτά", CATEGORY_PRODUCTIVITY),
        HabitTemplate("no_phone_morning", "Χωρίς κινητό το πρωί", "Τα πρώτα 30 λεπτά της ημέρας χωρίς οθόνη.", "📵", "#8E7CC3", GoalType.BOOLEAN, 1.0, "", CATEGORY_PRODUCTIVITY),
        HabitTemplate("journaling", "Ημερολόγιο", "Λίγες γραμμές κάθε βράδυ.", "✍️", "#F2B705", GoalType.BOOLEAN, 1.0, "", CATEGORY_PRODUCTIVITY),
        HabitTemplate("save_money", "Αποταμίευση", "Ποσό που βάζεις στην άκρη.", "💰", "#9ED037", GoalType.NUMERIC, 5.0, "€", CATEGORY_PRODUCTIVITY, FrequencyPeriod.MONTHLY, 4),

        HabitTemplate("clean_home", "Τακτοποίηση Σπιτιού", "10 λεπτά τακτοποίησης.", "🧹", "#9ED037", GoalType.BOOLEAN, 1.0, "", CATEGORY_LIFESTYLE, FrequencyPeriod.WEEKLY, 2),
        HabitTemplate("pet_grooming", "Καθαρισμός Κατοικίδιου", "Μπάνιο, βούρτσισμα ή περιποίηση του κατοικίδιού σου.", "🐾", "#F2B705", GoalType.BOOLEAN, 1.0, "", CATEGORY_LIFESTYLE, FrequencyPeriod.WEEKLY, 2),
        HabitTemplate("call_family", "Επικοινωνία με Οικογένεια", "Ένα τηλέφωνο ή μήνυμα σε αγαπημένο πρόσωπο.", "📞", "#F2B705", GoalType.BOOLEAN, 1.0, "", CATEGORY_LIFESTYLE, FrequencyPeriod.WEEKLY, 3),
        HabitTemplate("learn_language", "Εκμάθηση Γλώσσας", "Λίγα λεπτά εξάσκησης.", "🗣️", "#2FB6C0", GoalType.NUMERIC, 15.0, "λεπτά", CATEGORY_LIFESTYLE),
        HabitTemplate("plants", "Πότισμα Φυτών", "Φροντίδα των φυτών του σπιτιού.", "🌱", "#9ED037", GoalType.BOOLEAN, 1.0, "", CATEGORY_LIFESTYLE, FrequencyPeriod.WEEKLY, 2)
    )

    fun byId(id: String): HabitTemplate? = all.firstOrNull { it.id == id }

    fun groupedByCategory(): Map<String, List<HabitTemplate>> = all.groupBy { it.category }
}
