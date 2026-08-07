package com.habitpulse.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.habitpulse.app.data.local.dao.BadgeDao
import com.habitpulse.app.data.local.dao.CategoryDao
import com.habitpulse.app.data.local.dao.CustomTemplateDao
import com.habitpulse.app.data.local.dao.HabitDao
import com.habitpulse.app.data.local.dao.HabitLogDao
import com.habitpulse.app.data.local.entity.BadgeEntity
import com.habitpulse.app.data.local.entity.CategoryEntity
import com.habitpulse.app.data.local.entity.CustomTemplateEntity
import com.habitpulse.app.data.local.entity.DefaultBadges
import com.habitpulse.app.data.local.entity.HabitEntity
import com.habitpulse.app.data.local.entity.HabitLogEntity
import com.habitpulse.app.data.local.entity.UnlockedBadgeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        HabitEntity::class, HabitLogEntity::class, BadgeEntity::class, UnlockedBadgeEntity::class,
        CategoryEntity::class, CustomTemplateEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun badgeDao(): BadgeDao
    abstract fun categoryDao(): CategoryDao
    abstract fun customTemplateDao(): CustomTemplateDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habitpulse.db"
                )
                    // Η εφαρμογή δεν έχει δημοσιευτεί ακόμα· destructive migration είναι αποδεκτό
                    // εδώ και θα αντικατασταθεί με πραγματικά Migration objects πριν το release.
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default badge catalogue on first run
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
