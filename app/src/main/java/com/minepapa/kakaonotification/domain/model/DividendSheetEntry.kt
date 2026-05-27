package com.minepapa.kakaonotification.domain.model

data class DividendSheetEntry(
    val rowNumber: Int,
    val date: String,
    val amount: Long,
    val stockName: String,
)
