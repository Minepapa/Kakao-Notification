package com.minepapa.kakaonotification.ui.screen.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.minepapa.kakaonotification.R
import com.minepapa.kakaonotification.core.util.parseKeywords
import com.minepapa.kakaonotification.core.util.toKeywordsString
import com.minepapa.kakaonotification.domain.model.FilterRule
import com.minepapa.kakaonotification.domain.model.NotificationRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleScreen(
    ruleId: Long?,
    onBack: () -> Unit,
    viewModel: FilterRulesViewModel = hiltViewModel(),
) {
    var senderName   by rememberSaveable { mutableStateOf("") }
    var keywordsText by rememberSaveable { mutableStateOf("") }
    var route by rememberSaveable { mutableStateOf(NotificationRoute.INVESTING) }
    var existingRule by remember { mutableStateOf<FilterRule?>(null) }

    // 수정 모드: 기존 규칙 값을 불러와 필드에 채운다
    LaunchedEffect(ruleId) {
        if (ruleId != null) {
            viewModel.getRuleById(ruleId)?.let { rule ->
                existingRule = rule
                senderName   = rule.senderName
                keywordsText = rule.keywords.toKeywordsString()
                route = rule.route
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (ruleId == null) stringResource(R.string.rules_add) else "규칙 수정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value         = senderName,
                onValueChange = { senderName = it },
                label         = { Text(stringResource(R.string.rules_sender_hint)) },
                modifier      = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value         = keywordsText,
                onValueChange = { keywordsText = it },
                label         = { Text(stringResource(R.string.rules_keywords_hint)) },
                modifier      = Modifier.fillMaxWidth(),
            )
            Text("행선지")
            Row {
                listOf(
                    NotificationRoute.INVESTING to "투자",
                    NotificationRoute.SPENDING to "소비",
                ).forEach { (value, label) ->
                    RadioButton(selected = route == value, onClick = { route = value })
                    Text(label, modifier = Modifier.padding(end = 16.dp, top = 12.dp))
                }
            }
            Button(
                onClick = {
                    viewModel.addRule(
                        (existingRule ?: FilterRule()).copy(
                            senderName = senderName.trim(),
                            keywords = keywordsText.parseKeywords(),
                            route = route,
                        )
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled  = (ruleId == null || existingRule != null) &&
                    (senderName.isNotBlank() || keywordsText.isNotBlank()),
            ) {
                Text(stringResource(R.string.rules_save))
            }
        }
    }
}
