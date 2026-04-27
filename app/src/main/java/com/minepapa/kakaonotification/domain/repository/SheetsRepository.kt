package com.minepapa.kakaonotification.domain.repository

import com.minepapa.kakaonotification.domain.model.NotificationLog

interface SheetsRepository {
    suspend fun appendRows(
        spreadsheetId: String,
        sheetName: String,
        logs: List<NotificationLog>,
    ): Result<Unit>
}
