package com.minepapa.kakaonotification.core.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.minepapa.kakaonotification.service.KakaoNotificationListenerService

object NotificationPermissionHelper {

    fun isGranted(context: Context): Boolean {
        val flat = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ) ?: return false
        val componentName = ComponentName(context, KakaoNotificationListenerService::class.java)
        return flat.contains(componentName.flattenToString())
    }

    fun openSettings(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
