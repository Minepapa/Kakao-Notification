package com.minepapa.kakaonotification.data.remote.sheets.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GetValuesResponse(
    val values: List<List<String>>?
)
