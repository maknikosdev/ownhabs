package com.ownhabs.app.ui.strings

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf

private const val PREFS_NAME = "ownhabs_prefs"
private const val KEY_LANGUAGE = "language"

/** Απλό, τοπικό αποθηκευτικό της προτίμησης γλώσσας — καμία σχέση με τη γλώσσα συστήματος. */
object LanguagePreference {
    fun get(context: Context): Lang {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANGUAGE, null)
        return if (saved == "EN") Lang.EN else Lang.EL // Ελληνικά default
    }

    fun set(context: Context, lang: Lang) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, lang.name)
            .apply()
    }
}

val LocalStrings = staticCompositionLocalOf { ElStrings }
val LocalLang = staticCompositionLocalOf { Lang.EL }
