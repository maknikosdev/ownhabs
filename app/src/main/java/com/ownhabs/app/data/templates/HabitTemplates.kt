package com.ownhabs.app.data.templates

import com.ownhabs.app.data.local.entity.FrequencyPeriod
import com.ownhabs.app.data.local.entity.GoalType
import com.ownhabs.app.ui.strings.Lang

/** Οι 3 ενσωματωμένες κατηγορίες προτάσεων — σταθερό key + μεταφρασμένη ετικέτα ανά γλώσσα. */
enum class TemplateCategory {
    HEALTH, PRODUCTIVITY, LIFESTYLE;

    fun label(lang: Lang): String = when (this) {
        HEALTH -> if (lang == Lang.EL) "Υγεία & Ευεξία" else "Health & Wellness"
        PRODUCTIVITY -> if (lang == Lang.EL) "Παραγωγικότητα" else "Productivity"
        LIFESTYLE -> if (lang == Lang.EL) "Καθημερινότητα" else "Everyday Life"
    }
}

/**
 * Ένα "έτοιμο πρότυπο" συνήθειας που ο χρήστης μπορεί να επιλέξει με ένα tap
 * αντί να συμπληρώσει τη φόρμα από το μηδέν. Καθαρά τοπικά δεδομένα, hardcoded
 * στην εφαρμογή — δεν κατεβαίνουν από κανένα server. Δίγλωσσο: τίτλος/περιγραφή
 * υπάρχουν και στις δύο γλώσσες, επιλέγονται δυναμικά μέσω title(lang)/description(lang).
 */
data class HabitTemplate(
    val id: String,
    val titleEl: String,
    val titleEn: String,
    val descriptionEl: String,
    val descriptionEn: String,
    val icon: String,
    val colorHex: String,
    val goalType: GoalType,
    val targetValue: Double,
    val unit: String,
    val category: TemplateCategory,
    val frequencyPeriod: FrequencyPeriod = FrequencyPeriod.DAILY,
    val timesPerPeriod: Int = 1
) {
    fun title(lang: Lang): String = if (lang == Lang.EL) titleEl else titleEn
    fun description(lang: Lang): String = if (lang == Lang.EL) descriptionEl else descriptionEn
}

object HabitTemplates {

    val all: List<HabitTemplate> = listOf(
        HabitTemplate("water", "Νερό", "Water", "Ενυδάτωση κατά τη διάρκεια της ημέρας.", "Stay hydrated throughout the day.", "💧", "#3B82F6", GoalType.NUMERIC, 2000.0, "ml", TemplateCategory.HEALTH),
        HabitTemplate("exercise", "Άσκηση", "Exercise", "Οποιαδήποτε φυσική δραστηριότητα.", "Any kind of physical activity.", "🏃", "#E85D5D", GoalType.BOOLEAN, 1.0, "", TemplateCategory.HEALTH, FrequencyPeriod.WEEKLY, 3),
        HabitTemplate("steps", "Βήματα", "Steps", "Καθημερινός στόχος βημάτων.", "Daily step count goal.", "👣", "#2FB6C0", GoalType.NUMERIC, 8000.0, "steps", TemplateCategory.HEALTH),
        HabitTemplate("sleep", "Ύπνος", "Sleep", "Ώρες ύπνου το βράδυ.", "Hours of sleep at night.", "😴", "#8E7CC3", GoalType.NUMERIC, 8.0, "hrs", TemplateCategory.HEALTH),
        HabitTemplate("vitamins", "Βιταμίνες / Φάρμακα", "Vitamins / Medication", "Καθημερινή λήψη βιταμινών ή φαρμάκων.", "Take your daily vitamins or medication.", "💊", "#9ED037", GoalType.BOOLEAN, 1.0, "", TemplateCategory.HEALTH),
        HabitTemplate("healthy_meal", "Υγιεινό Γεύμα", "Healthy Meal", "Ένα ισορροπημένο γεύμα την ημέρα.", "One balanced meal a day.", "🥗", "#9ED037", GoalType.BOOLEAN, 1.0, "", TemplateCategory.HEALTH),

        HabitTemplate("reading", "Διάβασμα", "Reading", "Λίγες σελίδες κάθε μέρα.", "A few pages every day.", "📖", "#2FB6C0", GoalType.NUMERIC, 20.0, "pages", TemplateCategory.PRODUCTIVITY),
        HabitTemplate("deep_work", "Συγκεντρωμένη Δουλειά", "Deep Work", "Χρόνος χωρίς περισπασμούς.", "Focused time without distractions.", "🎯", "#F2B705", GoalType.NUMERIC, 60.0, "min", TemplateCategory.PRODUCTIVITY),
        HabitTemplate("no_phone_morning", "Χωρίς κινητό το πρωί", "No Phone in the Morning", "Τα πρώτα 30 λεπτά της ημέρας χωρίς οθόνη.", "The first 30 minutes of the day, screen-free.", "📵", "#8E7CC3", GoalType.BOOLEAN, 1.0, "", TemplateCategory.PRODUCTIVITY),
        HabitTemplate("journaling", "Ημερολόγιο", "Journaling", "Λίγες γραμμές κάθε βράδυ.", "A few lines every evening.", "✍️", "#F2B705", GoalType.BOOLEAN, 1.0, "", TemplateCategory.PRODUCTIVITY),
        HabitTemplate("save_money", "Αποταμίευση", "Saving Money", "Ποσό που βάζεις στην άκρη.", "An amount you set aside.", "💰", "#9ED037", GoalType.NUMERIC, 5.0, "€", TemplateCategory.PRODUCTIVITY, FrequencyPeriod.MONTHLY, 4),

        HabitTemplate("clean_home", "Τακτοποίηση Σπιτιού", "Tidy the House", "10 λεπτά τακτοποίησης.", "10 minutes of tidying up.", "🧹", "#9ED037", GoalType.BOOLEAN, 1.0, "", TemplateCategory.LIFESTYLE, FrequencyPeriod.WEEKLY, 2),
        HabitTemplate("pet_grooming", "Καθαρισμός Κατοικίδιου", "Pet Grooming", "Μπάνιο, βούρτσισμα ή περιποίηση του κατοικίδιού σου.", "Bathing, brushing, or grooming your pet.", "🐾", "#F2B705", GoalType.BOOLEAN, 1.0, "", TemplateCategory.LIFESTYLE, FrequencyPeriod.WEEKLY, 2),
        HabitTemplate("call_family", "Επικοινωνία με Οικογένεια", "Call Family", "Ένα τηλέφωνο ή μήνυμα σε αγαπημένο πρόσωπο.", "A call or message to someone you love.", "📞", "#F2B705", GoalType.BOOLEAN, 1.0, "", TemplateCategory.LIFESTYLE, FrequencyPeriod.WEEKLY, 3),
        HabitTemplate("learn_language", "Εκμάθηση Γλώσσας", "Learn a Language", "Λίγα λεπτά εξάσκησης.", "A few minutes of practice.", "🗣️", "#2FB6C0", GoalType.NUMERIC, 15.0, "min", TemplateCategory.LIFESTYLE),
        HabitTemplate("plants", "Πότισμα Φυτών", "Water the Plants", "Φροντίδα των φυτών του σπιτιού.", "Taking care of your houseplants.", "🌱", "#9ED037", GoalType.BOOLEAN, 1.0, "", TemplateCategory.LIFESTYLE, FrequencyPeriod.WEEKLY, 2)
    )

    fun byId(id: String): HabitTemplate? = all.firstOrNull { it.id == id }

    fun groupedByCategory(): Map<TemplateCategory, List<HabitTemplate>> = all.groupBy { it.category }
}
