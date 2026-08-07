# Add project specific ProGuard rules here.

# ---- Room (SQLite ORM) ----
# Τα entities/DAOs χρησιμοποιούν annotation processing· διατήρησε ό,τι σχετίζεται
# με @Entity/@Dao ώστε το R8 να μην τα κόψει ή να τα μετονομάσει.
-keep class com.habitpulse.app.data.local.entity.** { *; }
-keep class com.habitpulse.app.data.local.dao.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }

# ---- Kotlinx Serialization ----
# Χρησιμοποιείται στο JSON export/import (BackupModels.kt). Το serializer()
# παράγεται μέσω reflection/compiler plugin και πρέπει να επιβιώσει το obfuscation.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.habitpulse.app.data.backup.**$$serializer { *; }
-keepclassmembers class com.habitpulse.app.data.backup.** {
    *** Companion;
}
-keepclasseswithmembers class com.habitpulse.app.data.backup.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Kotlin enums used as Room TypeConverters ----
-keepclassmembers enum com.habitpulse.app.data.local.entity.** { *; }

# ---- Jetpack Compose / Navigation ----
# Τυπικά δεν χρειάζονται επιπλέον κανόνες με τα σύγχρονα Compose/Navigation, αλλά
# διατηρούμε τα route/argument data classes σε περίπτωση reflection-based lookups.
-keepclassmembers class com.habitpulse.app.ui.navigation.** { *; }

# ---- Γενικά ----
-keepattributes Signature, Exceptions, *Annotation*, SourceFile, LineNumberTable
