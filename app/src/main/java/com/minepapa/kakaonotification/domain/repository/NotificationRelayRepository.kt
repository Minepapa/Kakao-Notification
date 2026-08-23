package com.minepapa.kakaonotification.domain.repository

import com.minepapa.kakaonotification.domain.model.NotificationLog

// 2026-08-22 — SheetsRepository(구글시트 append)를 대체. "중계"라는 역할은 그대로고
// 목적지만 Firestore로 바뀌었다.
interface NotificationRelayRepository {
    suspend fun sendLogs(logs: List<NotificationLog>): Result<Unit>
}
