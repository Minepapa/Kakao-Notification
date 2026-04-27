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
            val sender = extras.getString(Notification.EXTRA_TITLE) ?: return@launch
            val body = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_TEXT))
                ?.toString() ?: return@launch

            val matchedRule = entryPoint.matchNotificationUseCase()(sender, body)
                ?: return@launch

            val logId = entryPoint.saveNotificationLogUseCase()(
                NotificationLog(
                    sender = sender,
                    body = body,
                    matchedRuleId = matchedRule.id,
                )
            ).getOrNull() ?: return@launch

            // 즉시 동기화 시도, 실패 시 WorkManager로 재시도
            val syncResult = entryPoint.syncToSheetsUseCase()()
            if (syncResult.isFailure) {
                enqueueSyncWorker(entryPoint.workManager())
            }
        }
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
