package com.minepapa.kakaonotification.domain.model

/**
 * Google Sheets의 '체결내역' 탭에 기록될 데이터 모델
 */
data class ExecutionHistory(
    val tradeDate: Long,
    val tradeType: String,        // 매수/매도
    val stockCode: String = "",   // 종목코드
    val stockName: String,        // 종목명
    val quantity: Int,            // 수량
    val price: Double,            // 단가
    val currency: String = "KRW", // KRW 또는 USD
)