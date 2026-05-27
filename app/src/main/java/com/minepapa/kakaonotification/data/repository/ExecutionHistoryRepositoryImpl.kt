package com.minepapa.kakaonotification.data.repository

import com.minepapa.kakaonotification.data.remote.sheets.GoogleSheetsApi
import com.minepapa.kakaonotification.data.remote.sheets.model.AppendRequest
import com.minepapa.kakaonotification.domain.model.ExecutionHistory
import com.minepapa.kakaonotification.domain.repository.ExecutionHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExecutionHistoryRepositoryImpl @Inject constructor(
    private val api: GoogleSheetsApi
) : ExecutionHistoryRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override suspend fun appendExecutions(
        spreadsheetId: String,
        logs: List<ExecutionHistory>
    ): Result<Unit> = runCatching {
        val existingKeys = getExistingKeys(spreadsheetId)
        val portfolioMap = getPortfolioMap(spreadsheetId)

        val newLogs = logs.filter { log ->
            compositeKey(log) !in existingKeys
        }

        if (newLogs.isEmpty()) return@runCatching

        val rows = newLogs.map { log ->
            val portfolio = portfolioMap[log.stockName]
            val isOverseas = log.currency == "USD"
            val stockCode = if (log.stockCode.isNotBlank()) log.stockCode
                            else resolveStockCode(spreadsheetId, log.stockName)
            listOf(
                dateFormat.format(Date(log.tradeDate)),                          // A: 체결일시
                log.tradeType,                                                   // B: 구분 (매수/매도)
                portfolio?.first ?: "",                                          // C: 계좌 (ISA/위탁/연금저축/IRP)
                stockCode,                                                       // D: 종목코드
                portfolio?.second ?: "",                                         // E: 자산군
                log.stockName,                                                   // F: 종목명
                if (isOverseas)                                                  // G: 매수단가 (해외: USD*환율 → 원화)
                    "=${log.price}*'설정'!\$B\$2"
                else
                    log.price.toString(),
                log.quantity.toString(),                                         // H: 수량
                "=INDIRECT(\"G\"&ROW())*INDIRECT(\"H\"&ROW())",                 // I: 매수금액 (G*H)
                currentPriceFormula(stockCode, isOverseas),                     // J: 현재가
                "=INDIRECT(\"L\"&ROW())-INDIRECT(\"I\"&ROW())",                 // K: 손익 (L-I)
                "=INDIRECT(\"H\"&ROW())*INDIRECT(\"J\"&ROW())",                 // L: 평가금액 (H*J)
                "=INDIRECT(\"L\"&ROW())/INDIRECT(\"I\"&ROW())-1",               // M: 수익률 (L/I-1)
            )
        }

        api.appendValues(
            spreadsheetId = spreadsheetId,
            range = EXECUTION_HISTORY_SHEET_NAME,
            valueInputOption = "USER_ENTERED",
            body = AppendRequest(rows)
        )
    }

    // 1) ISA/위탁/연금저축/IRP 탭 B열(종목명) 매칭 → F열 수식에서 6자리 종목코드 추출
    // 2) 없으면 Naver Finance 자동완성 API로 검색
    private suspend fun resolveStockCode(spreadsheetId: String, stockName: String): String {
        val fromPortfolio = getStockCodeFromPortfolioTabs(spreadsheetId, stockName)
        if (fromPortfolio.isNotBlank()) return fromPortfolio
        return searchStockCodeOnline(stockName)
    }

    private suspend fun getStockCodeFromPortfolioTabs(
        spreadsheetId: String,
        stockName: String,
    ): String {
        val accountTabs = listOf("ISA", "위탁", "연금저축", "IRP")
        accountTabs.forEach { tab ->
            runCatching {
                // B열(종목명)과 F열(수식)을 FORMULA render로 읽음
                val response = api.getFormulas(spreadsheetId, "'$tab'!A:F", "FORMULA")
                response.values?.forEach { row ->
                    val name    = if (row.size > 1) row[1].trim() else ""   // B열
                    val formula = if (row.size > 5) row[5].trim() else ""   // F열
                    if (name == stockName && formula.isNotBlank()) {
                        val code = extractStockCode(formula)
                        if (code.isNotBlank()) return code
                    }
                }
            }
        }
        return ""
    }

    // 수식 문자열에서 6자리 종목코드 추출
    // =GOOGLEFINANCE("476800")  → 476800
    // =IMPORTXML("...code=0086B0...", ...) → 0086B0
    // 평문 값 "476800" → 476800
    private fun extractStockCode(formulaOrValue: String): String {
        // 따옴표 안의 6자리 alphanumeric
        Regex("""["']([A-Za-z0-9]{6})["']""").find(formulaOrValue)
            ?.groupValues?.get(1)?.let { return it }
        // code= 파라미터
        Regex("""code=([A-Za-z0-9]{6})""").find(formulaOrValue)
            ?.groupValues?.get(1)?.let { return it }
        // 평문 6자리
        if (formulaOrValue.matches(Regex("[A-Za-z0-9]{6}"))) return formulaOrValue
        return ""
    }

    // Naver Finance 모바일 자동완성 API로 종목명 → 종목코드 검색
    // Google Finance는 JS 렌더링 기반이라 Android HTTP 클라이언트로 파싱 불가
    private suspend fun searchStockCodeOnline(stockName: String): String = withContext(Dispatchers.IO) {
        runCatching {
            val encoded = URLEncoder.encode(stockName, "UTF-8")
            val url = URL("https://m.stock.naver.com/api/search/auto-complete?query=$encoded")
            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "Mozilla/5.0")
            conn.setRequestProperty("Accept", "application/json")
            conn.connectTimeout = 5_000
            conn.readTimeout = 5_000
            val json = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()

            // hits 배열을 개별 항목으로 분리 후 nameKor 일치하는 항목의 itemCode 반환
            val itemCodeRegex = Regex(""""itemCode"\s*:\s*"([^"]+)"""")
            val nameKorRegex  = Regex(""""nameKor"\s*:\s*"([^"]+)"""")

            // 각 hit 객체는 "typeCode" 키로 시작
            val hits = json.split(Regex(""""typeCode"""")).drop(1)
            for (hit in hits) {
                val name = nameKorRegex.find(hit)?.groupValues?.get(1)?.trim() ?: continue
                val code = itemCodeRegex.find(hit)?.groupValues?.get(1)?.trim() ?: continue
                if (name == stockName.trim()) return@runCatching code
            }
            // 정확 일치 없으면 첫 번째 결과 반환
            itemCodeRegex.find(json)?.groupValues?.get(1) ?: ""
        }.getOrDefault("")
    }

    private fun currentPriceFormula(stockCode: String, isOverseas: Boolean): String {
        if (stockCode.isBlank()) return ""
        return when {
            isOverseas ->
                "=GOOGLEFINANCE(\"$stockCode\")*'설정'!\$B\$2"
            stockCode.all { it.isDigit() } ->
                "=GOOGLEFINANCE(\"$stockCode\")"
            else ->
                "=IMPORTXML(\"https://finance.naver.com/item/main.naver?code=$stockCode\", \"//p[@class='no_today']/em/span[1]\")"
        }
    }

    private suspend fun getPortfolioMap(spreadsheetId: String): Map<String, Pair<String, String>> {
        val accountTabs = listOf("ISA", "위탁", "연금저축", "IRP")
        val result = mutableMapOf<String, Pair<String, String>>()
        val duplicates = mutableSetOf<String>()

        accountTabs.forEach { tab ->
            runCatching {
                val response = api.getValues(spreadsheetId, "'$tab'!A:B")
                response.values?.forEach { row ->
                    if (row.size >= 2) {
                        val assetClass = row[0].trim()
                        val stockName  = row[1].trim()
                        if (stockName.isNotBlank()) {
                            if (result.containsKey(stockName)) duplicates.add(stockName)
                            else result[stockName] = Pair(tab, assetClass)
                        }
                    }
                }
            }
        }

        duplicates.forEach { result.remove(it) }
        return result
    }

    // A(0)=체결일시, B(1)=구분, F(5)=종목명, H(7)=수량
    // 날짜만으로 중복을 판단하면 동시 체결 시 두 번째 건이 누락되므로
    // (일시|구분|종목명|수량) 복합 키로 중복 여부를 확인
    private suspend fun getExistingKeys(spreadsheetId: String): Set<String> {
        return runCatching {
            val response = api.getValues(spreadsheetId, "'$EXECUTION_HISTORY_SHEET_NAME'!A:H")
            response.values
                ?.mapNotNull { row ->
                    val date      = normalizeDate(row.getOrElse(0) { "" })
                    val tradeType = row.getOrElse(1) { "" }.trim()
                    val stockName = row.getOrElse(5) { "" }.trim()
                    val quantity  = row.getOrElse(7) { "" }.trim().replace(Regex("\\.0*$"), "")
                    if (date.isBlank() || stockName.isBlank()) null
                    else "$date|$tradeType|$stockName|$quantity"
                }
                ?.toSet()
                ?: emptySet()
        }.getOrDefault(emptySet())
    }

    // Google Sheets가 "9:20:42" (leading zero 생략)로 반환할 수 있으므로
    // SimpleDateFormat으로 재파싱하여 앱의 compositeKey와 형식을 일치시킴
    private fun normalizeDate(raw: String): String {
        return runCatching {
            dateFormat.format(dateFormat.parse(raw.trim())!!)
        }.getOrDefault(raw.trim())
    }

    private fun compositeKey(log: ExecutionHistory): String =
        "${dateFormat.format(Date(log.tradeDate))}|${log.tradeType}|${log.stockName}|${log.quantity}"

    companion object {
        private const val EXECUTION_HISTORY_SHEET_NAME = "체결내역"
    }
}
