package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import com.minepapa.kakaonotification.domain.repository.SheetsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 미동기화 알림을 [알람] 탭에 적재한다.
 *
 * 체결내역/배당금 파싱·적재는 banana-portfolio 의 무인 파서(parse-notifications.mjs)가
 * [알람] 원문을 단일 소스로 처리한다. 앱은 신뢰성 높은 원문 적재만 담당한다.
 */
class SyncToSheetsUseCase @Inject constructor(
    private val logRepo: NotificationLogRepository,
    private val sheetsRepo: SheetsRepository,
    private val prefs: AppPreferences,
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

        // [알람] 탭에 원문 적재 (banana 파서가 이 원문을 읽어 체결내역/배당금 생성)
        val alarmSheetName = prefs.sheetName.first()
        sheetsRepo.appendRows(spreadsheetId, alarmSheetName, logsToSync).getOrThrow()

        // 동기화된 로그 상태 업데이트
        logRepo.markSynced(logsToSync.map { it.id }, System.currentTimeMillis())
    }
}
