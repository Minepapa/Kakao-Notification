package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.DividendRecord
import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.repository.DividendRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern
import javax.inject.Inject

class AppendDividendUseCase @Inject constructor(
    private val dividendRepository: DividendRepository
) {
    suspend operator fun invoke(
        spreadsheetId: String,
        logs: List<NotificationLog>
    ): Result<Unit> {
        if (spreadsheetId.isBlank()) {
            return Result.failure(IllegalStateException("스프레드시트 ID가 설정되지 않았습니다."))
        }

        val records = logs.mapNotNull { log ->
            val isDividend = log.body.contains("배당금") ||
                    log.body.contains("분배금") ||
                    log.body.contains("채권원리금")
            if (!isDividend) return@mapNotNull null
            parseDividendDetails(log)
        }

        if (records.isEmpty()) return Result.success(Unit)

        // 중복 제거 및 합산은 repository 에서 처리 (시트의 기존 데이터 기준)
        return dividendRepository.appendDividends(spreadsheetId, records)
    }

    private fun parseDividendDetails(log: NotificationLog): DividendRecord? {
        // NH투자증권 분배금/배당금 입금 안내 (멀티라인, 입금일 명시)
        val nhMatcher = nhPattern.matcher(log.body)
        if (nhMatcher.find()) {
            return try {
                val stockName = nhMatcher.group("stockName")?.trim() ?: return null
                val amountStr = nhMatcher.group("amount")?.replace(Regex("[^0-9]"), "") ?: return null
                val dateStr = nhMatcher.group("date")?.trim() ?: return null
                val amount = amountStr.toLongOrNull() ?: return null
                DividendRecord(
                    date = dateStr.replace(".", "-"),
                    afterTaxAmount = amount,
                    stockName = stockName,
                    receivedTime = datePartOf(log.receivedAt, "HH:mm:ss"),
                )
            } catch (e: Exception) {
                null
            }
        }

        // NH투자증권 채권원리금 입금 안내 (단일 라인, 날짜 MM/DD만 있음)
        val bondMatcher = nhBondPattern.matcher(log.body)
        if (bondMatcher.find()) {
            return try {
                val month = bondMatcher.group("month") ?: return null
                val day = bondMatcher.group("day") ?: return null
                val amountStr = bondMatcher.group("amount")?.replace(Regex("[^0-9]"), "") ?: return null
                val stockName = bondMatcher.group("stockName")?.trim() ?: return null
                val amount = amountStr.toLongOrNull() ?: return null
                val year = datePartOf(log.receivedAt, "yyyy")
                DividendRecord(
                    date = "$year-$month-$day",
                    afterTaxAmount = amount,
                    stockName = stockName,
                    receivedTime = datePartOf(log.receivedAt, "HH:mm:ss"),
                )
            } catch (e: Exception) {
                null
            }
        }

        // 삼성증권 분배금/배당금 입금안내 (멀티라인, 입금일 없음 → 수신일 사용)
        val samsungMatcher = samsungPattern.matcher(log.body)
        if (samsungMatcher.find()) {
            return try {
                val stockName = samsungMatcher.group("stockName")?.trim() ?: return null
                val amountStr = samsungMatcher.group("amount")?.replace(Regex("[^0-9]"), "") ?: return null
                val amount = amountStr.toLongOrNull() ?: return null
                DividendRecord(
                    date = datePartOf(log.receivedAt, "yyyy-MM-dd"),
                    afterTaxAmount = amount,
                    stockName = stockName,
                    receivedTime = datePartOf(log.receivedAt, "HH:mm:ss"),
                )
            } catch (e: Exception) {
                null
            }
        }

        return null
    }

    private fun datePartOf(receivedAt: Long, pattern: String): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(receivedAt))
    }

    // [NH투자증권] 분배금/배당금 입금 안내 — 종목명, 세후금액, 입금일 추출
    private val nhPattern = Pattern.compile(
        "\\[NH투자증권\\]\\s*(?:분배금|배당금)\\s*입금\\s*안내[\\s\\S]*?" +
                "종목명\\s*:\\s*(?<stockName>[^\\n\\r]+)[\\s\\S]*?" +
                "세후금액\\s*:\\s*(?<amount>[\\d,]+)\\s*원[\\s\\S]*?" +
                "입금일\\s*:\\s*(?<date>\\d{4}\\.\\d{2}\\.\\d{2})",
        Pattern.DOTALL
    )

    // [NH투자증권] 채권원리금 입금 안내 — MM/DD 금액 종목명 추출
    private val nhBondPattern = Pattern.compile(
        "\\[NH투자증권\\]\\s*채권원리금\\s*입금\\s*안내[\\n\\r]+" +
                "[^\\n\\r]*입금\\s+(?<month>\\d{2})/(?<day>\\d{2})\\s+\\d{2}:\\d{2}\\s+" +
                "(?<amount>[\\d,]+)\\s+(?<stockName>[^\\n\\r]+)",
        Pattern.DOTALL
    )

    // [삼성증권] <분배금/배당금 입금안내> — 종목명, 세후 분배금액/배당금액 추출 (날짜 없음 → 수신일 사용)
    private val samsungPattern = Pattern.compile(
        "\\[삼성증권\\]\\s*<(?:분배금|배당금)[^>]*>[\\s\\S]*?" +
                "종목명\\s*:\\s*(?<stockName>[^\\n\\r\\-]+)[\\s\\S]*?" +
                "세후\\s*(?:분배금액|배당금액)\\s*:\\s*(?<amount>[\\d,]+)\\s*원",
        Pattern.DOTALL
    )
}
