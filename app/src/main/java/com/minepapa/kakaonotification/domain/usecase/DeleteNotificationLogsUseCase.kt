package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import javax.inject.Inject

class DeleteNotificationLogsUseCase @Inject constructor(
    private val repo: NotificationLogRepository
) {
    suspend operator fun invoke(ids: List<Long>) = repo.deleteLogs(ids)
}
