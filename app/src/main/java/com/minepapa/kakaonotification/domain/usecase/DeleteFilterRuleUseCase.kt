package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.repository.FilterRuleRepository
import javax.inject.Inject

class DeleteFilterRuleUseCase @Inject constructor(
    private val repo: FilterRuleRepository,
) {
    suspend operator fun invoke(id: Long): Result<Unit> = runCatching {
        repo.delete(id)
    }
}
