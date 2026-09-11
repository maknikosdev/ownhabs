package com.ownhabs.app.ui.strings

/**
 * Οι τίτλοι των badges (Overachiever, Consistent Flame x7, κ.λπ.) είναι ήδη ονόματα
 * τύπου "brand" σε λατινικό αλφάβητο και παραμένουν ίδιοι και στις δύο γλώσσες — όπως
 * ακριβώς συμβαίνει και σε πολλές αγγλόφωνες εφαρμογές με achievement names. Μόνο οι
 * περιγραφές χρειάζονται μετάφραση, οπότε το BadgeEntity.description που έρχεται από τη
 * βάση αγνοείται στο UI· η οθόνη Badges παίρνει την περιγραφή από εδώ βάσει badge.id.
 */
object BadgeStrings {
    fun description(id: String, lang: Lang): String = when (id) {
        "overachiever" -> if (lang == Lang.EL) "Κατέγραψες τιμή μεγαλύτερη από τον στόχο σου."
        else "You logged a value higher than your target."

        "flame_7" -> if (lang == Lang.EL) "Σερί 7 συνεχόμενων ημερών."
        else "A streak of 7 consecutive days."

        "flame_30" -> if (lang == Lang.EL) "Σερί 30 συνεχόμενων ημερών."
        else "A streak of 30 consecutive days."

        "flame_90" -> if (lang == Lang.EL) "Σερί 90 συνεχόμενων ημερών."
        else "A streak of 90 consecutive days."

        "flame_365" -> if (lang == Lang.EL) "Σερί 365 συνεχόμενων ημερών."
        else "A streak of 365 consecutive days."

        "record_breaker" -> if (lang == Lang.EL) "Ξεπέρασες το προσωπικό σου ρεκόρ σερί."
        else "You beat your personal streak record."

        "centurion" -> if (lang == Lang.EL) "100 συνολικές ολοκληρώσεις σε μια συνήθεια."
        else "100 total completions on a single habit."

        else -> ""
    }
}
