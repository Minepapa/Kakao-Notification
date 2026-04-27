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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minepapa.kakaonotification.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val spreadsheetId by viewModel.spreadsheetId.collectAsStateWithLifecycle()
    val sheetName     by viewModel.sheetName.collectAsStateWithLifecycle()
    val isSignedIn    by viewModel.isSignedIn.collectAsStateWithLifecycle()
    val syncStatus    by viewModel.syncStatus.collectAsStateWithLifecycle()

    var spreadsheetIdInput by rememberSaveable(spreadsheetId) { mutableStateOf(spreadsheetId ?: "") }
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
            // Google Sign-In
            if (isSignedIn != null) {
                OutlinedButton(
                    onClick  = viewModel::signOut,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.settings_google_signout))
                }
            } else {
                Button(
                    // 실제 Sign-In은 Activity에서 처리 후 onGoogleSignInSuccess 호출
                    onClick  = { /* Google Sign-In Intent는 Activity에서 실행 */ },
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
                label    = { Text(stringResource(R.string.settings_spreadsheet_id)) },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value         = sheetNameInput,
                onValueChange = {
                    sheetNameInput = it
                    viewModel.setSheetName(it)
                },
                label    = { Text(stringResource(R.string.settings_sheet_name)) },
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick  = viewModel::syncNow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_sync_now))
            }

            if (syncStatus != null) {
                Text(text = syncStatus!!)
            }
        }
    }
}
