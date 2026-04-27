package com.minepapa.kakaonotification.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.minepapa.kakaonotification.domain.usecase.SyncToSheetsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SheetsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncToSheetsUseCase: SyncToSheetsUseCase,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return syncToSheetsUseCase().fold(
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
