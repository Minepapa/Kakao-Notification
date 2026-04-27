package com.minepapa.kakaonotification.core.di

import android.content.Context
import androidx.room.Room
import com.minepapa.kakaonotification.data.local.db.AppDatabase
import com.minepapa.kakaonotification.data.local.db.dao.FilterRuleDao
import com.minepapa.kakaonotification.data.local.db.dao.NotificationLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFilterRuleDao(db: AppDatabase): FilterRuleDao = db.filterRuleDao()

    @Provides
    fun provideNotificationLogDao(db: AppDatabase): NotificationLogDao = db.notificationLogDao()
}
