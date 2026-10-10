package com.minepapa.kakaonotification.data.repository

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.minepapa.kakaonotification.core.util.Constants
import com.minepapa.kakaonotification.domain.model.NotificationLog
import com.minepapa.kakaonotification.domain.model.NotificationRoute
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FirestoreRelayRepositoryImplTest {
    private val log = NotificationLog(
        sender = "가상 발신자",
        body = "가상 본문",
        matchedKeywords = emptyList(),
        receivedAt = 1700000001000L,
        route = NotificationRoute.SPENDING,
        source = "app",
        packageName = "example.synthetic.app",
        appLabel = "가상 앱",
        postedAt = 1700000000000L,
    )

    @Test
    fun spendingUploadSetsDeterministicDocumentWithMetadata() = runTest {
        val firestore = mockk<FirebaseFirestore>()
        val collection = mockk<CollectionReference>()
        val document = mockk<DocumentReference>()
        val fields = slot<Any>()
        val completed = TaskCompletionSource<Void>().apply { setResult(null) }.task
        every { firestore.collection(Constants.SPENDING_FIRESTORE_COLLECTION) } returns collection
        every { collection.document(documentId(log)) } returns document
        every { document.set(capture(fields)) } returns completed

        assertTrue(FirestoreRelayRepositoryImpl(firestore).sendLogs(listOf(log)).isSuccess)

        @Suppress("UNCHECKED_CAST")
        val uploaded = fields.captured as Map<String, Any>
        assertEquals("2023-11-15 07:13:20", uploaded["ts"])
        assertEquals(log.source, uploaded["source"])
        assertEquals(log.packageName, uploaded["packageName"])
        assertEquals(log.appLabel, uploaded["appLabel"])
        assertEquals(log.sender, uploaded["sender"])
        assertEquals(log.body, uploaded["body"])
        assertEquals(log.postedAt, uploaded["postedAt"])
        assertTrue(uploaded["createdAt"] is FieldValue)
        assertEquals(8, uploaded.size)
        verify(exactly = 1) { collection.document(documentId(log)) }
        verify(exactly = 1) { document.set(any()) }
        verify(exactly = 0) { collection.add(any()) }
    }

    @Test
    fun kakaoUploadSetsDeterministicDocumentWithOriginalFields() = runTest {
        val investingLog = log.copy(route = NotificationRoute.INVESTING, source = "kakao")
        val firestore = mockk<FirebaseFirestore>()
        val collection = mockk<CollectionReference>()
        val document = mockk<DocumentReference>()
        val fields = slot<Any>()
        val completed = TaskCompletionSource<Void>().apply { setResult(null) }.task
        every { firestore.collection(Constants.FIRESTORE_COLLECTION) } returns collection
        every { collection.document(documentId(investingLog)) } returns document
        every { document.set(capture(fields)) } returns completed

        assertTrue(FirestoreRelayRepositoryImpl(firestore).sendLogs(listOf(investingLog)).isSuccess)

        @Suppress("UNCHECKED_CAST")
        val uploaded = fields.captured as Map<String, Any>
        assertEquals("2023-11-15 07:13:21", uploaded["ts"])
        assertEquals(investingLog.sender, uploaded["sender"])
        assertEquals(investingLog.body, uploaded["body"])
        assertTrue(uploaded["createdAt"] is FieldValue)
        assertEquals(setOf("ts", "sender", "body", "createdAt"), uploaded.keys)
        verify(exactly = 1) { collection.document(documentId(investingLog)) }
        verify(exactly = 1) { document.set(any()) }
        verify(exactly = 0) { collection.add(any()) }
    }
}
