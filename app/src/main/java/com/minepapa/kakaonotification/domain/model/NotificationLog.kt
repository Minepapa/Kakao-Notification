package com.minepapa.kakaonotification.domain.model

data class NotificationLog(
    val id: Long = 0,
    val sender: String,
    val body: String,
    val matchedRuleId: Long?,
    val receivedAt: Long = System.currentTimeMillis(),
    val syncedAt: Long? = null,
)
