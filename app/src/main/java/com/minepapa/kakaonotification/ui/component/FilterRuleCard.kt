package com.minepapa.kakaonotification.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.minepapa.kakaonotification.domain.model.FilterRule

@Composable
fun FilterRuleCard(
    rule: FilterRule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit:   () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier              = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = rule.senderName.ifBlank { "전체 발신자" },
                    style = MaterialTheme.typography.titleSmall,
                )
                if (rule.keywords.isNotEmpty()) {
                    Text(
                        text  = "키워드: ${rule.keywords.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Switch(
                checked         = rule.isEnabled,
                onCheckedChange = onToggle,
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "수정")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "삭제")
            }
        }
    }
}
