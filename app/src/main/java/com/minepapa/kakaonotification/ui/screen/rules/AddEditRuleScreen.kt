package com.minepapa.kakaonotification.ui.screen.rules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleScreen(
    ruleId: Long?,
    onBack: () -> Unit,
    viewModel: FilterRulesViewModel = hiltViewModel(),
) {
    var senderName   by rememberSaveable { mutableStateOf("") }
    var keywordsText by rememberSaveable { mutableStateOf("") }

    // 수정 모드: 기존 규칙 값을 불러와 필드에 채운다
    LaunchedEffect(ruleId) {
        if (ruleId != null) {
            viewModel.getRuleById(ruleId)?.let { rule ->
                senderName   = rule.senderName
                keywordsText = rule.keywords.toKeywordsString()
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
            Button(
                onClick = {
                    viewModel.addRule(
                        FilterRule(
                            id         = ruleId ?: 0L,  // 0이면 신규, 기존 id면 덮어쓰기(REPLACE)
                            senderName = senderName.trim(),
                            keywords   = keywordsText.parseKeywords(),
                        )
                    )
                    onBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled  = senderName.isNotBlank() || keywordsText.isNotBlank(),
            ) {
                Text(stringResource(R.string.rules_save))
            }
        }
    }
}
