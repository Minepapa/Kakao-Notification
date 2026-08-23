package com.minepapa.kakaonotification.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.minepapa.kakaonotification.domain.usecase.SyncToFirestoreUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

// 2026-08-22 — SheetsSyncWorker를 대체.
@HiltWorker
class FirestoreSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncToFirestoreUseCase: SyncToFirestoreUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return syncToFirestoreUseCase().fold(
            onSuccess = { Result.success() },
            onFailure = {
                if (runAttemptCount < MAX_RETRY) Result.retry() else Result.failure()
            },
        )
    }

    companion object {
        private const val MAX_RETRY = 3
    }
}
