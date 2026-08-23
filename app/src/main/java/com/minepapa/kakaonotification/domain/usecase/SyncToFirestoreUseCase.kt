package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import com.minepapa.kakaonotification.domain.repository.NotificationRelayRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * 미동기화 알림을 Firestore `kakaoInbox` 컬렉션에 적재한다.
 *
 * 체결내역/배당금 파싱·적재는 banana-portfolio-v2의 무인 파서(parse-notifications-to-vault.mjs)가
 * 이 컬렉션 원문을 단일 소스로 처리한다. 앱은 신뢰성 높은 원문 적재만 담당한다.
 * (2026-08-22 — SyncToSheetsUseCase를 대체, spreadsheetId 설정 없이 컬렉션 이름은
 * Constants.FIRESTORE_COLLECTION으로 고정.)
 */
class SyncToFirestoreUseCase @Inject constructor(
    private val logRepo: NotificationLogRepository,
    private val relayRepo: NotificationRelayRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val logsToSync = logRepo.getUnsynced().first()
        if (logsToSync.isEmpty()) {
            return@runCatching // 동기화할 로그 없음
        }

        relayRepo.sendLogs(logsToSync).getOrThrow()

        // 동기화된 로그 상태 업데이트
        logRepo.markSynced(logsToSync.map { it.id }, System.currentTimeMillis())
    }
}
