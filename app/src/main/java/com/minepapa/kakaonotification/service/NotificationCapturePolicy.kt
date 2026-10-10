package com.minepapa.kakaonotification.service

/** NotificationListenerService에서 Android 객체를 읽은 뒤 적용하는 수집 기준. */
internal class NotificationCapturePolicy(private val kakaoPackage: String) {
    enum class Capture { KAKAO, SPENDING_APP }

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

    private fun contentKey(title: String, bodies: List<String>) = buildString {
        append(title.length).append(':').append(title)
        for (body in bodies) append(body.length).append(':').append(body)
    }

    companion object {
        private const val MAX_RECENT = 256
    }
}
