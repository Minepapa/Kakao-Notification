package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.repository.FilterRuleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFilterRulesUseCase @Inject constructor(
    private val repo: FilterRuleRepository,
) {
    operator fun invoke(): Flow<List<FilterRule>> = repo.getAll()
}
