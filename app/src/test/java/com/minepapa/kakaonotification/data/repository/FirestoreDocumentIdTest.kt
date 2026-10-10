package com.minepapa.kakaonotification.data.repository

import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.model.NotificationRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class FirestoreDocumentIdTest {
    private val log = NotificationLog(
        sender = "가상 발신자",
        body = "가상 본문",
        matchedKeywords = emptyList(),
        receivedAt = 1700000001000L,
        route = NotificationRoute.SPENDING,
        source = "app",
        packageName = "example.synthetic.app",
        appLabel = "가상 앱",
        postedAt = 1700000000000L,
    )

    @Test
    fun idUsesSha256OfLengthPrefixedIdentityFields() {
        assertEquals("0c0cd1841cae75f09d643d353f9bc4f6", documentId(log))
    }

    @Test
    fun retryAndNonIdentityMetadataKeepTheSameId() {
        assertEquals(documentId(log), documentId(log.copy(
            id = 42,
            syncedAt = 1700000002000L,
            receivedAt = 1700000003000L,
            appLabel = "변경된 앱 이름",
            matchedKeywords = listOf("가상"),
        )))
    }

    @Test
    fun changingAnyIdentityFieldChangesTheId() {
        val variants = listOf(
            log.copy(route = NotificationRoute.INVESTING),
            log.copy(source = "kakao"),
            log.copy(packageName = "example.other.app"),
            log.copy(sender = "다른 발신자"),
            log.copy(body = "다른 본문"),
            log.copy(postedAt = log.postedAt + 1),
        )
        variants.forEach { assertNotEquals(documentId(log), documentId(it)) }
    }

    @Test
    fun fieldBoundariesDoNotCollide() {
        assertNotEquals(
            documentId(log.copy(sender = "가", body = "상 발신자가상 본문")),
            documentId(log.copy(sender = "가상", body = " 발신자가상 본문")),
        )
    }
}
