package com.ownhabs.app.data.repository

import com.ownhabs.app.data.local.dao.BadgeDao
import com.ownhabs.app.data.local.dao.HabitDao
import com.ownhabs.app.data.local.dao.HabitLogDao
import com.ownhabs.app.data.local.entity.BadgeEntity
import com.ownhabs.app.data.local.entity.HabitEntity
import com.ownhabs.app.data.local.entity.HabitLogEntity
import com.ownhabs.app.data.local.entity.HabitStatus
import com.ownhabs.app.data.local.entity.UnlockedBadgeEntity
import com.ownhabs.app.domain.BadgeEngine
import com.ownhabs.app.domain.StreakCalculator
import com.ownhabs.app.domain.StreakResult
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao,
    private val badgeDao: BadgeDao,
    private val streakFreezeDao: com.ownhabs.app.data.local.dao.StreakFreezeDao
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

    /**
     * Ορίζει ΡΗΤΑ τη σημερινή τιμή μιας συνήθειας (Έγινε πλήρως / Εν μέρει / Δεν έγινε),
     * αντικαθιστώντας οποιαδήποτε προηγούμενη καταγραφή της ίδιας ημέρας αντί να προσθέτει
     * πάνω της. Value = 0.0 σημαίνει "Δεν έγινε" και απλά διαγράφει τη σημερινή καταγραφή.
     */
    suspend fun setTodayValue(habit: HabitEntity, value: Double, notes: String = ""): List<BadgeEntity> {
        val zone = java.time.ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        val dayStart = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        habitLogDao.deleteLogsInRange(habit.id, dayStart, dayEnd)
        if (value <= 0.0) return emptyList()
        habitLogDao.upsert(HabitLogEntity(habitId = habit.id, timestamp = System.currentTimeMillis(), value = value, notes = notes))
        return badgeEngine.evaluate(habit, value)
    }

    suspend fun updateLog(log: HabitLogEntity) = habitLogDao.upsert(log)
    suspend fun deleteLog(log: HabitLogEntity) = habitLogDao.deleteById(log.id)

    suspend fun getStreak(habitId: String): StreakResult {
        val logs = habitLogDao.getAllForHabit(habitId)
        val frozen = streakFreezeDao.getForHabit(habitId)
            .map { java.time.LocalDate.ofEpochDay(it.dateEpochDay) }
            .toSet()
        return StreakCalculator.calculate(logs, frozen)
    }

    // --- Streak Freeze ("κάρτα χάρης" που δεν σπάει το σερί) ---
    fun observeFreezes(habitId: String) = streakFreezeDao.observeForHabit(habitId)

    suspend fun getFrozenDatesOnce(habitId: String): Set<java.time.LocalDate> =
        streakFreezeDao.getForHabit(habitId).map { java.time.LocalDate.ofEpochDay(it.dateEpochDay) }.toSet()

    suspend fun freezesUsedThisMonth(habitId: String, referenceDate: java.time.LocalDate = java.time.LocalDate.now()): Int {
        val start = referenceDate.withDayOfMonth(1).toEpochDay()
        val end = referenceDate.withDayOfMonth(referenceDate.lengthOfMonth()).toEpochDay()
        return streakFreezeDao.countInMonth(habitId, start, end)
    }

    sealed class FreezeResult {
        object Applied : FreezeResult()
        object AlreadyLogged : FreezeResult()
        object AlreadyFrozen : FreezeResult()
        object MonthlyLimitReached : FreezeResult()
    }

    /** Εφαρμόζει streak freeze σε μια ημέρα, αν δεν έχει ήδη καταγραφή και δεν έχει ξεπεραστεί το μηνιαίο όριο. */
    suspend fun applyStreakFreeze(habitId: String, date: java.time.LocalDate): FreezeResult {
        val zone = java.time.ZoneId.systemDefault()
        val dayStart = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        if (habitLogDao.getLogsForDay(habitId, dayStart, dayEnd).isNotEmpty()) {
            return FreezeResult.AlreadyLogged
        }
        if (streakFreezeDao.findForDate(habitId, date.toEpochDay()) != null) {
            return FreezeResult.AlreadyFrozen
        }
        if (freezesUsedThisMonth(habitId, date) >= com.ownhabs.app.data.local.entity.StreakFreezeEntity.FREEZES_PER_MONTH) {
            return FreezeResult.MonthlyLimitReached
        }
        streakFreezeDao.insert(
            com.ownhabs.app.data.local.entity.StreakFreezeEntity(habitId = habitId, dateEpochDay = date.toEpochDay())
        )
        return FreezeResult.Applied
    }

    suspend fun removeStreakFreeze(id: String) = streakFreezeDao.deleteById(id)

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
