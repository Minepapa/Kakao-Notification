package com.minepapa.kakaonotification.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minepapa.kakaonotification.domain.model.NotificationLog

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val matchedKeyword: String?,
    val receivedAt: Long,
    val syncedAt: Long?,
) {
    fun toDomain() = NotificationLog(
        id             = id,
        sender         = sender,
        body           = body,
        matchedKeyword = matchedKeyword,
        receivedAt     = receivedAt,
        syncedAt       = syncedAt,
    )

    companion object {
        fun fromDomain(log: NotificationLog) = NotificationLogEntity(
            id             = log.id,
            sender         = log.sender,
            body           = log.body,
            matchedKeyword = log.matchedKeyword,
            receivedAt     = log.receivedAt,
            syncedAt       = log.syncedAt,
        )
    }
}
