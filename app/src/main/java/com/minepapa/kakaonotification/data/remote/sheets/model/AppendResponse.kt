package com.minepapa.kakaonotification.data.remote.sheets.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppendResponse(
    val spreadsheetId: String?,
    val tableRange: String?,
)
