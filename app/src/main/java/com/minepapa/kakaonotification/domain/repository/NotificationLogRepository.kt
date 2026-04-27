package com.minepapa.kakaonotification.domain.repository

import com.minepapa.kakaonotification.domain.model.NotificationLog
import kotlinx.coroutines.flow.Flow

interface NotificationLogRepository {
    fun getAll(): Flow<List<NotificationLog>>
    fun getUnsynced(): Flow<List<NotificationLog>>
    suspend fun insert(log: NotificationLog): Long
    suspend fun markSynced(ids: List<Long>, syncedAt: Long)
}
