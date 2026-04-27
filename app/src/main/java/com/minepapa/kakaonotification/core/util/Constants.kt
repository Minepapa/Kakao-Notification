package com.minepapa.kakaonotification.core.util

object Constants {
    const val KAKAO_PACKAGE_NAME = "com.kakao.talk"
    const val SYNC_WORKER_TAG    = "sheets_sync_worker"
    const val SHEETS_BASE_URL    = "https://sheets.googleapis.com/"
    const val DEFAULT_SHEET_NAME = "Notifications"

    // Google OAuth scope for Sheets read/write
    const val SHEETS_SCOPE = "https://www.googleapis.com/auth/spreadsheets"
}
