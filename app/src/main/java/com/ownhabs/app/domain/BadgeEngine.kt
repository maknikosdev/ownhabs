package com.ownhabs.app.domain

import com.ownhabs.app.data.local.dao.BadgeDao
import com.ownhabs.app.data.local.dao.HabitLogDao
import com.ownhabs.app.data.local.entity.BadgeCriterion
import com.ownhabs.app.data.local.entity.BadgeEntity
import com.ownhabs.app.data.local.entity.HabitEntity
import com.ownhabs.app.data.local.entity.UnlockedBadgeEntity

/**
 * Re-evaluates badge criteria for a habit right after a new log is saved.
 * Returns the list of newly unlocked badges so the UI can show a celebration dialog.
 */
class BadgeEngine(
    private val badgeDao: BadgeDao,
    private val habitLogDao: HabitLogDao
) {

    suspend fun evaluate(habit: HabitEntity, justLoggedValue: Double): List<BadgeEntity> {
        val allLogs = habitLogDao.getAllForHabit(habit.id)
        val streak = StreakCalculator.calculate(allLogs)
        val totalLogs = allLogs.size
        val catalogue = badgeDao.getAllBadges()
        val newlyUnlocked = mutableListOf<BadgeEntity>()

        for (badge in catalogue) {
            val alreadyUnlocked = badgeDao.findUnlocked(habit.id, badge.id) != null
            if (alreadyUnlocked) continue

            val qualifies = when (badge.criterion) {
                BadgeCriterion.GOAL_EXCEEDED ->
                    habit.goalType.name == "NUMERIC" && justLoggedValue > habit.targetValue

                BadgeCriterion.STREAK ->
                    if (badge.targetValue >= 0) {
                        streak.current >= badge.targetValue.toInt()
                    } else {
                        // Record Breaker: current streak beats previous best (i.e. current == best and best > 1 previous run)
                        streak.current > 0 && streak.current == streak.best && totalLogs > streak.best
                    }

                BadgeCriterion.TOTAL_LOGS ->
                    totalLogs >= badge.targetValue.toInt()
            }

            if (qualifies) {
                badgeDao.insertUnlocked(
                    UnlockedBadgeEntity(badgeId = badge.id, habitId = habit.id)
                )
                newlyUnlocked.add(badge)
            }
        }

        return newlyUnlocked
    }
}
