package com.minepapa.kakaonotification.data.repository

import com.minepapa.kakaonotification.data.local.db.dao.FilterRuleDao
import com.minepapa.kakaonotification.data.local.db.entity.FilterRuleEntity
import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.repository.FilterRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FilterRuleRepositoryImpl @Inject constructor(
    private val dao: FilterRuleDao,
) : FilterRuleRepository {

    override fun getAll(): Flow<List<FilterRule>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): FilterRule? =
        dao.getById(id)?.toDomain()

    override suspend fun insert(rule: FilterRule): Long =
        dao.insert(FilterRuleEntity.fromDomain(rule))

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun setEnabled(id: Long, enabled: Boolean) =
        dao.setEnabled(id, enabled)
}
