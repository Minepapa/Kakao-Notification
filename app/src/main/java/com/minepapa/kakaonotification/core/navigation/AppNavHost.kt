package com.minepapa.kakaonotification.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.minepapa.kakaonotification.ui.screen.dashboard.DashboardScreen
import com.minepapa.kakaonotification.ui.screen.dividend.DividendListScreen
import com.minepapa.kakaonotification.ui.screen.log.NotificationLogScreen
import com.minepapa.kakaonotification.ui.screen.rules.AddEditRuleScreen
import com.minepapa.kakaonotification.ui.screen.rules.FilterRulesScreen
import com.minepapa.kakaonotification.ui.screen.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    onGoogleSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController  = navController,
        startDestination = Screen.Dashboard.route,
        modifier       = modifier,
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToRules    = { navController.navigate(Screen.Rules.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
            )
        }
        composable(Screen.Rules.route) {
            FilterRulesScreen(
                onAddRule  = { navController.navigate(Screen.AddRule.route) },
                onEditRule = { id -> navController.navigate("rules/$id") },
                onBack     = { navController.popBackStack() },
            )
        }
        composable(Screen.AddRule.route) {
            AddEditRuleScreen(
                ruleId = null,
                onBack = { navController.popBackStack() },
            )
        }
        composable("rules/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toLongOrNull()
            AddEditRuleScreen(
                ruleId = id,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Screen.Log.route) {
            NotificationLogScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onGoogleSignIn          = onGoogleSignIn,
                onBack                  = { navController.popBackStack() },
                onNavigateToDividendList = { navController.navigate(Screen.DividendList.route) },
            )
        }
        composable(Screen.DividendList.route) {
            DividendListScreen(onBack = { navController.popBackStack() })
        }
    }
}
