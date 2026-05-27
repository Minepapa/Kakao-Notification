package com.minepapa.kakaonotification.domain.repository

import com.minepapa.kakaonotification.domain.model.DividendRecord
import com.minepapa.kakaonotification.domain.model.DividendSheetEntry

interface DividendRepository {
    suspend fun appendDividends(spreadsheetId: String, records: List<DividendRecord>): Result<Unit>
    suspend fun getDividends(spreadsheetId: String): Result<List<DividendSheetEntry>>
    suspend fun updateStockName(spreadsheetId: String, rowNumber: Int, newName: String): Result<Unit>
}
