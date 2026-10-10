package com.minepapa.kakaonotification.ui.screen.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SpendingAppListTest {
    @Test
    fun excludesOwnAppAndKakaoTalkFromInstalledAndSelectedApps() {
        val apps = buildSpendingAppList(
            installed = listOf(
                SpendingApp("com.example.self", "Self"),
                SpendingApp("com.kakao.talk", "KakaoTalk"),
                SpendingApp("com.example.bank", "Bank"),
            ),
            selectedPackages = setOf("com.example.self", "com.kakao.talk", "com.example.bank"),
            ownPackageName = "com.example.self",
        )

        assertEquals(listOf("com.example.bank"), apps.map { it.packageName })
    }

    @Test
    fun includesUninstalledSelectedAppSoItCanBeDeselected() {
        val apps = buildSpendingAppList(
            installed = listOf(SpendingApp("com.example.bank", "Bank")),
            selectedPackages = setOf("com.example.missing", "com.example.bank"),
            ownPackageName = "com.example.self",
        )

        assertEquals(listOf("com.example.bank", "com.example.missing"), apps.map { it.packageName })
        assertFalse(apps.single { it.packageName == "com.example.missing" }.isInstalled)
        assertEquals("com.example.missing", apps.single { !it.isInstalled }.label)
    }

    @Test
    fun selectedInstalledAppAppearsOnlyOnce() {
        val apps = buildSpendingAppList(
            installed = listOf(
                SpendingApp("com.example.bank", "Bank"),
                SpendingApp("com.example.bank", "Bank duplicate"),
            ),
            selectedPackages = setOf("com.example.bank"),
            ownPackageName = "com.example.self",
        )

        assertEquals(1, apps.size)
        assertEquals("Bank", apps.single().label)
    }
}
