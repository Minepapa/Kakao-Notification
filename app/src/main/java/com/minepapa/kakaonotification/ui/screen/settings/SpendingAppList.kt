package com.minepapa.kakaonotification.ui.screen.settings

import java.util.Locale

data class SpendingApp(val packageName: String, val label: String, val isInstalled: Boolean = true)

internal fun buildSpendingAppList(
    installed: List<SpendingApp>,
    selectedPackages: Set<String>,
    ownPackageName: String,
): List<SpendingApp> {
    val excluded = setOf(ownPackageName, "com.kakao.talk")
    val installedApps = installed
        .filterNot { it.packageName in excluded }
        .distinctBy { it.packageName }
    val installedNames = installedApps.mapTo(mutableSetOf()) { it.packageName }
    val missingSelectedApps = selectedPackages
        .filterNot { it in excluded || it in installedNames }
        .map { SpendingApp(it, it, isInstalled = false) }
    return (installedApps + missingSelectedApps)
        .sortedWith(compareBy<SpendingApp> { it.label.lowercase(Locale.getDefault()) }.thenBy { it.packageName })
}
