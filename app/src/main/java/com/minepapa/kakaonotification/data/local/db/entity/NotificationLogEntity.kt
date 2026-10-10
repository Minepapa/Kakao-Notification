package com.minepapa.kakaonotification.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.model.NotificationRoute

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String,
    val body: String,
    val matchedKeywords: String,  // 쉼표 구분 저장, 없으면 빈 문자열
    val receivedAt: Long,
    val syncedAt: Long?,
    @ColumnInfo(defaultValue = "'investing'") val route: String = NotificationRoute.INVESTING,
    @ColumnInfo(defaultValue = "'kakao'") val source: String = "kakao",
    @ColumnInfo(defaultValue = "''") val packageName: String = "",
    @ColumnInfo(defaultValue = "''") val appLabel: String = "",
    @ColumnInfo(defaultValue = "0") val postedAt: Long = receivedAt,
) {
    fun toDomain() = NotificationLog(
        id              = id,
        sender          = sender,
        body            = body,
        matchedKeywords = if (matchedKeywords.isBlank()) emptyList()
                          else matchedKeywords.split(","),
        receivedAt      = receivedAt,
        syncedAt        = syncedAt,
        route           = route,
        source          = source,
        packageName     = packageName,
        appLabel        = appLabel,
        postedAt        = postedAt,
    )

    companion object {
        fun fromDomain(log: NotificationLog) = NotificationLogEntity(
            id              = log.id,
            sender          = log.sender,
            body            = log.body,
            matchedKeywords = log.matchedKeywords.joinToString(","),
            receivedAt      = log.receivedAt,
            syncedAt        = log.syncedAt,
            route           = log.route,
            source          = log.source,
            packageName     = log.packageName,
            appLabel        = log.appLabel,
            postedAt        = log.postedAt,
        )
    }
}
