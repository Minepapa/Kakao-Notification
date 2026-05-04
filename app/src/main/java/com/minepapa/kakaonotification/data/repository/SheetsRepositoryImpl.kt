package com.minepapa.kakaonotification.data.repository

import com.minepapa.kakaonotification.data.remote.sheets.GoogleSheetsApi
import com.minepapa.kakaonotification.data.remote.sheets.model.AppendRequest
import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.repository.SheetsRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class SheetsRepositoryImpl @Inject constructor(
    private val api: GoogleSheetsApi,
) : SheetsRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override suspend fun appendRows(
        spreadsheetId: String,
        sheetName: String,
        logs: List<NotificationLog>,
    ): Result<Unit> = runCatching {
        // 컬럼 순서: 시간 | 발신인 | 키워드 | 내용
        val rows = logs.map { log ->
            listOf(
                dateFormat.format(Date(log.receivedAt)),
                log.sender,
                log.matchedKeywords.joinToString(", "),
                log.body,
            )
        }
        api.appendValues(
            spreadsheetId = spreadsheetId,
            range = sheetName,
            valueInputOption = "RAW",
            body = AppendRequest(values = rows),
        )
    }
}
