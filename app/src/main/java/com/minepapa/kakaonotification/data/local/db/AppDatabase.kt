package com.minepapa.kakaonotification.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.minepapa.kakaonotification.data.local.db.dao.FilterRuleDao
import com.minepapa.kakaonotification.data.local.db.dao.NotificationLogDao
import com.minepapa.kakaonotification.data.local.db.entity.FilterRuleEntity
import com.minepapa.kakaonotification.data.local.db.entity.NotificationLogEntity

@Database(
    entities = [FilterRuleEntity::class, NotificationLogEntity::class],
    version = 4,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun filterRuleDao(): FilterRuleDao
    abstract fun notificationLogDao(): NotificationLogDao

    companion object {
        const val NAME = "kakao_notification_db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `notification_logs_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sender` TEXT NOT NULL, `body` TEXT NOT NULL, `matchedKeyword` TEXT, `receivedAt` INTEGER NOT NULL, `syncedAt` INTEGER)")
                db.execSQL("INSERT INTO `notification_logs_new` (`id`, `sender`, `body`, `matchedKeyword`, `receivedAt`, `syncedAt`) SELECT `id`, `sender`, `body`, NULL, `receivedAt`, `syncedAt` FROM `notification_logs`")
                db.execSQL("DROP TABLE `notification_logs`")
                db.execSQL("ALTER TABLE `notification_logs_new` RENAME TO `notification_logs`")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `notification_logs_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sender` TEXT NOT NULL, `body` TEXT NOT NULL, `matchedKeywords` TEXT NOT NULL, `receivedAt` INTEGER NOT NULL, `syncedAt` INTEGER)")
                db.execSQL("INSERT INTO `notification_logs_new` (`id`, `sender`, `body`, `matchedKeywords`, `receivedAt`, `syncedAt`) SELECT `id`, `sender`, `body`, COALESCE(`matchedKeyword`, ''), `receivedAt`, `syncedAt` FROM `notification_logs`")
                db.execSQL("DROP TABLE `notification_logs`")
                db.execSQL("ALTER TABLE `notification_logs_new` RENAME TO `notification_logs`")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `filter_rules` ADD COLUMN `route` TEXT NOT NULL DEFAULT 'investing'")
                db.execSQL("ALTER TABLE `notification_logs` ADD COLUMN `route` TEXT NOT NULL DEFAULT 'investing'")
                db.execSQL("ALTER TABLE `notification_logs` ADD COLUMN `source` TEXT NOT NULL DEFAULT 'kakao'")
                db.execSQL("ALTER TABLE `notification_logs` ADD COLUMN `packageName` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `notification_logs` ADD COLUMN `appLabel` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `notification_logs` ADD COLUMN `postedAt` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE `notification_logs` SET `postedAt` = `receivedAt`")
            }
        }
    }
}
