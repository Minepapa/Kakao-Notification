package com.minepapa.kakaonotification.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minepapa.kakaonotification.data.remote.auth.FirebaseAuthProvider
import com.minepapa.kakaonotification.domain.usecase.SyncToFirestoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authProvider: FirebaseAuthProvider,
    private val syncToFirestoreUseCase: SyncToFirestoreUseCase,
) : ViewModel() {

    // Firebase Auth 로그인 상태는 앱 재개 시마다 재확인(resume에서 호출)
    private val _isSignedIn = MutableStateFlow(authProvider.isSignedIn())
    val isSignedIn = _isSignedIn.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus = _syncStatus.asStateFlow()

    fun refreshSignInState() {
        _isSignedIn.value = authProvider.isSignedIn()
    }

    fun signOut() {
        authProvider.signOut()
        _isSignedIn.value = false
    }

    fun syncNow() {
        viewModelScope.launch {
            _syncStatus.value = "동기화 중..."
            syncToFirestoreUseCase().fold(
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
