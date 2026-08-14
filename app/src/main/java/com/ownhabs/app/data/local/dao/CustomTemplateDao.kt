package com.ownhabs.app.data.local.dao

import androidx.room.*
import com.ownhabs.app.data.local.entity.CustomTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomTemplateDao {

    @Query("SELECT * FROM custom_templates ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<CustomTemplateEntity>>

    @Query("SELECT * FROM custom_templates WHERE categoryId = :categoryId ORDER BY createdAt ASC")
    fun observeForCategory(categoryId: String): Flow<List<CustomTemplateEntity>>

    @Query("SELECT * FROM custom_templates WHERE id = :id")
    suspend fun getById(id: String): CustomTemplateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(template: CustomTemplateEntity)

    @Query("DELETE FROM custom_templates WHERE id = :id")
    suspend fun deleteById(id: String)
}
