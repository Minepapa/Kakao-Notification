package com.minepapa.kakaonotification.domain.repository

import com.minepapa.kakaonotification.domain.model.FilterRule
import kotlinx.coroutines.flow.Flow

interface FilterRuleRepository {
    fun getAll(): Flow<List<FilterRule>>
    suspend fun getById(id: Long): FilterRule?
    suspend fun insert(rule: FilterRule): Long
    suspend fun delete(id: Long)
    suspend fun setEnabled(id: Long, enabled: Boolean)
}
