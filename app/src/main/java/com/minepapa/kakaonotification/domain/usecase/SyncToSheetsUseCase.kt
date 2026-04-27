package com.minepapa.kakaonotification.domain.usecase

import com.minepapa.kakaonotification.data.local.preferences.AppPreferences
import com.minepapa.kakaonotification.domain.repository.NotificationLogRepository
import com.minepapa.kakaonotification.domain.repository.SheetsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncToSheetsUseCase @Inject constructor(
    private val notificationLogRepo: NotificationLogRepository,
    private val sheetsRepo: SheetsRepository,
    private val prefs: AppPreferences,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val spreadsheetId = prefs.spreadsheetId.first()
        val sheetName     = prefs.sheetName.first()

        val unsynced = notificationLogRepo.getUnsynced().first()
        if (unsynced.isEmpty()) return@runCatching

        sheetsRepo.appendRows(spreadsheetId, sheetName, unsynced).getOrThrow()

        val now = System.currentTimeMillis()
        notificationLogRepo.markSynced(unsynced.map { it.id }, now)
    }
}
