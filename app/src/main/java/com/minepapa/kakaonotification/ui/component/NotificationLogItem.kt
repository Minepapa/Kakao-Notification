package com.minepapa.kakaonotification.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minepapa.kakaonotification.core.util.toFormattedDateTime
import com.minepapa.kakaonotification.domain.model.NotificationLog

private val KeywordBlue = Color(0xFF1976D2)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NotificationLogItem(
    log: NotificationLog,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(
                    text  = log.sender,
                    style = MaterialTheme.typography.titleSmall,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text  = if (log.syncedAt != null) "동기화됨" else "대기중",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (log.syncedAt != null)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                )
            }
            if (log.matchedKeywords.isNotEmpty()) {
                FlowRow(
                    modifier            = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    log.matchedKeywords.forEach { keyword ->
                        Text(
                            text  = "#$keyword",
                            style = MaterialTheme.typography.labelSmall,
                            color = KeywordBlue,
                        )
                    }
                }
            }
            Text(
                text     = log.body,
                style    = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text     = log.receivedAt.toFormattedDateTime(),
                style    = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
