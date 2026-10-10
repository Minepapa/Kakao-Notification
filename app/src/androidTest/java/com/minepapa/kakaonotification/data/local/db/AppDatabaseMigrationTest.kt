package com.minepapa.kakaonotification.data.local.db

import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @Test
    fun migrationFrom3PreservesRulesAndLogs() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "migration-test.db"
        context.deleteDatabase(name)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(name)
                .callback(object : SupportSQLiteOpenHelper.Callback(3) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE `filter_rules` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `senderName` TEXT NOT NULL, `keywords` TEXT NOT NULL, `isEnabled` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
                        db.execSQL("CREATE TABLE `notification_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sender` TEXT NOT NULL, `body` TEXT NOT NULL, `matchedKeywords` TEXT NOT NULL, `receivedAt` INTEGER NOT NULL, `syncedAt` INTEGER)")
                    }

                    override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build()
        )
        try {
            val db = helper.writableDatabase
            db.execSQL("INSERT INTO `filter_rules` (`id`, `senderName`, `keywords`, `isEnabled`, `createdAt`) VALUES (7, '가상 발신자', '가상 키워드', 1, 100)")
            db.execSQL("INSERT INTO `notification_logs` (`id`, `sender`, `body`, `matchedKeywords`, `receivedAt`) VALUES (8, '가상 제목', '가상 본문', '', 200)")

            AppDatabase.MIGRATION_3_4.migrate(db)

            db.query("SELECT `id`, `senderName`, `route` FROM `filter_rules`").use { cursor ->
                cursor.moveToFirst()
                assertEquals(7L, cursor.getLong(0))
                assertEquals("가상 발신자", cursor.getString(1))
                assertEquals("investing", cursor.getString(2))
            }
            db.query("SELECT `id`, `route`, `postedAt` FROM `notification_logs`").use { cursor ->
                cursor.moveToFirst()
                assertEquals(8L, cursor.getLong(0))
                assertEquals("investing", cursor.getString(1))
                assertEquals(200L, cursor.getLong(2))
            }
        } finally {
            helper.close()
            context.deleteDatabase(name)
        }
    }
}
