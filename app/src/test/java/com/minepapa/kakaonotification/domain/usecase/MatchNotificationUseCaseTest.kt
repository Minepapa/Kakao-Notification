package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.repository.FilterRuleRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MatchNotificationUseCaseTest {

    private lateinit var repo: FilterRuleRepository
    private lateinit var useCase: MatchNotificationUseCase

    @Before
    fun setUp() {
        repo = mockk()
        useCase = MatchNotificationUseCase(repo)
    }

    @Test
    fun `단일 키워드 매칭`() = runTest {
        val rule = FilterRule(id = 1, senderName = "홍길동", keywords = listOf("공지"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("홍길동", "오늘 공지 있습니다")

        assertEquals(listOf("공지"), result?.matchedKeywords)
    }

    @Test
    fun `여러 키워드가 동시에 포함되면 모두 반환`() = runTest {
        val rule = FilterRule(id = 1, senderName = "", keywords = listOf("입금", "송금", "이체"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("은행", "입금 및 송금 완료")

        assertEquals(listOf("입금", "송금"), result?.matchedKeywords)
    }

    @Test
    fun `키워드 목록이 비어있으면 matchedKeywords는 빈 리스트`() = runTest {
        val rule = FilterRule(id = 1, senderName = "팀장", keywords = emptyList())
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("팀장", "아무 내용이나")

        assertTrue(result?.matchedKeywords?.isEmpty() == true)
    }

    @Test
    fun `발신자 불일치면 null 반환`() = runTest {
        val rule = FilterRule(id = 1, senderName = "홍길동", keywords = listOf("공지"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        assertNull(useCase("이순신", "공지 있습니다"))
    }

    @Test
    fun `키워드 전혀 불일치면 null 반환`() = runTest {
        val rule = FilterRule(id = 1, senderName = "홍길동", keywords = listOf("공지"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        assertNull(useCase("홍길동", "일반 대화입니다"))
    }

    @Test
    fun `비활성화된 규칙은 무시`() = runTest {
        val rule = FilterRule(id = 1, senderName = "홍길동", keywords = listOf("공지"), isEnabled = false)
        every { repo.getAll() } returns flowOf(listOf(rule))

        assertNull(useCase("홍길동", "공지 있습니다"))
    }

    @Test
    fun `대소문자 무시하여 매칭`() = runTest {
        val rule = FilterRule(id = 1, senderName = "Alice", keywords = listOf("ALERT", "ERROR"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("alice", "system alert and error triggered")

        assertEquals(listOf("ALERT", "ERROR"), result?.matchedKeywords)
    }
}
