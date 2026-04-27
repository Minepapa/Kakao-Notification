package com.minepapa.kakaonotification.data.remote.sheets

import com.minepapa.kakaonotification.data.remote.auth.GoogleOAuthProvider
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class SheetsAuthInterceptor @Inject constructor(
    private val authProvider: GoogleOAuthProvider,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { authProvider.getAccessToken() }
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
