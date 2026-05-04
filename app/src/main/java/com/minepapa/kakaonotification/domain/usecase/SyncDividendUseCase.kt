package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncDividendUseCase @Inject constructor(
    private val logRepo: NotificationLogRepository,
    private val prefs: AppPreferences,
    private val appendDividendUseCase: AppendDividendUseCase,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val spreadsheetId = prefs.spreadsheetId.first()
        if (spreadsheetId.isBlank()) return@runCatching

        val allLogs = logRepo.getAll().first()
        if (allLogs.isEmpty()) return@runCatching

        appendDividendUseCase(spreadsheetId, allLogs).getOrThrow()
    }
}
