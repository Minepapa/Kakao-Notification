package com.minepapa.kakaonotification.core.util

object Constants {
    const val KAKAO_PACKAGE_NAME   = "com.kakao.talk"
    const val SYNC_WORKER_TAG      = "firestore_sync_worker"

    // banana-portfolio-v2가 읽는 Firestore 컬렉션 — 2026-08-22, 구글시트 "알람" 탭
    // 릴레이(ADR 0005)를 대체(ADR 0014). scripts/lib/kakao-inbox.mjs와 이름을 맞춘다.
    const val FIRESTORE_COLLECTION = "kakaoInbox"
}
