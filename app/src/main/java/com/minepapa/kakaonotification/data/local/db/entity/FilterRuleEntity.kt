package com.minepapa.kakaonotification.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.model.NotificationRoute

@Entity(tableName = "filter_rules")
data class FilterRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderName: String,
    val keywords: String,
    val isEnabled: Boolean,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "'investing'") val route: String = NotificationRoute.INVESTING,
) {
    fun toDomain() = FilterRule(
        id = id,
        senderName = senderName,
        keywords = if (keywords.isBlank()) emptyList() else keywords.split(",").map { it.trim() },
        isEnabled = isEnabled,
        createdAt = createdAt,
        route = route,
    )

    companion object {
        fun fromDomain(rule: FilterRule) = FilterRuleEntity(
            id = rule.id,
            senderName = rule.senderName,
            keywords = rule.keywords.joinToString(","),
            isEnabled = rule.isEnabled,
            createdAt = rule.createdAt,
            route = rule.route,
        )
    }
}
