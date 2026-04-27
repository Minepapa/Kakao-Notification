package com.minepapa.kakaonotification.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toFormattedDateTime(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(this))

fun String.parseKeywords(): List<String> =
    split(",").map { it.trim() }.filter { it.isNotBlank() }

fun List<String>.toKeywordsString(): String = joinToString(", ")
