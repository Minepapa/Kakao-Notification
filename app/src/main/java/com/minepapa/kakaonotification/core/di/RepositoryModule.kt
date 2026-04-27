package com.minepapa.kakaonotification.core.di

import com.minepapa.kakaonotification.data.repository.FilterRuleRepositoryImpl
import com.minepapa.kakaonotification.data.repository.NotificationLogRepositoryImpl
import com.minepapa.kakaonotification.data.repository.SheetsRepositoryImpl
import com.minepapa.kakaonotification.domain.repository.FilterRuleRepository
import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import com.minepapa.kakaonotification.domain.repository.SheetsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFilterRuleRepository(impl: FilterRuleRepositoryImpl): FilterRuleRepository

    @Binds
    @Singleton
    abstract fun bindNotificationLogRepository(impl: NotificationLogRepositoryImpl): NotificationLogRepository

    @Binds
    @Singleton
    abstract fun bindSheetsRepository(impl: SheetsRepositoryImpl): SheetsRepository
}
