package com.minepapa.kakaonotification.data.local.db.entity

import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.model.NotificationRoute
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteEntityTest {
    @Test
    fun ruleRouteDefaultsToInvestingAndRoundTrips() {
        assertEquals(NotificationRoute.INVESTING, FilterRule().route)

        val rule = FilterRule(senderName = "가상 발신자", route = NotificationRoute.SPENDING)
        assertEquals(rule, FilterRuleEntity.fromDomain(rule).toDomain())
    }

    @Test
    fun spendingLogMetadataRoundTrips() {
        val log = NotificationLog(
            sender = "가상 알림",
            body = "가상 본문",
            matchedKeywords = emptyList(),
            receivedAt = 1234L,
            route = NotificationRoute.SPENDING,
            source = "app",
            packageName = "example.synthetic.app",
            appLabel = "가상 앱",
            postedAt = 1200L,
        )
        assertEquals(log, NotificationLogEntity.fromDomain(log).toDomain())
    }
}
