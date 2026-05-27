package com.minepapa.kakaonotification.ui.screen.settings

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
    onBack: () -> Unit,
    onNavigateToDividendList: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val spreadsheetId by viewModel.spreadsheetId.collectAsStateWithLifecycle()
    val sheetName     by viewModel.sheetName.collectAsStateWithLifecycle()
    val isSignedIn    by viewModel.isSignedIn.collectAsStateWithLifecycle()
    val syncStatus    by viewModel.syncStatus.collectAsStateWithLifecycle()

    // Sign-In 화면에서 돌아올 때 상태 재확인
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.refreshSignInState()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var spreadsheetIdInput by rememberSaveable(spreadsheetId) { mutableStateOf(spreadsheetId) }
    var sheetNameInput     by rememberSaveable(sheetName)     { mutableStateOf(sheetName) }

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
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Google 계정 연결
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

            OutlinedTextField(
                value         = spreadsheetIdInput,
                onValueChange = {
                    spreadsheetIdInput = it
                    viewModel.setSpreadsheetId(it)
                },
                label        = { Text(stringResource(R.string.settings_spreadsheet_id)) },
                modifier     = Modifier.fillMaxWidth(),
                singleLine   = true,
            )

            OutlinedTextField(
                value         = sheetNameInput,
                onValueChange = {
                    sheetNameInput = it
                    viewModel.setSheetName(it)
                },
                label      = { Text(stringResource(R.string.settings_sheet_name)) },
                modifier   = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Button(
                onClick  = viewModel::syncNow,
                modifier = Modifier.fillMaxWidth(),
                enabled  = isSignedIn,
            ) {
                Text(stringResource(R.string.settings_sync_now))
            }

            OutlinedButton(
                onClick  = viewModel::syncExecutionHistory,
                modifier = Modifier.fillMaxWidth(),
                enabled  = isSignedIn,
            ) {
                Text(stringResource(R.string.settings_sync_execution_history))
            }

            OutlinedButton(
                onClick  = viewModel::syncDividend,
                modifier = Modifier.fillMaxWidth(),
                enabled  = isSignedIn,
            ) {
                Text("배당금 재동기화")
            }

            OutlinedButton(
                onClick  = onNavigateToDividendList,
                modifier = Modifier.fillMaxWidth(),
                enabled  = isSignedIn,
            ) {
                Text("배당금 내역 관리")
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
        }
    }
}
