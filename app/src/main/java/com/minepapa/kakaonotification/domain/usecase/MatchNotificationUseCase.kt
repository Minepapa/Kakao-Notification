package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.repository.FilterRuleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class MatchResult(
    val rule: FilterRule,
    val matchedKeywords: List<String>,
)

class MatchNotificationUseCase @Inject constructor(
    private val repo: FilterRuleRepository,
) {
    suspend operator fun invoke(sender: String, body: String): MatchResult? {
        val rules = repo.getAll().first().filter { it.isEnabled }
        for (rule in rules) {
            val senderMatch = rule.senderName.isBlank() ||
                sender.contains(rule.senderName, ignoreCase = true)
            if (!senderMatch) continue

            if (rule.keywords.isEmpty()) {
                return MatchResult(rule, matchedKeywords = emptyList())
            }
            val matched = rule.keywords.filter { kw ->
                body.contains(kw, ignoreCase = true)
            }
            if (matched.isNotEmpty()) {
                return MatchResult(rule, matchedKeywords = matched)
            }
        }
        return null
    }
}
