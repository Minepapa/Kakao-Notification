package com.minepapa.kakaonotification.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minepapa.kakaonotification.domain.model.FilterRule

@Entity(tableName = "filter_rules")
data class FilterRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val keywords: String,
    val isEnabled: Boolean,
    val createdAt: Long,
) {
    fun toDomain() = FilterRule(
        id = id,
        senderName = senderName,
        keywords = if (keywords.isBlank()) emptyList() else keywords.split(",").map { it.trim() },
        isEnabled = isEnabled,
        createdAt = createdAt,
    )

    companion object {
        fun fromDomain(rule: FilterRule) = FilterRuleEntity(
            id = rule.id,
            senderName = rule.senderName,
            keywords = rule.keywords.joinToString(","),
            isEnabled = rule.isEnabled,
            createdAt = rule.createdAt,
        )
    }
}
