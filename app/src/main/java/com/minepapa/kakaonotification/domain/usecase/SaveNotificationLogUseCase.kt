package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import javax.inject.Inject

class SaveNotificationLogUseCase @Inject constructor(
    private val repo: NotificationLogRepository,
) {
    suspend operator fun invoke(log: NotificationLog): Result<Long> = runCatching {
        repo.insert(log)
    }
}
