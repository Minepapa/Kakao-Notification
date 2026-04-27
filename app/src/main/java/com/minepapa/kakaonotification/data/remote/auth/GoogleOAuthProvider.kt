package com.minepapa.kakaonotification.data.remote.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.minepapa.kakaonotification.core.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleOAuthProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(Constants.SHEETS_SCOPE))
        .build()

    private val client get() = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent = client.signInIntent

    fun isSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return account.grantedScopes.any { it.scopeUri == Constants.SHEETS_SCOPE }
    }

    /**
     * Sheets API 호출 직전에 호출. GoogleAuthUtil이 만료된 토큰을 자동으로 갱신한다.
     * OkHttp 인터셉터의 runBlocking 안에서 호출되므로 IO 디스패처 사용.
     */
    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return@withContext null
        val googleAccount = account.account ?: return@withContext null
        runCatching {
            GoogleAuthUtil.getToken(
                context,
                googleAccount,
                "oauth2:${Constants.SHEETS_SCOPE}",
            )
        }.getOrNull()
    }

    fun signOut() {
        client.signOut()
    }
}
