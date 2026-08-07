package com.habitpulse.app.data.repository

import com.habitpulse.app.data.local.dao.BadgeDao
import com.habitpulse.app.data.local.dao.HabitDao
import com.habitpulse.app.data.local.dao.HabitLogDao
import com.habitpulse.app.data.local.entity.BadgeEntity
import com.habitpulse.app.data.local.entity.HabitEntity
import com.habitpulse.app.data.local.entity.HabitLogEntity
import com.habitpulse.app.data.local.entity.HabitStatus
import com.habitpulse.app.data.local.entity.UnlockedBadgeEntity
import com.habitpulse.app.domain.BadgeEngine
import com.habitpulse.app.domain.StreakCalculator
import com.habitpulse.app.domain.StreakResult
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val badgeDao: BadgeDao
) {
    private val badgeEngine = BadgeEngine(badgeDao, habitLogDao)

    // --- Habits (CRUD) ---
    fun observeActiveHabits(): Flow<List<HabitEntity>> = habitDao.observeActive()
    fun observeArchivedHabits(): Flow<List<HabitEntity>> = habitDao.observeArchived()
    fun observeAllHabits(): Flow<List<HabitEntity>> = habitDao.observeAll()

    suspend fun getHabit(id: String): HabitEntity? = habitDao.getById(id)

    suspend fun saveHabit(habit: HabitEntity) = habitDao.upsert(habit)

    suspend fun archiveHabit(id: String) = habitDao.setStatus(id, HabitStatus.ARCHIVED.name)
    suspend fun unarchiveHabit(id: String) = habitDao.setStatus(id, HabitStatus.ACTIVE.name)

    suspend fun deleteHabitPermanently(id: String) = habitDao.deleteById(id) // cascades to logs

    // --- Logs (Create / Update / Delete history) ---
    fun observeLogs(habitId: String): Flow<List<HabitLogEntity>> = habitLogDao.observeLogsForHabit(habitId)

    suspend fun getLogsForDay(habitId: String, dayStartMillis: Long, dayEndMillis: Long) =
        habitLogDao.getLogsForDay(habitId, dayStartMillis, dayEndMillis)

    /** Ίδιο query με getLogsForDay — απλά πιο σαφές όνομα όταν το εύρος είναι εβδομάδα/μήνας. */
    suspend fun getLogsInRange(habitId: String, startMillis: Long, endMillis: Long) =
        habitLogDao.getLogsForDay(habitId, startMillis, endMillis)

    suspend fun getAllLogs(habitId: String) = habitLogDao.getAllForHabit(habitId)

    /** Logs a completion / value and evaluates badges. Returns newly unlocked badges. */
    suspend fun logHabit(habit: HabitEntity, value: Double, notes: String = "", timestamp: Long = System.currentTimeMillis()): List<BadgeEntity> {
        habitLogDao.upsert(
            HabitLogEntity(habitId = habit.id, timestamp = timestamp, value = value, notes = notes)
        )
        return badgeEngine.evaluate(habit, value)
    }

    suspend fun updateLog(log: HabitLogEntity) = habitLogDao.upsert(log)
    suspend fun deleteLog(log: HabitLogEntity) = habitLogDao.deleteById(log.id)

    suspend fun getStreak(habitId: String): StreakResult {
        val logs = habitLogDao.getAllForHabit(habitId)
        return StreakCalculator.calculate(logs)
    }

    // --- Badges ---
    fun observeBadges(): Flow<List<BadgeEntity>> = badgeDao.observeBadges()
    fun observeUnlockedBadges(): Flow<List<UnlockedBadgeEntity>> = badgeDao.observeUnlocked()

    // --- Backup helpers (used by BackupManager) ---
    suspend fun getAllHabitsOnce() = habitDao.getAllOnce()
    suspend fun getAllLogsOnce() = habitLogDao.getAllOnce()
    suspend fun getAllBadgesOnce() = badgeDao.getAllBadges()
    suspend fun getAllUnlockedOnce() = badgeDao.getAllUnlockedOnce()

    suspend fun replaceAllData(habits: List<HabitEntity>, logs: List<HabitLogEntity>, unlocked: List<UnlockedBadgeEntity>) {
        habitLogDao.clearAll()
        badgeDao.clearUnlocked()
        habitDao.clearAll()
        habitDao.upsertAll(habits)
        habitLogDao.upsertAll(logs)
        badgeDao.insertUnlockedAll(unlocked)
    }

    suspend fun mergeData(habits: List<HabitEntity>, logs: List<HabitLogEntity>, unlocked: List<UnlockedBadgeEntity>) {
        // Merge by id/UUID + timestamp: REPLACE strategy naturally de-duplicates on primary key.
        habitDao.upsertAll(habits)
        habitLogDao.upsertAll(logs)
        badgeDao.insertUnlockedAll(unlocked)
    }
}
