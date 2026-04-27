package com.minepapa.kakaonotification.domain.model

data class FilterRule(
    val id: Long = 0,
    val senderName: String = "",
    val keywords: List<String> = emptyList(),
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
)
