package com.minepapa.kakaonotification.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minepapa.kakaonotification.R
import com.minepapa.kakaonotification.ui.component.PermissionBanner
import com.minepapa.kakaonotification.domain.usecase.RouteRuleSummary
import com.minepapa.kakaonotification.domain.usecase.hiddenRulesLine
import com.minepapa.kakaonotification.domain.usecase.selectedSpendingAppsLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToRules: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val ruleSummary by viewModel.ruleSummary.collectAsStateWithLifecycle()
    val selectedSpendingApps by viewModel.selectedSpendingApps.collectAsStateWithLifecycle()
    val isSignedIn by viewModel.isSignedIn.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshSignInState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (!viewModel.hasPermission) {
                PermissionBanner(onGrantClick = viewModel::openPermissionSettings)
            }

            Card(onClick = onNavigateToRules, modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("적용 중인 규칙", style = MaterialTheme.typography.titleMedium)
                    RouteSummary("투자", ruleSummary.investing)
                    RouteSummary("소비", ruleSummary.spending)
                    Text(
                        "비활성 규칙 ${ruleSummary.inactiveCount}개",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        selectedSpendingAppsLine(selectedSpendingApps),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Text(
                text  = if (isSignedIn) "Google 계정 연결됨" else "Google 계정 미연결 — 설정에서 연결해주세요",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSignedIn)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun RouteSummary(label: String, summary: RouteRuleSummary) {
    Text("$label ${summary.activeCount}개", style = MaterialTheme.typography.titleSmall)
    summary.lines.forEach { line ->
        Text(
            "• $line",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    if (summary.hiddenCount > 0) {
        Text(hiddenRulesLine(summary.hiddenCount), style = MaterialTheme.typography.bodySmall)
    }
}
