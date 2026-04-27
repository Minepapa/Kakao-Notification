package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.repository.FilterRuleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class MatchNotificationUseCase @Inject constructor(
    private val repo: FilterRuleRepository,
) {
    suspend operator fun invoke(sender: String, body: String): FilterRule? {
        val rules = repo.getAll().first().filter { it.isEnabled }
        return rules.firstOrNull { rule ->
            val senderMatch = rule.senderName.isBlank() ||
                sender.contains(rule.senderName, ignoreCase = true)
            val keywordMatch = rule.keywords.isEmpty() ||
                rule.keywords.any { kw -> body.contains(kw, ignoreCase = true) }
            senderMatch && keywordMatch
        }
    }
}
