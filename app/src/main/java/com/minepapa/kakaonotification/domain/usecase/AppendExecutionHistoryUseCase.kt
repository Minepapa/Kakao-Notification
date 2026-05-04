package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.ExecutionHistory
import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.repository.ExecutionHistoryRepository
import javax.inject.Inject
import java.util.regex.Pattern


/**
 * 동기화된 알림 로그를 파싱하여 체결 내역 시트에 기록하는 UseCase
 */
class AppendExecutionHistoryUseCase @Inject constructor(
    private val executionHistoryRepository: ExecutionHistoryRepository
) {
    suspend operator fun invoke(
        spreadsheetId: String,
        syncedLogs: List<NotificationLog>
    ): Result<Unit> {
        if (spreadsheetId.isBlank()) {
            return Result.failure(IllegalStateException("스프레드시트 ID가 설정되지 않았습니다."))
        }

        val executionHistories = syncedLogs.mapNotNull { log ->
            // 본문에 체결 관련 내용이 없으면 건너뜀
            val isExecution = log.body.contains("체결") && (log.body.contains("매수") || log.body.contains("매도"))
            if (!isExecution) return@mapNotNull null

            parseExecutionDetails(log)
        }

        if (executionHistories.isEmpty()) {
            return Result.success(Unit) // 처리할 체결 내역 없음
        }

        return executionHistoryRepository.appendExecutions(spreadsheetId, executionHistories)
    }

    private fun parseExecutionDetails(log: NotificationLog): ExecutionHistory? {
        tradePatterns.forEach { pattern ->
            val matcher = pattern.regex.matcher(log.body)
            if (matcher.find()) {
                return try {
                    val tradeType = if (log.body.contains("매수")) "매수" else "매도"
                    val stockName = matcher.group("stockName")?.trim()
                    val stockCode = try { matcher.group("stockCode")?.trim() ?: "" } catch (e: IllegalArgumentException) { "" }
                    val quantity = matcher.group("quantity")?.replace(Regex("[^0-9]"), "")?.toIntOrNull()
                    val price = matcher.group("price")?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull()

                    if (stockName != null && quantity != null && price != null) {
                        ExecutionHistory(
                            tradeDate = log.receivedAt,
                            tradeType = tradeType,
                            stockCode = stockCode,
                            stockName = stockName,
                            quantity = quantity,
                            price = price,
                            currency = if (pattern.isOverseas) "USD" else "KRW",
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

    private val tradePatterns = listOf(
        // NH투자증권 국내 - 종목코드 있는 멀티라인 형식
        // "[NH투자증권] 매수 주문체결 알림\n종 목 명 : KODEX ...\n종목코드 : 476800\n체결수량 : 50주\n체결단가 : 4,935원"
        TradePattern(
            broker = "NH투자증권",
            regex = Pattern.compile(
                "\\[NH투자증권\\][\\s\\S]*?종\\s*목\\s*명\\s*:\\s*(?<stockName>[^\\n\\r]+)[\\s\\S]*?종목코드\\s*:\\s*(?<stockCode>[A-Za-z0-9]{6})[\\s\\S]*?체결수량\\s*:\\s*(?<quantity>[\\d,]+)\\s*주[\\s\\S]*?체결단가\\s*:\\s*(?<price>[\\d,]+)\\s*원",
                Pattern.DOTALL
            )
        ),

        // NH투자증권 해외 - 종목명에서 티커 추출
        // "[NH투자증권] 해외주식 체결집계 내역 안내\n종목명 : (TSLA US)테슬라\n체결수량 : 1주\n체결가격 : 358.940"
        TradePattern(
            broker = "NH투자증권 해외",
            isOverseas = true,
            regex = Pattern.compile(
                "\\[NH투자증권\\] 해외주식[\\s\\S]*?종목명\\s*:\\s*\\((?<stockCode>[A-Z0-9]+)\\s+[A-Z]+\\)(?<stockName>[^\\n\\r]+)[\\s\\S]*?체결수량\\s*:\\s*(?<quantity>[\\d,]+)\\s*주[\\s\\S]*?체결가격\\s*:\\s*(?<price>[\\d.]+)",
                Pattern.DOTALL
            )
        ),

        // 삼성증권 주식체결안내 멀티라인 형식
        // "[삼성증권]<주식체결안내>\n71612*****-15\nKODEX 미국나스닥100\n매수4주 23,845원\n체결(1차)"
        // Line2: 계좌(마스킹, 건너뜀) / Line3: 종목명 / Line4: 구분+수량+단가
        TradePattern(
            broker = "삼성증권",
            regex = Pattern.compile(
                "\\[삼성증권\\]<주식체결안내>[\\n\\r]+[^\\n\\r]+[\\n\\r]+(?<stockName>[^\\n\\r]+)[\\n\\r]+(?:매수|매도)(?<quantity>[\\d,]+)주\\s+(?<price>[\\d,]+)원"
            )
        ),

        // 한국투자증권 체결안내 멀티라인 형식
        // "[한국투자증권 체결안내]11:14\n*종목명:TIGER TDF2045 적격(0025N0)\n*체결수량:4주\n*체결단가:11,440원"
        // 종목명 괄호 안 6자리가 종목코드
        TradePattern(
            broker = "한국투자증권",
            regex = Pattern.compile(
                "\\[한국투자증권 체결안내\\][\\s\\S]*?종목명\\s*:\\s*(?<stockName>[^(\\n\\r]+?)\\s*\\((?<stockCode>[A-Za-z0-9]{6})\\)[\\s\\S]*?체결수량\\s*:\\s*(?<quantity>[\\d,]+)\\s*주[\\s\\S]*?체결단가\\s*:\\s*(?<price>[\\d,]+)\\s*원",
                Pattern.DOTALL
            )
        ),
    )

    private data class TradePattern(
        val broker: String,
        val regex: Pattern,
        val isOverseas: Boolean = false,
    )
}