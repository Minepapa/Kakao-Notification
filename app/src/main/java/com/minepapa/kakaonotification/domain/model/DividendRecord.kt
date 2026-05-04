package com.minepapa.kakaonotification.domain.model

data class DividendRecord(
    val date: String,           // YYYY-MM-DD
    val afterTaxAmount: Long,   // 세후금액 (원)
    val stockName: String,
    val receivedTime: String,   // HH:mm:ss — 동일 알람 중복 판별용
)
