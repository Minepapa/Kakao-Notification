package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import com.minepapa.kakaonotification.domain.repository.SheetsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncToSheetsUseCase @Inject constructor(
    private val logRepo: NotificationLogRepository,
    private val sheetsRepo: SheetsRepository,
    private val prefs: AppPreferences,
    private val appendExecutionHistoryUseCase: AppendExecutionHistoryUseCase,
    private val appendDividendUseCase: AppendDividendUseCase,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val spreadsheetId = prefs.spreadsheetId.first()
        if (spreadsheetId.isBlank()) {
            return@runCatching // ID 없으면 중단
        }

        val logsToSync = logRepo.getUnsynced().first()
        if (logsToSync.isEmpty()) {
            return@runCatching // 동기화할 로그 없음
        }

        // 1. [알람] 탭에 동기화
        val alarmSheetName = prefs.sheetName.first()
        sheetsRepo.appendRows(spreadsheetId, alarmSheetName, logsToSync).getOrThrow()

        // 2. [체결내역] 탭 동기화
        appendExecutionHistoryUseCase(spreadsheetId, logsToSync).onFailure {
            System.err.println("Execution history sync failed: ${it.message}")
        }

        // 3. [배당금] 탭 동기화
        appendDividendUseCase(spreadsheetId, logsToSync).onFailure {
            System.err.println("Dividend sync failed: ${it.message}")
        }

        // 4. 동기화된 로그 상태 업데이트
        logRepo.markSynced(logsToSync.map { it.id }, System.currentTimeMillis())
    }
}
