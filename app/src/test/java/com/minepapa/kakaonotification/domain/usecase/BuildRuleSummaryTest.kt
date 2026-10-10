package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.model.NotificationRoute
import com.minepapa.kakaonotification.domain.model.SelectedSpendingApp
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildRuleSummaryTest {
    @Test
    fun countsActiveRulesByRouteAndSeparatesInactiveRules() {
        val summary = buildRuleSummary(
            listOf(
                FilterRule(senderName = "은행", route = NotificationRoute.INVESTING),
                FilterRule(senderName = "카드", route = NotificationRoute.SPENDING),
                FilterRule(senderName = "꺼진 투자", isEnabled = false),
                FilterRule(senderName = "꺼진 소비", isEnabled = false, route = NotificationRoute.SPENDING),
            )
        )

        assertEquals(1, summary.investing.activeCount)
        assertEquals(1, summary.spending.activeCount)
        assertEquals(2, summary.inactiveCount)
        assertEquals(listOf("은행"), summary.investing.lines)
        assertEquals(listOf("카드"), summary.spending.lines)
    }

    @Test
    fun showsFiveLinesPerRouteAndCountsTheRest() {
        val rules = (1..7).map { FilterRule(senderName = "투자$it") } +
            (1..6).map { FilterRule(senderName = "소비$it", route = NotificationRoute.SPENDING) }

        val summary = buildRuleSummary(rules)

        assertEquals(7, summary.investing.activeCount)
        assertEquals(5, summary.investing.lines.size)
        assertEquals(2, summary.investing.hiddenCount)
        assertEquals("외 2개", hiddenRulesLine(summary.investing.hiddenCount))
        assertEquals(6, summary.spending.activeCount)
        assertEquals(5, summary.spending.lines.size)
        assertEquals(1, summary.spending.hiddenCount)
        assertEquals("외 1개", hiddenRulesLine(summary.spending.hiddenCount))
    }

    @Test
    fun summarizesSenderAndFirstThreeKeywords() {
        val line = ruleSummaryLine(
            FilterRule(keywords = listOf("식비", "교통", "쇼핑", "여행"))
        )

        assertEquals("전체 발신자 · 식비, 교통, 쇼핑", line)
    }

    @Test
    fun summarizesNoSelectedApps() {
        assertEquals("소비 수집 앱 0개", selectedSpendingAppsLine(emptyList()))
    }

    @Test
    fun summarizesThreeSelectedApps() {
        val apps = (1..3).map { SelectedSpendingApp("package$it", "앱$it") }
        assertEquals("소비 수집 앱 3개 · 앱1, 앱2, 앱3", selectedSpendingAppsLine(apps))
    }

    @Test
    fun summarizesSelectedAppOverflow() {
        val apps = (1..5).map { SelectedSpendingApp("package$it", "앱$it") }
        assertEquals("소비 수집 앱 5개 · 앱1, 앱2, 앱3 외 2개", selectedSpendingAppsLine(apps))
    }
}
