package com.minepapa.kakaonotification.service

/** NotificationListenerService에서 Android 객체를 읽은 뒤 적용하는 수집 기준. */
internal class NotificationCapturePolicy(private val kakaoPackage: String) {
    enum class Capture { KAKAO, SPENDING_APP }

    /** MessagingStyle 메시지 하나 — 본문과 그 메시지 자체의 시각. */
    data class Message(val text: String, val timeMs: Long)

    private val recent = object : LinkedHashMap<String, String>(MAX_RECENT, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>): Boolean =
            size > MAX_RECENT
    }

    fun route(
        packageName: String,
        selectedPackages: Set<String>,
        isGroupSummary: Boolean,
        isOngoingEvent: Boolean,
        isForegroundService: Boolean,
    ): Capture? {
        if (isGroupSummary) return null
        return when {
            packageName == kakaoPackage -> Capture.KAKAO
            packageName in selectedPackages && !isOngoingEvent && !isForegroundService -> Capture.SPENDING_APP
            else -> null
        }
    }

    fun hasContent(capture: Capture, title: String, bodies: List<String>): Boolean =
        when (capture) {
            Capture.KAKAO -> title.isNotBlank() && bodies.isNotEmpty()
            Capture.SPENDING_APP -> title.isNotBlank() || bodies.any { it.isNotBlank() }
        }

    fun postedAt(notificationWhen: Long, postTime: Long): Long =
        notificationWhen.takeIf { it > 0 } ?: postTime

    @Synchronized
    fun accept(key: String, postedAt: Long, title: String, bodies: List<String>): Boolean {
        val content = contentKey(title, bodies)
        val eventKey = "$key:$postedAt"
        if (recent[eventKey] == content) return false
        recent[eventKey] = content
        return true
    }

    @Synchronized
    fun forget(key: String, postedAt: Long, title: String, bodies: List<String>) {
        val eventKey = "$key:$postedAt"
        if (recent[eventKey] == contentKey(title, bodies)) recent.remove(eventKey)
    }

    // 카카오톡은 채팅방마다 알림 하나를 계속 갱신한다. 갱신이 몰리면 한 갱신에 새 메시지가
    // 여러 개 실리는데 BigText에는 마지막 메시지만 있다 — 2026-10-27 삼성증권 분할체결 14건 중
    // 6차·13차를 이렇게 놓쳤다(아르고스도 같은 두 건). 그래서 메시지 목록이 있으면 메시지마다
    // 따로 수집한다. 가장 새 메시지에서 MESSAGE_WINDOW_MS보다 오래된 이력은 이미 보냈거나
    // 리스너가 켜지기 전 것이라, 프로세스 재시작 뒤 다시 보내면 중복만 생겨 건너뛴다.
    // 시각 없는 메시지가 섞이면 메시지마다 문서 ID를 고정할 수 없어 예전 BigText 경로를 쓴다.
    fun usesMessages(messages: List<Message>): Boolean =
        messages.isNotEmpty() && messages.all { it.timeMs > 0 }

    fun recentMessages(messages: List<Message>): List<Message> {
        val latest = messages.maxOfOrNull { it.timeMs } ?: return emptyList()
        return messages.filter { it.text.isNotBlank() && latest - it.timeMs <= MESSAGE_WINDOW_MS }
    }

    // 같은 방에 같은 ms 메시지가 둘이면 키가 번갈아 덮어써지지 않게 본문 해시도 넣는다.
    private fun messageKey(key: String, message: Message) =
        "$key:msg:${message.timeMs}:${message.text.hashCode()}"

    @Synchronized
    fun acceptMessage(key: String, title: String, message: Message): Boolean {
        val messageKey = messageKey(key, message)
        val content = contentKey(title, listOf(message.text))
        if (recent[messageKey] == content) return false
        recent[messageKey] = content
        return true
    }

    @Synchronized
    fun forgetMessage(key: String, title: String, message: Message) {
        val messageKey = messageKey(key, message)
        if (recent[messageKey] == contentKey(title, listOf(message.text))) recent.remove(messageKey)
    }

    private fun contentKey(title: String, bodies: List<String>) = buildString {
        append(title.length).append(':').append(title)
        for (body in bodies) append(body.length).append(':').append(body)
    }

    companion object {
        private const val MAX_RECENT = 256
        const val MESSAGE_WINDOW_MS = 120_000L
    }
}
