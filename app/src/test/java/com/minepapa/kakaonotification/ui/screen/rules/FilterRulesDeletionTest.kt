package com.minepapa.kakaonotification.ui.screen.rules

import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.repository.FilterRuleRepository
import com.minepapa.kakaonotification.domain.usecase.AddFilterRuleUseCase
import com.minepapa.kakaonotification.domain.usecase.DeleteFilterRuleUseCase
import com.minepapa.kakaonotification.domain.usecase.GetFilterRulesUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FilterRulesDeletionTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `요청 후 취소하면 규칙을 삭제하지 않는다`() = runTest(dispatcher) {
        val repo = mockk<FilterRuleRepository>(relaxed = true)
        val getRules = mockk<GetFilterRulesUseCase>()
        every { getRules() } returns flowOf(emptyList())
        val viewModel = FilterRulesViewModel(getRules, mockk<AddFilterRuleUseCase>(), DeleteFilterRuleUseCase(repo), repo)
        val rule = FilterRule(id = 7, senderName = "보낸 사람")

        viewModel.requestDeleteRule(rule)
        assertEquals(rule, viewModel.rulePendingDeletion.value)
        viewModel.cancelDeleteRule()
        viewModel.confirmDeleteRule()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.rulePendingDeletion.value)
        coVerify(exactly = 0) { repo.delete(any()) }
    }

    @Test fun `확인하면 요청한 규칙만 삭제한다`() = runTest(dispatcher) {
        val repo = mockk<FilterRuleRepository>(relaxed = true)
        val getRules = mockk<GetFilterRulesUseCase>()
        every { getRules() } returns flowOf(emptyList())
        val viewModel = FilterRulesViewModel(getRules, mockk<AddFilterRuleUseCase>(), DeleteFilterRuleUseCase(repo), repo)

        viewModel.requestDeleteRule(FilterRule(id = 7))
        viewModel.confirmDeleteRule()
        testScheduler.advanceUntilIdle()

        assertNull(viewModel.rulePendingDeletion.value)
        coVerify(exactly = 1) { repo.delete(7) }
    }
}
