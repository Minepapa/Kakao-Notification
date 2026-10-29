package com.minepapa.kakaonotification.service

import android.app.Notification
import android.util.Log
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
import com.minepapa.kakaonotification.domain.model.NotificationRoute
import com.minepapa.kakaonotification.domain.usecase.MatchNotificationUseCase
import com.minepapa.kakaonotification.domain.usecase.SaveNotificationLogUseCase
import com.minepapa.kakaonotification.domain.usecase.SyncToFirestoreUseCase
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineExceptionHandler
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
        fun syncToFirestoreUseCase(): SyncToFirestoreUseCase
        fun appPreferences(): AppPreferences
        fun workManager(): WorkManager
    }

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, _ ->
            Log.w(TAG, "Notification capture failed")
        }
    )
    private val capturePolicy = NotificationCapturePolicy(KAKAO_PACKAGE)

    private val entryPoint: NlsEntryPoint by lazy {
        EntryPointAccessors.fromApplication(applicationContext, NlsEntryPoint::class.java)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        scope.launch {
            if (!entryPoint.appPreferences().listenerEnabled.first()) return@launch

            val capture = capturePolicy.route(
                sbn.packageName,
                entryPoint.appPreferences().selectedSpendingPackages.first(),
                (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0,
                (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT) != 0,
                (sbn.notification.flags and Notification.FLAG_FOREGROUND_SERVICE) != 0,
            ) ?: return@launch

            val extras = sbn.notification.extras
            val sender = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
            val messages = readMessages(extras)
            if (capture == NotificationCapturePolicy.Capture.KAKAO && capturePolicy.usesMessages(messages)) {
                if (sender.isNotBlank()) captureKakaoMessages(sbn, sender, messages)
                return@launch
            }
            val extractedBodies = extractBodies(extras)
            val bodies = if (capture == NotificationCapturePolicy.Capture.KAKAO) {
                extractedBodies
            } else {
                extractedBodies.takeIf { it.isNotEmpty() }?.let { listOf(it.joinToString("\n")) }.orEmpty()
            }
            if (!capturePolicy.hasContent(capture, sender, bodies)) return@launch
            val postedAt = capturePolicy.postedAt(sbn.notification.`when`, sbn.postTime)
            if (!capturePolicy.accept(sbn.key, postedAt, sender, bodies)) return@launch

            val appLabel = runCatching {
                packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(sbn.packageName, 0)
                ).toString()
            }.getOrDefault(sbn.packageName)

            var anyLogged = false
            for (body in if (bodies.isEmpty()) listOf("") else bodies) {
                val matchResult = if (capture == NotificationCapturePolicy.Capture.KAKAO) {
                    entryPoint.matchNotificationUseCase()(sender, body) ?: continue
                } else null
                entryPoint.saveNotificationLogUseCase()(
                    NotificationLog(
                        sender          = sender,
                        body            = body,
                        matchedKeywords = matchResult?.matchedKeywords.orEmpty(),
                        route           = matchResult?.rule?.route ?: NotificationRoute.SPENDING,
                        source          = if (capture == NotificationCapturePolicy.Capture.KAKAO) "kakao" else "app",
                        packageName     = sbn.packageName,
                        appLabel        = appLabel,
                        postedAt        = postedAt,
                    )
                ).getOrNull() ?: continue
                anyLogged = true
            }

            if (anyLogged) enqueueSyncWorker(entryPoint.workManager())
            else capturePolicy.forget(sbn.key, postedAt, sender, bodies)
        }
    }

    // 메시지마다 자기 시각을 postedAt으로 쓴다. Firestore 문서 ID가 본문·postedAt 해시라
    // 같은 메시지는 어느 갱신에서 읽어도 같은 문서가 된다(맥 원장도 이 ID로 중복을 거른다).
    private suspend fun captureKakaoMessages(
        sbn: StatusBarNotification,
        sender: String,
        messages: List<NotificationCapturePolicy.Message>,
    ) {
        val appLabel = runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(sbn.packageName, 0)
            ).toString()
        }.getOrDefault(sbn.packageName)
        val fallbackPostedAt = capturePolicy.postedAt(sbn.notification.`when`, sbn.postTime)

        var anyLogged = false
        for (message in capturePolicy.recentMessages(messages)) {
            if (!capturePolicy.acceptMessage(sbn.key, sender, message)) continue
            val matchResult = entryPoint.matchNotificationUseCase()(sender, message.text)
            if (matchResult == null) {
                capturePolicy.forgetMessage(sbn.key, sender, message)
                continue
            }
            val saved = entryPoint.saveNotificationLogUseCase()(
                NotificationLog(
                    sender          = sender,
                    body            = message.text,
                    matchedKeywords = matchResult.matchedKeywords,
                    route           = matchResult.rule.route,
                    source          = "kakao",
                    packageName     = sbn.packageName,
                    appLabel        = appLabel,
                    postedAt        = message.timeMs.takeIf { it > 0 } ?: fallbackPostedAt,
                )
            ).getOrNull()
            if (saved == null) {
                capturePolicy.forgetMessage(sbn.key, sender, message)
                continue
            }
            anyLogged = true
        }
        if (anyLogged) enqueueSyncWorker(entryPoint.workManager())
    }

    // MessagingStyle 메시지 묶음은 "text"(CharSequence)·"time"(Long) 키를 가진 Bundle이다.
    // API 수준과 무관하게 같게 읽으려고 직접 꺼낸다. 이력 메시지가 먼저, 오래된 순.
    private fun readMessages(extras: android.os.Bundle): List<NotificationCapturePolicy.Message> {
        fun read(key: String): List<NotificationCapturePolicy.Message> =
            runCatching { extras.getParcelableArray(key) }.getOrNull().orEmpty().mapNotNull { item ->
                val bundle = item as? android.os.Bundle ?: return@mapNotNull null
                val text = bundle.getCharSequence("text")?.toString() ?: return@mapNotNull null
                NotificationCapturePolicy.Message(text, bundle.getLong("time"))
            }
        return (read(Notification.EXTRA_HISTORIC_MESSAGES) + read(Notification.EXTRA_MESSAGES))
            .distinct().sortedBy { it.timeMs }
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
        val request = OneTimeWorkRequestBuilder<FirestoreSyncWorker>()
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
        private const val TAG = "KakaoNotificationListener"
        const val KAKAO_PACKAGE = "com.kakao.talk"
        const val SYNC_WORKER_TAG = "firestore_sync_worker"
    }
}
