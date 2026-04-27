package com.minepapa.kakaonotification.data.remote.auth

import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OAuth 액세스 토큰을 DataStore에서 읽어 제공한다.
 * 토큰 발급/갱신은 SettingsViewModel에서 Google Sign-In 후 [setToken]으로 저장한다.
 */
@Singleton
class GoogleOAuthProvider @Inject constructor(
    private val prefs: AppPreferences,
) {
    suspend fun getAccessToken(): String? = prefs.oauthToken.first()

    suspend fun setToken(token: String?) {
        prefs.setOauthToken(token)
    }

    suspend fun isSignedIn(): Boolean = getAccessToken() != null
}
