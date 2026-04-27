package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.repository.FilterRuleRepository
import javax.inject.Inject

class AddFilterRuleUseCase @Inject constructor(
    private val repo: FilterRuleRepository,
) {
    suspend operator fun invoke(rule: FilterRule): Result<Long> = runCatching {
        require(rule.senderName.isNotBlank() || rule.keywords.isNotEmpty()) {
            "발신자 또는 키워드 중 하나 이상 입력해야 합니다."
        }
        repo.insert(rule)
    }
}
