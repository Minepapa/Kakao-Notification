package com.minepapa.kakaonotification.ui.screen.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import com.minepapa.kakaonotification.data.remote.auth.GoogleOAuthProvider
import com.minepapa.kakaonotification.domain.usecase.SyncToSheetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AppPreferences,
    private val oauthProvider: GoogleOAuthProvider,
    private val syncToSheetsUseCase: SyncToSheetsUseCase,
) : ViewModel() {

    val spreadsheetId = prefs.spreadsheetId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val sheetName = prefs.sheetName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    // Google Sign-In 상태는 앱 재개 시마다 재확인 (resume에서 호출)
    private val _isSignedIn = MutableStateFlow(oauthProvider.isSignedIn())
    val isSignedIn = _isSignedIn.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus = _syncStatus.asStateFlow()

    fun refreshSignInState() {
        _isSignedIn.value = oauthProvider.isSignedIn()
    }

    fun setSpreadsheetId(id: String) {
        viewModelScope.launch { prefs.setSpreadsheetId(id) }
    }

    fun setSheetName(name: String) {
        viewModelScope.launch { prefs.setSheetName(name) }
    }

    fun signOut() {
        oauthProvider.signOut()
        _isSignedIn.value = false
    }

    fun syncNow() {
        viewModelScope.launch {
            _syncStatus.value = "동기화 중..."
            syncToSheetsUseCase().fold(
                onSuccess = {
                    _syncStatus.value = "동기화 완료"
                    delay(2000) // 2초 후 메시지 초기화
                    _syncStatus.value = null
                },
                onFailure = {
                    _syncStatus.value = "오류: ${it.message}"
                    delay(5000) // 5초 후 메시지 초기화
                    _syncStatus.value = null
                }
            )
        }
    }
}
