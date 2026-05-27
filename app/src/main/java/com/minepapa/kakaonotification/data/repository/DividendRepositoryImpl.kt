package com.minepapa.kakaonotification.data.repository

import com.minepapa.kakaonotification.data.remote.sheets.GoogleSheetsApi
import com.minepapa.kakaonotification.data.remote.sheets.model.AppendRequest
import com.minepapa.kakaonotification.domain.model.DividendRecord
import com.minepapa.kakaonotification.domain.model.DividendSheetEntry
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

        // 같은 날짜의 모든 processedKeys를 모아서 증권사별 종목명 차이에 의한 중복 방지
        val allKeysByDate = mutableMapOf<String, MutableSet<String>>()
        existing.forEach { (key, entry) ->
            allKeysByDate.getOrPut(key.first) { mutableSetOf() }.addAll(entry.processedKeys)
        }

        val grouped = records.groupBy { Pair(it.date, it.stockName) }

        grouped.forEach { (key, newRecords) ->
            val entry = existing[key]
            val dateKeys = allKeysByDate.getOrPut(key.first) { mutableSetOf() }

            val novelRecords = newRecords.filter { it.uniqueKey !in dateKeys }
            if (novelRecords.isEmpty()) return@forEach

            if (entry != null) {
                val newTotal = entry.currentAmount + novelRecords.sumOf { it.afterTaxAmount }
                val newKeys = (entry.processedKeys + novelRecords.map { it.uniqueKey })
                    .joinToString(",")

                val encodedAmount = URLEncoder.encode(
                    "'$DIVIDEND_SHEET_NAME'!B${entry.rowNumber}", "UTF-8"
                )
                val encodedKeys = URLEncoder.encode(
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
                    range = encodedKeys,
                    valueInputOption = "USER_ENTERED",
                    body = AppendRequest(listOf(listOf(newKeys))),
                )
            } else {
                val distinct = novelRecords.distinctBy { it.uniqueKey }
                val totalAmount = distinct.sumOf { it.afterTaxAmount }
                val keys = distinct.map { it.uniqueKey }.joinToString(",")
                api.appendValues(
                    spreadsheetId = spreadsheetId,
                    range = DIVIDEND_SHEET_NAME,
                    valueInputOption = "USER_ENTERED",
                    body = AppendRequest(
                        listOf(listOf(key.first, totalAmount.toString(), key.second, keys))
                    ),
                )
            }

            dateKeys.addAll(novelRecords.map { it.uniqueKey })
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
                    val processedKeys = row.getOrNull(3)
                        ?.split(",")
                        ?.map { it.trim() }
                        ?.filter { it.isNotBlank() }
                        ?.toSet() ?: emptySet()
                    val sheetRow = index + 2 // 0-index + 헤더 1행 + 1

                    Pair(date, name) to ExistingEntry(
                        rowNumber = sheetRow,
                        currentAmount = amount,
                        processedKeys = processedKeys,
                    )
                }
                .toMap()
        }.getOrDefault(emptyMap())
    }

    private data class ExistingEntry(
        val rowNumber: Int,
        val currentAmount: Long,
        val processedKeys: Set<String>,
    )

    override suspend fun getDividends(
        spreadsheetId: String
    ): Result<List<DividendSheetEntry>> = runCatching {
        val response = api.getValues(spreadsheetId, "'$DIVIDEND_SHEET_NAME'!A:D")
        val rows = response.values ?: return@runCatching emptyList()
        rows.drop(1).mapIndexedNotNull { index, row ->
            val date = row.getOrNull(0)?.trim() ?: return@mapIndexedNotNull null
            val amount = row.getOrNull(1)
                ?.replace(Regex("[^0-9]"), "")
                ?.toLongOrNull() ?: 0L
            val name = row.getOrNull(2)?.trim() ?: return@mapIndexedNotNull null
            if (date.isBlank() || name.isBlank()) return@mapIndexedNotNull null
            DividendSheetEntry(
                rowNumber = index + 2,
                date = date,
                amount = amount,
                stockName = name,
            )
        }
    }

    override suspend fun updateStockName(
        spreadsheetId: String,
        rowNumber: Int,
        newName: String
    ): Result<Unit> = runCatching {
        val range = URLEncoder.encode("'$DIVIDEND_SHEET_NAME'!C$rowNumber", "UTF-8")
        api.updateValues(
            spreadsheetId = spreadsheetId,
            range = range,
            valueInputOption = "USER_ENTERED",
            body = AppendRequest(listOf(listOf(newName))),
        )
    }

    companion object {
        private const val DIVIDEND_SHEET_NAME = "배당금"
    }
}
