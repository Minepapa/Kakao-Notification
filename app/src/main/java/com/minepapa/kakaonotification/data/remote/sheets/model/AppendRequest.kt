package com.minepapa.kakaonotification.data.remote.sheets.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppendRequest(
    val values: List<List<String>>,
)
