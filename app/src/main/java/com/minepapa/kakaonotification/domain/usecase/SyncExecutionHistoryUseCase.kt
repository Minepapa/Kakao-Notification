package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncExecutionHistoryUseCase @Inject constructor(
    private val logRepo: NotificationLogRepository,
    private val prefs: AppPreferences,
    private val appendExecutionHistoryUseCase: AppendExecutionHistoryUseCase
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val spreadsheetId = prefs.spreadsheetId.first()
        if (spreadsheetId.isBlank()) return@runCatching

        val allLogs = logRepo.getAll().first()
        if (allLogs.isEmpty()) return@runCatching

        appendExecutionHistoryUseCase(spreadsheetId, allLogs).getOrThrow()
    }
}
