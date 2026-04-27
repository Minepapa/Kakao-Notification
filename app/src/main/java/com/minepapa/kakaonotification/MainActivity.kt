package com.minepapa.kakaonotification

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.minepapa.kakaonotification.core.navigation.AppNavHost
import com.minepapa.kakaonotification.core.navigation.Screen
import com.minepapa.kakaonotification.data.remote.auth.GoogleOAuthProvider
import com.minepapa.kakaonotification.ui.theme.KakaoNotificationTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var oauthProvider: GoogleOAuthProvider

    // Sign-In 결과 수신 — SettingsScreen 버튼이 signInLauncher.launch()를 호출
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        runCatching { task.getResult(ApiException::class.java) }
            .onFailure { Log.e("GoogleSignIn", "Sign-in failed", it) }
        // 성공/실패 여부와 관계없이 SettingsViewModel이 isSignedIn()을 재확인한다
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KakaoNotificationTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val bottomNavItems = listOf(
                    Triple(Screen.Dashboard.route, Icons.Default.Home,     R.string.nav_dashboard),
                    Triple(Screen.Rules.route,     Icons.Default.List,     R.string.nav_rules),
                    Triple(Screen.Log.route,        Icons.Default.List,     R.string.nav_log),
                    Triple(Screen.Settings.route,   Icons.Default.Settings, R.string.nav_settings),
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            bottomNavItems.forEach { (route, icon, labelRes) ->
                                NavigationBarItem(
                                    selected = currentRoute == route,
                                    onClick  = {
                                        navController.navigate(route) {
                                            popUpTo(Screen.Dashboard.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState    = true
                                        }
                                    },
                                    icon  = { Icon(icon, contentDescription = null) },
                                    label = { Text(stringResource(labelRes)) },
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController  = navController,
                        onGoogleSignIn = { signInLauncher.launch(oauthProvider.getSignInIntent()) },
                        modifier       = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
