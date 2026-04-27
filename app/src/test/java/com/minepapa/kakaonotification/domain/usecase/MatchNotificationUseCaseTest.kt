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
    fun `발신자와 키워드 모두 일치하면 해당 규칙 반환`() = runTest {
        val rule = FilterRule(id = 1, senderName = "홍길동", keywords = listOf("공지"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("홍길동", "오늘 공지 있습니다")

        assertEquals(rule, result)
    }

    @Test
    fun `발신자가 비어있으면 모든 발신자 매칭`() = runTest {
        val rule = FilterRule(id = 2, senderName = "", keywords = listOf("긴급"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("누구든지", "긴급 공지입니다")

        assertEquals(rule, result)
    }

    @Test
    fun `키워드 목록이 비어있으면 본문 무관 매칭`() = runTest {
        val rule = FilterRule(id = 3, senderName = "팀장", keywords = emptyList())
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("팀장", "아무 내용이나")

        assertEquals(rule, result)
    }

    @Test
    fun `발신자 불일치면 null 반환`() = runTest {
        val rule = FilterRule(id = 4, senderName = "홍길동", keywords = listOf("공지"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("이순신", "공지 있습니다")

        assertNull(result)
    }

    @Test
    fun `키워드 불일치면 null 반환`() = runTest {
        val rule = FilterRule(id = 5, senderName = "홍길동", keywords = listOf("공지"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("홍길동", "일반 대화입니다")

        assertNull(result)
    }

    @Test
    fun `비활성화된 규칙은 매칭하지 않음`() = runTest {
        val rule = FilterRule(id = 6, senderName = "홍길동", keywords = listOf("공지"), isEnabled = false)
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("홍길동", "공지 있습니다")

        assertNull(result)
    }

    @Test
    fun `대소문자 무시하여 매칭`() = runTest {
        val rule = FilterRule(id = 7, senderName = "Alice", keywords = listOf("ALERT"))
        every { repo.getAll() } returns flowOf(listOf(rule))

        val result = useCase("alice", "system alert triggered")

        assertEquals(rule, result)
    }
}
