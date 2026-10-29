package com.minepapa.kakaonotification.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationCapturePolicyTest {
    private val policy = NotificationCapturePolicy("com.example.chat")

    @Test
    fun `선택한 앱은 규칙 없이 소비 알림으로 수집한다`() {
        val selected = setOf("com.example.card")
        assertEquals(
            NotificationCapturePolicy.Capture.SPENDING_APP,
            policy.route("com.example.card", selected, false, false, false),
        )
        assertTrue(policy.hasContent(NotificationCapturePolicy.Capture.SPENDING_APP, "승인", emptyList()))
        assertTrue(policy.hasContent(NotificationCapturePolicy.Capture.SPENDING_APP, "", listOf("결제")))
        assertFalse(policy.hasContent(NotificationCapturePolicy.Capture.SPENDING_APP, "", emptyList()))
        assertNull(policy.route("com.example.other", selected, false, false, false))
    }

    @Test
    fun `카카오톡은 선택 앱 목록과 무관하게 규칙 매칭 대상으로 분기한다`() {
        assertEquals(
            NotificationCapturePolicy.Capture.KAKAO,
            policy.route("com.example.chat", setOf("com.example.chat"), false, false, false),
        )
        assertFalse(policy.hasContent(NotificationCapturePolicy.Capture.KAKAO, "", listOf("결제")))
    }

    @Test
    fun `그룹 요약은 출처와 무관하게 건너뛴다`() {
        assertNull(policy.route("com.example.chat", emptySet(), true, false, false))
        assertNull(policy.route("com.example.card", setOf("com.example.card"), true, false, false))
    }

    @Test
    fun `선택 앱의 상시 알림과 포그라운드 서비스 알림은 건너뛴다`() {
        val selected = setOf("com.example.card")
        assertNull(policy.route("com.example.card", selected, false, true, false))
        assertNull(policy.route("com.example.card", selected, false, false, true))
        assertEquals(
            NotificationCapturePolicy.Capture.KAKAO,
            policy.route("com.example.chat", selected, false, true, true),
        )
    }

    @Test
    fun `같은 key와 내용의 재게시만 건너뛴다`() {
        assertTrue(policy.accept("key-1", 100L, "제목", listOf("본문")))
        assertFalse(policy.accept("key-1", 100L, "제목", listOf("본문")))
        assertTrue(policy.accept("key-1", 100L, "제목", listOf("수정된 본문")))
        assertTrue(policy.accept("key-2", 100L, "제목", listOf("수정된 본문")))
    }

    @Test
    fun `같은 대화방 key와 문구라도 알림 시각이 다르면 새 메시지로 수집한다`() {
        assertTrue(policy.accept("room-key", 100L, "체결", listOf("동일한 체결 문구")))
        assertTrue(policy.accept("room-key", 101L, "체결", listOf("동일한 체결 문구")))
        assertFalse(policy.accept("room-key", 101L, "체결", listOf("동일한 체결 문구")))
    }

    @Test
    fun `알림 시각이 없으면 게시 시각을 사용한다`() {
        assertEquals(200L, policy.postedAt(0L, 200L))
        assertEquals(200L, policy.postedAt(-1L, 200L))
        assertEquals(100L, policy.postedAt(100L, 200L))
    }

    @Test
    fun `저장되지 않은 알림은 같은 내용으로 다시 시도할 수 있다`() {
        assertTrue(policy.accept("key-1", 100L, "제목", listOf("본문")))
        policy.forget("key-1", 100L, "제목", listOf("본문"))
        assertTrue(policy.accept("key-1", 100L, "제목", listOf("본문")))
    }

    private fun fill(round: Int) = NotificationCapturePolicy.Message(
        "[삼성증권]<주식체결안내>\nTIGER 미국배당다우존스\n매도1주 14,825원\n체결(${round}차)",
        1_793_076_027_320L + round * 605L,
    )

    // 2026-10-27 13:40 실사고: 몰린 갱신 하나에 12·6·13·4차가 함께 실렸고 BigText에는 4차만 있었다.
    @Test
    fun `몰린 갱신에 실린 새 메시지는 각각 한 번씩 수집한다`() {
        val earlier = listOf(fill(11), fill(12))
        earlier.forEach { assertTrue(policy.acceptMessage("room", "삼성증권", it)) }

        val crowded = earlier + listOf(fill(13), fill(14), fill(15))
        val accepted = policy.recentMessages(crowded).filter { policy.acceptMessage("room", "삼성증권", it) }
        assertEquals(listOf(fill(13), fill(14), fill(15)), accepted)
        assertTrue(policy.recentMessages(crowded).none { policy.acceptMessage("room", "삼성증권", it) })
    }

    @Test
    fun `가장 새 메시지보다 오래된 이력과 빈 메시지는 다시 보내지 않는다`() {
        val latest = NotificationCapturePolicy.Message("새 체결", 1_000_000L)
        val old = NotificationCapturePolicy.Message("옛 체결", latest.timeMs - NotificationCapturePolicy.MESSAGE_WINDOW_MS - 1)
        val blank = NotificationCapturePolicy.Message(" ", latest.timeMs)
        assertEquals(listOf(latest), policy.recentMessages(listOf(old, blank, latest)))
    }

    @Test
    fun `시각 없는 메시지가 섞이면 예전 경로, 같은 ms의 다른 메시지는 따로 수집`() {
        assertFalse(policy.usesMessages(listOf(fill(1), NotificationCapturePolicy.Message("시각 없음", 0L))))
        assertFalse(policy.usesMessages(emptyList()))
        assertTrue(policy.usesMessages(listOf(fill(1))))
        val a = NotificationCapturePolicy.Message("체결 A", 5_000L)
        val b = NotificationCapturePolicy.Message("체결 B", 5_000L)
        assertTrue(policy.acceptMessage("room", "삼성증권", a))
        assertTrue(policy.acceptMessage("room", "삼성증권", b))
        assertFalse(policy.acceptMessage("room", "삼성증권", a))
    }

    @Test
    fun `저장되지 않은 메시지는 다음 갱신에서 다시 시도할 수 있다`() {
        val message = fill(6)
        assertTrue(policy.acceptMessage("room", "삼성증권", message))
        policy.forgetMessage("room", "삼성증권", message)
        assertTrue(policy.acceptMessage("room", "삼성증권", message))
    }
}
