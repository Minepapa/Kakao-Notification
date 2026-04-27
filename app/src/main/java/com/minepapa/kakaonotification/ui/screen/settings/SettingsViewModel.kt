package com.minepapa.kakaonotification.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import com.minepapa.kakaonotification.data.remote.auth.GoogleOAuthProvider
import com.minepapa.kakaonotification.domain.usecase.SyncToSheetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val oauthProvider: GoogleOAuthProvider,
    private val syncToSheetsUseCase: SyncToSheetsUseCase,
) : ViewModel() {

    val spreadsheetId = prefs.spreadsheetId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val sheetName = prefs.sheetName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Notifications")

    val isSignedIn = prefs.oauthToken
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus = _syncStatus.asStateFlow()

    fun setSpreadsheetId(id: String) {
        viewModelScope.launch { prefs.setSpreadsheetId(id) }
    }

    fun setSheetName(name: String) {
        viewModelScope.launch { prefs.setSheetName(name) }
    }

    /** Google Sign-In 완료 후 SettingsScreen에서 액세스 토큰을 전달받아 저장한다. */
    fun onGoogleSignInSuccess(accessToken: String) {
        viewModelScope.launch { oauthProvider.setToken(accessToken) }
    }

    fun signOut() {
        viewModelScope.launch { oauthProvider.setToken(null) }
    }

    fun syncNow() {
        viewModelScope.launch {
            _syncStatus.value = "동기화 중..."
            syncToSheetsUseCase().fold(
                onSuccess = { _syncStatus.value = "동기화 완료" },
                onFailure = { _syncStatus.value = "오류: ${it.message}" },
            )
        }
    }
}
