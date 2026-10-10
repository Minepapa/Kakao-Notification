package com.minepapa.kakaonotification.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minepapa.kakaonotification.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onGoogleSignIn: () -> Unit,
    onSelectSpendingApps: () -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val isSignedIn    by viewModel.isSignedIn.collectAsStateWithLifecycle()
    val syncStatus    by viewModel.syncStatus.collectAsStateWithLifecycle()
    val selectedSpendingPackages by viewModel.selectedSpendingPackages.collectAsStateWithLifecycle()

    // Sign-In 화면에서 돌아올 때 상태 재확인
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshSignInState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Google 계정 연결(Firebase Auth — Firestore kakaoInbox 쓰기 권한에 필요)
                    if (isSignedIn) {
                        OutlinedButton(
                            onClick  = viewModel::signOut,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.settings_google_signout))
                        }
                    } else {
                        Button(
                            onClick  = onGoogleSignIn,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.settings_google_signin))
                        }
                    }

                    Button(
                        onClick  = viewModel::syncNow,
                        modifier = Modifier.fillMaxWidth(),
                        enabled  = isSignedIn,
                    ) {
                        Text(stringResource(R.string.settings_sync_now))
                    }

                    if (syncStatus != null) {
                        Text(
                            text  = syncStatus!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (syncStatus!!.startsWith("오류"))
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary,
                        )
                    }

                    ListItem(
                        headlineContent = { Text("소비 알림 수집 앱") },
                        supportingContent = { Text("${selectedSpendingPackages.size}개 선택됨") },
                        trailingContent = {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        },
                        modifier = Modifier.clickable(onClick = onSelectSpendingApps),
                    )
                }
            }
            item {
                Column {
                    // 앱 제작일(빌드 시각)·내려받기 한 날짜(실제 폰 설치/재설치 시각) — 재빌드
                    // 후 Android Studio Run으로 재설치했을 때 실제로 반영됐는지 두 값을 눈으로
                    // 대조해 확인하기 위한 용도.
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text  = stringResource(R.string.settings_build_date, viewModel.buildDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text  = stringResource(R.string.settings_install_date, viewModel.installDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
