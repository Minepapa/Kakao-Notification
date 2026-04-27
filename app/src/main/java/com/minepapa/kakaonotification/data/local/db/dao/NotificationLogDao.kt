package com.minepapa.kakaonotification.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.minepapa.kakaonotification.data.local.db.entity.NotificationLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationLogDao {

    @Query("SELECT * FROM notification_logs ORDER BY receivedAt DESC")
    fun getAll(): Flow<List<NotificationLogEntity>>

    @Query("SELECT * FROM notification_logs WHERE syncedAt IS NULL ORDER BY receivedAt ASC")
    fun getUnsynced(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NotificationLogEntity): Long

    @Query("UPDATE notification_logs SET syncedAt = :syncedAt WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>, syncedAt: Long)
}
