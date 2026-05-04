package com.minepapa.kakaonotification.ui.screen.log

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minepapa.kakaonotification.R
import com.minepapa.kakaonotification.ui.component.NotificationLogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationLogScreen(
    onBack: () -> Unit,
    viewModel: NotificationLogViewModel = hiltViewModel(),
) {
    val logs          by viewModel.logs.collectAsStateWithLifecycle()
    val selectionMode by viewModel.selectionMode.collectAsStateWithLifecycle()
    val selectedIds   by viewModel.selectedIds.collectAsStateWithLifecycle()

    BackHandler(enabled = selectionMode) { viewModel.exitSelectionMode() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (selectionMode)
                        Text("${selectedIds.size}개 선택됨")
                    else
                        Text(stringResource(R.string.nav_log))
                },
                navigationIcon = {
                    IconButton(onClick = if (selectionMode) viewModel::exitSelectionMode else onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    if (selectionMode) {
                        val allSelected = selectedIds.size == logs.size && logs.isNotEmpty()
                        TextButton(onClick = viewModel::selectAll) {
                            Text(if (allSelected) "전체 해제" else "전체 선택")
                        }
                        IconButton(
                            onClick = viewModel::deleteSelected,
                            enabled = selectedIds.isNotEmpty(),
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = if (selectedIds.isNotEmpty())
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                            )
                        }
                    } else {
                        IconButton(onClick = viewModel::enterSelectionMode) {
                            Icon(Icons.Default.Delete, contentDescription = "삭제 모드")
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier        = Modifier.fillMaxSize(),
            contentPadding  = PaddingValues(
                top    = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 8.dp,
                start  = 16.dp,
                end    = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(logs, key = { it.id }) { log ->
                NotificationLogItem(
                    log           = log,
                    selectionMode = selectionMode,
                    isSelected    = log.id in selectedIds,
                    onToggleSelect = { viewModel.toggleSelection(log.id) },
                )
            }
        }
    }
}
