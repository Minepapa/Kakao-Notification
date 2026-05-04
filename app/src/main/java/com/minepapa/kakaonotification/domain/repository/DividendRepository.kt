package com.minepapa.kakaonotification.domain.repository

import com.minepapa.kakaonotification.domain.model.DividendRecord

interface DividendRepository {
    suspend fun appendDividends(spreadsheetId: String, records: List<DividendRecord>): Result<Unit>
}
