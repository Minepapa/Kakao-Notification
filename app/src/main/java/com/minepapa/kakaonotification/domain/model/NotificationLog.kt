package com.minepapa.kakaonotification.domain.model

data class NotificationLog(
    val id: Long = 0,
    val sender: String,
    val body: String,
    val matchedKeywords: List<String>,
    val receivedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
    val route: String = NotificationRoute.INVESTING,
    val source: String = "kakao",
    val packageName: String = "",
    val appLabel: String = "",
    val postedAt: Long = receivedAt,
)
