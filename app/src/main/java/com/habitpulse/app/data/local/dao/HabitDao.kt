package com.habitpulse.app.data.local.dao

import androidx.room.*
import com.habitpulse.app.data.local.entity.HabitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE status = 'ACTIVE' ORDER BY createdAt ASC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE status = 'ARCHIVED' ORDER BY createdAt ASC")
    fun observeArchived(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: String): HabitEntity?

    @Query("SELECT * FROM habits")
    suspend fun getAllOnce(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(habits: List<HabitEntity>)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE habits SET status = :status WHERE id = :id")
    suspend fun setStatus(id: String, status: String)

    @Query("DELETE FROM habits")
    suspend fun clearAll()
}
