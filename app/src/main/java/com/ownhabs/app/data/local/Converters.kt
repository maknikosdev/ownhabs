package com.ownhabs.app.data.local

import androidx.room.TypeConverter
import com.ownhabs.app.data.local.entity.BadgeCriterion
import com.ownhabs.app.data.local.entity.FrequencyPeriod
import com.ownhabs.app.data.local.entity.GoalType
import com.ownhabs.app.data.local.entity.HabitStatus

class Converters {
    @TypeConverter
    fun fromGoalType(value: GoalType): String = value.name

    @TypeConverter
    fun toGoalType(value: String): GoalType = GoalType.valueOf(value)

    @TypeConverter
    fun fromHabitStatus(value: HabitStatus): String = value.name

    @TypeConverter
    fun toHabitStatus(value: String): HabitStatus = HabitStatus.valueOf(value)

    @TypeConverter
    fun fromBadgeCriterion(value: BadgeCriterion): String = value.name

    @TypeConverter
    fun toBadgeCriterion(value: String): BadgeCriterion = BadgeCriterion.valueOf(value)

    @TypeConverter
    fun fromFrequencyPeriod(value: FrequencyPeriod): String = value.name

    @TypeConverter
    fun toFrequencyPeriod(value: String): FrequencyPeriod = FrequencyPeriod.valueOf(value)
}
