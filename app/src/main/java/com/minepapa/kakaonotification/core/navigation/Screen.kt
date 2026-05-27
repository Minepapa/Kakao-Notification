package com.minepapa.kakaonotification.core.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Rules     : Screen("rules")
    object AddRule   : Screen("rules/add")
    object Log       : Screen("log")
    object Settings      : Screen("settings")
    object DividendList  : Screen("dividend-list")

    data class EditRule(val id: Long) : Screen("rules/$id")
}
