package com.ownhabs.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ownhabs.app.data.local.dao.BadgeDao
import com.ownhabs.app.data.local.dao.CategoryDao
import com.ownhabs.app.data.local.dao.CustomTemplateDao
import com.ownhabs.app.data.local.dao.HabitDao
import com.ownhabs.app.data.local.dao.HabitLogDao
import com.ownhabs.app.data.local.dao.StreakFreezeDao
import com.ownhabs.app.data.local.entity.BadgeEntity
import com.ownhabs.app.data.local.entity.CategoryEntity
import com.ownhabs.app.data.local.entity.CustomTemplateEntity
import com.ownhabs.app.data.local.entity.DefaultBadges
import com.ownhabs.app.data.local.entity.HabitEntity
import com.ownhabs.app.data.local.entity.HabitLogEntity
import com.ownhabs.app.data.local.entity.StreakFreezeEntity
import com.ownhabs.app.data.local.entity.UnlockedBadgeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HabitEntity::class, HabitLogEntity::class, BadgeEntity::class, UnlockedBadgeEntity::class,
        CategoryEntity::class, CustomTemplateEntity::class, StreakFreezeEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun badgeDao(): BadgeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun customTemplateDao(): CustomTemplateDao
    abstract fun streakFreezeDao(): StreakFreezeDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ownhabs.db"
                )
                    // Δεν έχει κυκλοφορήσει ακόμα δημόσια έκδοση· destructive migration
                    // είναι αποδεκτό μέχρι το πρώτο production release, οπότε θα
                    // αντικατασταθεί με πραγματικά Migration objects.
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                INSTANCE?.badgeDao()?.insertBadges(DefaultBadges.seed())
                            }
                        }
                    }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
