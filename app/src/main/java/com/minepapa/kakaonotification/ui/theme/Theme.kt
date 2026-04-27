package com.minepapa.kakaonotification.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary       = KakaoBrown,
    secondary     = KakaoYellow,
    tertiary      = Purple40,
)

private val DarkColors = darkColorScheme(
    primary       = KakaoYellow,
    secondary     = KakaoBrown,
    tertiary      = Pink80,
)

@Composable
fun KakaoNotificationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography  = Typography,
        content     = content,
    )
}
