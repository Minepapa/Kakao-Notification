package com.minepapa.kakaonotification.data.remote.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.minepapa.kakaonotification.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// 2026-08-22 — GoogleOAuthProvider(Sheets API 스코프 전용 OAuth 액세스토큰)를 대체.
// Firestore는 Firebase Auth 신원으로 보안규칙을 판정하므로(firestore.rules의
// request.auth.token.email 검사), Sheets 스코프 대신 Google ID 토큰을 받아 Firebase
// Auth 자격증명으로 교환한다. R.string.default_web_client_id는 google-services 플러그인이
// google-services.json의 oauth_client(웹 클라이언트, type 3)에서 자동 생성한다.
@Singleton
class FirebaseAuthProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
) {
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    private val client get() = GoogleSignIn.getClient(context, gso)

    fun getSignInIntent(): Intent = client.signInIntent

    fun isSignedIn(): Boolean = auth.currentUser != null

    // signInLauncher 콜백(MainActivity)에서 호출 — Google ID 토큰을 Firebase 자격증명으로
    // 교환해 실제 로그인을 완료한다(이전엔 Google Sign-In 자체가 곧 "로그인 완료"였지만,
    // Firestore 보안규칙은 Firebase Auth 세션만 본다).
    suspend fun handleSignInResult(data: Intent?): Result<Unit> = runCatching {
        val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
        val idToken = account.idToken ?: error("Google ID 토큰을 받지 못함")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).await()
        Unit
    }

    fun signOut() {
        auth.signOut()
        client.signOut()
    }
}
