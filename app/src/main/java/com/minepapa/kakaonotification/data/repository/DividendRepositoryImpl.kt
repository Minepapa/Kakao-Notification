package com.minepapa.kakaonotification.data.repository

import com.minepapa.kakaonotification.data.remote.sheets.GoogleSheetsApi
import com.minepapa.kakaonotification.data.remote.sheets.model.AppendRequest
import com.minepapa.kakaonotification.domain.model.DividendRecord
import com.minepapa.kakaonotification.domain.repository.DividendRepository
import java.net.URLEncoder
import javax.inject.Inject

class DividendRepositoryImpl @Inject constructor(
    private val api: GoogleSheetsApi
) : DividendRepository {

    override suspend fun appendDividends(
        spreadsheetId: String,
        records: List<DividendRecord>
    ): Result<Unit> = runCatching {
        val existing = getExistingEntries(spreadsheetId)

        // (date, stockName)별로 묶어서 처리
        val grouped = records.groupBy { Pair(it.date, it.stockName) }

        grouped.forEach { (key, newRecords) ->
            val entry = existing[key]
            if (entry != null) {
                // 시/분/초가 다른 것만 신규 → 기존 항목에 누적
                val novelRecords = newRecords.filter { it.receivedTime !in entry.processedTimes }
                if (novelRecords.isEmpty()) return@forEach

                val newTotal = entry.currentAmount + novelRecords.sumOf { it.afterTaxAmount }
                val newTimes = (entry.processedTimes + novelRecords.map { it.receivedTime })
                    .joinToString(",")

                val encodedAmount = URLEncoder.encode(
                    "'$DIVIDEND_SHEET_NAME'!B${entry.rowNumber}", "UTF-8"
                )
                val encodedTimes = URLEncoder.encode(
                    "'$DIVIDEND_SHEET_NAME'!D${entry.rowNumber}", "UTF-8"
                )
                api.updateValues(
                    spreadsheetId = spreadsheetId,
                    range = encodedAmount,
                    valueInputOption = "USER_ENTERED",
                    body = AppendRequest(listOf(listOf(newTotal.toString()))),
                )
                api.updateValues(
                    spreadsheetId = spreadsheetId,
                    range = encodedTimes,
                    valueInputOption = "USER_ENTERED",
                    body = AppendRequest(listOf(listOf(newTimes))),
                )
            } else {
                // 완전히 새로운 (date, stockName) — 배치 내 시각별 중복 제거 후 합산
                val distinct = newRecords.distinctBy { it.receivedTime }
                val totalAmount = distinct.sumOf { it.afterTaxAmount }
                val times = distinct.map { it.receivedTime }.joinToString(",")
                api.appendValues(
                    spreadsheetId = spreadsheetId,
                    range = DIVIDEND_SHEET_NAME,
                    valueInputOption = "USER_ENTERED",
                    body = AppendRequest(
                        listOf(listOf(key.first, totalAmount.toString(), key.second, times))
                    ),
                )
            }
        }
    }

    private suspend fun getExistingEntries(
        spreadsheetId: String
    ): Map<Pair<String, String>, ExistingEntry> {
        return runCatching {
            val response = api.getValues(spreadsheetId, "'$DIVIDEND_SHEET_NAME'!A:D")
            val rows = response.values ?: return@runCatching emptyMap()

            // rows[0] = 헤더(시트 1행), rows[1] = 첫 데이터(시트 2행)
            rows.drop(1)
                .mapIndexedNotNull { index, row ->
                    val date = row.getOrNull(0)?.trim() ?: return@mapIndexedNotNull null
                    val name = row.getOrNull(2)?.trim() ?: return@mapIndexedNotNull null
                    if (date.isBlank() || name.isBlank()) return@mapIndexedNotNull null

                    val amount = row.getOrNull(1)
                        ?.replace(Regex("[^0-9]"), "")
                        ?.toLongOrNull() ?: 0L
                    val processedTimes = row.getOrNull(3)
                        ?.split(",")
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        ?.toSet() ?: emptySet()
                    val sheetRow = index + 2 // 0-index + 헤더 1행 + 1

                    Pair(date, name) to ExistingEntry(
                        rowNumber = sheetRow,
                        currentAmount = amount,
                        processedTimes = processedTimes,
                    )
                }
                .toMap()
        }.getOrDefault(emptyMap())
    }

    private data class ExistingEntry(
        val rowNumber: Int,
        val currentAmount: Long,
        val processedTimes: Set<String>,
    )

    companion object {
        private const val DIVIDEND_SHEET_NAME = "배당금"
    }
}
