package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.model.NotificationRoute
import com.minepapa.kakaonotification.domain.model.SelectedSpendingApp

data class RouteRuleSummary(
    val activeCount: Int,
    val lines: List<String>,
    val hiddenCount: Int,
)

data class RuleSummary(
    val investing: RouteRuleSummary,
    val spending: RouteRuleSummary,
    val inactiveCount: Int,
)

fun buildRuleSummary(rules: List<FilterRule>, maxVisible: Int = 5): RuleSummary {
    require(maxVisible >= 0)
    val activeRules = rules.filter { it.isEnabled }
    fun summarize(routeRules: List<FilterRule>): RouteRuleSummary = RouteRuleSummary(
        activeCount = routeRules.size,
        lines = routeRules.take(maxVisible).map(::ruleSummaryLine),
        hiddenCount = (routeRules.size - maxVisible).coerceAtLeast(0),
    )
    return RuleSummary(
        investing = summarize(activeRules.filter { it.route != NotificationRoute.SPENDING }),
        spending = summarize(activeRules.filter { it.route == NotificationRoute.SPENDING }),
        inactiveCount = rules.size - activeRules.size,
    )
}

fun ruleSummaryLine(rule: FilterRule): String {
    val sender = rule.senderName.ifBlank { "전체 발신자" }
    val keywords = rule.keywords.take(3)
    return if (keywords.isEmpty()) sender else "$sender · ${keywords.joinToString(", ")}"
}

fun hiddenRulesLine(count: Int): String = "외 ${count}개"

fun selectedSpendingAppsLine(apps: List<SelectedSpendingApp>): String = buildString {
    append("소비 수집 앱 ${apps.size}개")
    if (apps.isNotEmpty()) {
        append(" · ")
        append(apps.take(3).joinToString(", ") { it.label })
    }
    if (apps.size > 3) append(" 외 ${apps.size - 3}개")
}
