package com.minepapa.kakaonotification.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.minepapa.kakaonotification.core.util.Constants
import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.repository.NotificationRelayRepository
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// 2026-08-22 — SheetsRepositoryImpl(구글시트 append)을 대체. 컬럼 순서 대신 필드명으로
// 쓴다는 점만 다르고, Mac 쪽(scripts/lib/kakao-inbox.mjs)이 보는 필드(ts·body)는 동일한
// 의미를 유지한다 — sender는 감사용으로 같이 남기되 Mac 파서는 안 읽는다(참고:
// notification-parsers.mjs는 시각·본문 두 필드만 본다).
class FirestoreRelayRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : NotificationRelayRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override suspend fun sendLogs(logs: List<NotificationLog>): Result<Unit> = runCatching {
        val collection = firestore.collection(Constants.FIRESTORE_COLLECTION)
        for (log in logs) {
            collection.add(
                mapOf(
                    "ts" to dateFormat.format(Date(log.receivedAt)),
                    "sender" to log.sender,
                    "body" to log.body,
                    "createdAt" to FieldValue.serverTimestamp(),
                )
            ).await()
        }
    }
}
