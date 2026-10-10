package com.minepapa.kakaonotification.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.minepapa.kakaonotification.core.util.Constants
import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.model.NotificationRoute
import com.minepapa.kakaonotification.domain.repository.NotificationRelayRepository
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

// 2026-08-22 — SheetsRepositoryImpl(구글시트 append)을 대체. 컬럼 순서 대신 필드명으로
// 쓴다는 점만 다르고, Mac 쪽(scripts/lib/kakao-inbox.mjs)이 보는 필드(ts·body)는 동일한
// 의미를 유지한다 — sender는 감사용으로 같이 남기되 Mac 파서는 안 읽는다(참고:
// notification-parsers.mjs는 시각·본문 두 필드만 본다).
class FirestoreRelayRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : NotificationRelayRepository {

    override suspend fun sendLogs(logs: List<NotificationLog>): Result<Unit> = runCatching {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).apply {
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        for (log in logs) {
            val spending = log.route == NotificationRoute.SPENDING
            val collection = firestore.collection(
                if (spending) Constants.SPENDING_FIRESTORE_COLLECTION else Constants.FIRESTORE_COLLECTION
            )
            val fields = if (spending) {
                mapOf(
                    "ts" to dateFormat.format(Date(log.postedAt)),
                    "source" to log.source,
                    "packageName" to log.packageName,
                    "appLabel" to log.appLabel,
                    "sender" to log.sender,
                    "body" to log.body,
                    "postedAt" to log.postedAt,
                    "createdAt" to FieldValue.serverTimestamp(),
                )
            } else {
                mapOf(
                    "ts" to dateFormat.format(Date(log.receivedAt)),
                    "sender" to log.sender,
                    "body" to log.body,
                    "createdAt" to FieldValue.serverTimestamp(),
                )
            }
            collection.document(documentId(log)).set(fields).await()
        }
    }
}

internal fun documentId(log: NotificationLog): String {
    val digest = MessageDigest.getInstance("SHA-256")
    listOf(log.route, log.source, log.packageName, log.sender, log.body, log.postedAt.toString())
        .forEach { value ->
            val bytes = value.toByteArray(Charsets.UTF_8)
            val size = bytes.size
            digest.update(byteArrayOf(
                (size ushr 24).toByte(),
                (size ushr 16).toByte(),
                (size ushr 8).toByte(),
                size.toByte(),
            ))
            digest.update(bytes)
        }
    val hex = "0123456789abcdef"
    return buildString(32) {
        digest.digest().take(16).forEach { byte ->
            val value = byte.toInt() and 0xff
            append(hex[value ushr 4])
            append(hex[value and 0x0f])
        }
    }
}
