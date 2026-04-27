package com.minepapa.kakaonotification.core.util

object Constants {
    const val KAKAO_PACKAGE_NAME   = "com.kakao.talk"
    const val SYNC_WORKER_TAG      = "sheets_sync_worker"
    const val SHEETS_BASE_URL      = "https://sheets.googleapis.com/"
    const val SHEETS_SCOPE         = "https://www.googleapis.com/auth/spreadsheets"

    // 기존 포트폴리오 스프레드시트 (banana-portfolio 프로젝트와 동일)
    const val DEFAULT_SPREADSHEET_ID = "1ANhZyJUm51T8HfvQ56sK-Xrli9IViKmKG462l9rLKeg"
    // 카카오 알림 전용 탭 이름 — 스프레드시트에 "Notification" 탭 생성 완료
    const val DEFAULT_SHEET_NAME     = "Notification"
}
