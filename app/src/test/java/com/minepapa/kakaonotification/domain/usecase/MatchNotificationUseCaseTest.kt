package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.repository.FilterRuleRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
    fun `발신자와 키워드 모두 일치하면 해당 규칙과 키워드 반환`() = runTest {
        val rule = FilterRule(id = 1, senderName = "홍길동", keywords = listOf("공지"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("홍길동", "오늘 공지 있습니다")

        assertEquals(rule, result?.rule)
        assertEquals("공지", result?.matchedKeyword)
    }

    @Test
    fun `키워드 목록이 비어있으면 matchedKeyword는 null`() = runTest {
        val rule = FilterRule(id = 1, senderName = "팀장", keywords = emptyList())
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("팀장", "아무 내용이나")

        assertEquals(rule, result?.rule)
        assertNull(result?.matchedKeyword)
    }

    @Test
    fun `여러 키워드 중 실제로 매칭된 키워드만 반환`() = runTest {
        val rule = FilterRule(id = 1, senderName = "", keywords = listOf("입금", "송금", "이체"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("은행", "100만원 송금 완료")

        assertEquals("송금", result?.matchedKeyword)
    }

    @Test
    fun `발신자 불일치면 null 반환`() = runTest {
        val rule = FilterRule(id = 1, senderName = "홍길동", keywords = listOf("공지"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        assertNull(useCase("이순신", "공지 있습니다"))
    }

    @Test
    fun `키워드 불일치면 null 반환`() = runTest {
        val rule = FilterRule(id = 1, senderName = "홍길동", keywords = listOf("공지"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        assertNull(useCase("홍길동", "일반 대화입니다"))
    }

    @Test
    fun `비활성화된 규칙은 매칭하지 않음`() = runTest {
        val rule = FilterRule(id = 1, senderName = "홍길동", keywords = listOf("공지"), isEnabled = false)
        every { repo.getAll() } returns flowOf(listOf(rule))

        assertNull(useCase("홍길동", "공지 있습니다"))
    }

    @Test
    fun `대소문자 무시하여 매칭`() = runTest {
        val rule = FilterRule(id = 1, senderName = "Alice", keywords = listOf("ALERT"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("alice", "system alert triggered")

        assertEquals("ALERT", result?.matchedKeyword)
    }
}
