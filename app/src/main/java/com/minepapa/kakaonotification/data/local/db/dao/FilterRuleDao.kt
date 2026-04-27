package com.minepapa.kakaonotification.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minepapa.kakaonotification.data.local.db.entity.FilterRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FilterRuleDao {

    @Query("SELECT * FROM filter_rules ORDER BY createdAt DESC")
    fun getAll(): Flow<List<FilterRuleEntity>>

    @Query("SELECT * FROM filter_rules WHERE id = :id")
    suspend fun getById(id: Long): FilterRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FilterRuleEntity): Long

    @Query("DELETE FROM filter_rules WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE filter_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
