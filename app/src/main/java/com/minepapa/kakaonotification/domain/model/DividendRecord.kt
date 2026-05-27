package com.minepapa.kakaonotification.domain.model

data class DividendRecord(
    val date: String,           // YYYY-MM-DD
    val afterTaxAmount: Long,   // 세후금액 (원)
    val stockName: String,
    val receivedTime: String,   // HH:mm:ss
) {
    // 동일 시각·다른 금액의 별개 건을 구분하는 복합키
    val uniqueKey: String get() = "${receivedTime}_$afterTaxAmount"
}
