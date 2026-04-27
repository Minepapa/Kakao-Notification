package com.minepapa.kakaonotification.data.repository

import com.minepapa.kakaonotification.data.local.db.dao.NotificationLogDao
import com.minepapa.kakaonotification.data.local.db.entity.NotificationLogEntity
import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationLogRepositoryImpl @Inject constructor(
    private val dao: NotificationLogDao,
) : NotificationLogRepository {

    override fun getAll(): Flow<List<NotificationLog>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override fun getUnsynced(): Flow<List<NotificationLog>> =
        dao.getUnsynced().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(log: NotificationLog): Long =
        dao.insert(NotificationLogEntity.fromDomain(log))

    override suspend fun markSynced(ids: List<Long>, syncedAt: Long) =
        dao.markSynced(ids, syncedAt)
}
