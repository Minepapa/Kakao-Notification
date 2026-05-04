package com.minepapa.kakaonotification.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.usecase.MatchNotificationUseCase
import com.minepapa.kakaonotification.domain.usecase.SaveNotificationLogUseCase
import com.minepapa.kakaonotification.domain.usecase.SyncToSheetsUseCase
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class KakaoNotificationListenerService : NotificationListenerService() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface NlsEntryPoint {
        fun matchNotificationUseCase(): MatchNotificationUseCase
        fun saveNotificationLogUseCase(): SaveNotificationLogUseCase
        fun syncToSheetsUseCase(): SyncToSheetsUseCase
        fun appPreferences(): AppPreferences
        fun workManager(): WorkManager
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val entryPoint: NlsEntryPoint by lazy {
        EntryPointAccessors.fromApplication(applicationContext, NlsEntryPoint::class.java)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != KAKAO_PACKAGE) return

        scope.launch {
            if (!entryPoint.appPreferences().listenerEnabled.first()) return@launch

            val extras = sbn.notification.extras
            val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
                ?: return@launch

            val bodies = extractBodies(extras)
            if (bodies.isEmpty()) return@launch

            var anyLogged = false
            for (body in bodies) {
                val matchResult = entryPoint.matchNotificationUseCase()(sender, body) ?: continue
                entryPoint.saveNotificationLogUseCase()(
                    NotificationLog(
                        sender          = sender,
                        body            = body,
                        matchedKeywords = matchResult.matchedKeywords,
                    )
                ).getOrNull() ?: continue
                anyLogged = true
            }

            if (anyLogged) enqueueSyncWorker(entryPoint.workManager())
        }
    }

    private fun extractBodies(extras: android.os.Bundle): List<String> {
        // BigTextStyle — 단일 메시지 (가장 일반적인 경우)
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        if (!bigText.isNullOrBlank()) return listOf(bigText)

        // InboxStyle — 동시에 여러 메시지가 도착했을 때 KakaoTalk이 묶어서 전달
        val textLines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
        if (!textLines.isNullOrEmpty()) {
            val lines = textLines.mapNotNull { it?.toString()?.takeIf { t -> t.isNotBlank() } }
            if (lines.isNotEmpty()) return lines
        }

        // Fallback: EXTRA_TEXT (단순 텍스트 알림)
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        return if (!text.isNullOrBlank()) listOf(text) else emptyList()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun enqueueSyncWorker(workManager: WorkManager) {
        val request = OneTimeWorkRequestBuilder<SheetsSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .addTag(SYNC_WORKER_TAG)
            .build()

        workManager.enqueueUniqueWork(
            SYNC_WORKER_TAG,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request,
        )
    }

    companion object {
        const val KAKAO_PACKAGE = "com.kakao.talk"
        const val SYNC_WORKER_TAG = "sheets_sync_worker"
    }
}
