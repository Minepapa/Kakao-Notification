package com.minepapa.kakaonotification.ui.screen.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minepapa.kakaonotification.BuildConfig
import com.minepapa.kakaonotification.data.remote.auth.FirebaseAuthProvider
import com.minepapa.kakaonotification.domain.usecase.SyncToFirestoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authProvider: FirebaseAuthProvider,
    private val syncToFirestoreUseCase: SyncToFirestoreUseCase,
) : ViewModel() {

    // Firebase Auth 로그인 상태는 앱 재개 시마다 재확인(resume에서 호출)
    private val _isSignedIn = MutableStateFlow(authProvider.isSignedIn())
    val isSignedIn = _isSignedIn.asStateFlow()

    private val _syncStatus = MutableStateFlow<String?>(null)
    val syncStatus = _syncStatus.asStateFlow()

    // 앱 제작일 = Gradle이 이 APK를 빌드한 시각(app/build.gradle.kts에서 굽는 상수, 빌드마다 갱신).
    val buildDate: String = BuildConfig.BUILD_TIME

    // 내려받기(설치) 일 = PackageManager의 lastUpdateTime — Android Studio에서 Run으로
    // 재설치할 때마다 갱신되는 값이라(firstInstallTime과 달리, 최초 설치 이후에도 매번
    // 바뀜) "방금 새로 만든 빌드가 실제로 폰에 들어갔는지" 확인하려는 목적에 정확히
    // 맞는다 — buildDate와 나란히 놓고 두 값이 가까우면 재설치가 실제로 반영된 것.
    val installDate: String = formatInstallDate()

    private fun formatInstallDate(): String {
        val lastUpdateTime = context.packageManager
            .getPackageInfo(context.packageName, 0)
            .lastUpdateTime
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        return formatter.format(Date(lastUpdateTime))
    }

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
