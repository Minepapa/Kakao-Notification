package com.minepapa.kakaonotification.ui.screen.log

import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import com.minepapa.kakaonotification.domain.usecase.DeleteNotificationLogsUseCase
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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationLogDeletionTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test fun `삭제 요청을 취소하면 선택을 유지하고 로그는 삭제하지 않는다`() = runTest(dispatcher) {
        val repo = mockk<NotificationLogRepository>(relaxed = true)
        every { repo.getAll() } returns flowOf(emptyList())
        val viewModel = NotificationLogViewModel(repo, DeleteNotificationLogsUseCase(repo))

        viewModel.enterSelectionMode()
        viewModel.toggleSelection(3)
        viewModel.requestDeleteSelected()
        assertEquals(setOf(3L), viewModel.deletionPendingIds.value)
        viewModel.cancelDeleteSelected()
        viewModel.confirmDeleteSelected()
        testScheduler.advanceUntilIdle()

        assertEquals(emptySet<Long>(), viewModel.deletionPendingIds.value)
        assertEquals(setOf(3L), viewModel.selectedIds.value)
        coVerify(exactly = 0) { repo.deleteLogs(any()) }
    }

    @Test fun `삭제 확인은 요청 시점의 선택만 삭제한다`() = runTest(dispatcher) {
        val repo = mockk<NotificationLogRepository>(relaxed = true)
        every { repo.getAll() } returns flowOf(emptyList())
        val viewModel = NotificationLogViewModel(repo, DeleteNotificationLogsUseCase(repo))

        viewModel.enterSelectionMode()
        viewModel.toggleSelection(3)
        viewModel.requestDeleteSelected()
        viewModel.toggleSelection(4)
        viewModel.confirmDeleteSelected()
        testScheduler.advanceUntilIdle()

        coVerify(exactly = 1) { repo.deleteLogs(listOf(3L)) }
        assertFalse(viewModel.selectionMode.value)
        assertEquals(emptySet<Long>(), viewModel.selectedIds.value)
    }
}
