package com.minepapa.kakaonotification.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.minepapa.kakaonotification.data.local.db.dao.FilterRuleDao
import com.minepapa.kakaonotification.data.local.db.dao.NotificationLogDao
import com.minepapa.kakaonotification.data.local.db.entity.FilterRuleEntity
import com.minepapa.kakaonotification.data.local.db.entity.NotificationLogEntity

@Database(
    entities = [FilterRuleEntity::class, NotificationLogEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun filterRuleDao(): FilterRuleDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        const val NAME = "kakao_notification_db"
    }
}
