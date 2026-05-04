package com.minepapa.kakaonotification.domain.repository

import com.minepapa.kakaonotification.domain.model.ExecutionHistory

/**
 * 체결 내역을 Google Sheets에 기록하기 위한 Repository 인터페이스
 */
interface ExecutionHistoryRepository {
    suspend fun appendExecutions(
        spreadsheetId: String,
        logs: List<ExecutionHistory>
    ): Result<Unit>
}