package com.example.data.model

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED,
    BLOCKED,
    VOICEMAIL
}

data class CallRecord(
    val id: Long,
    val number: String,
    val callerName: String?,
    val photoUri: String? = null,
    val date: Long,
    val durationSeconds: Long,
    val type: CallType,
    val simDisplayName: String? = null,
    val isRead: Boolean = true
) {
    val displayName: String
        get() = callerName?.takeIf { it.isNotBlank() } ?: number.ifBlank { "Unknown" }

    val formattedDuration: String
        get() {
            if (durationSeconds <= 0) return if (type == CallType.MISSED) "Missed" else "0s"
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        }

    val initials: String
        get() {
            val title = displayName.trim()
            val parts = title.split("\\s+".toRegex())
            return when {
                parts.isEmpty() || parts[0].isEmpty() -> "#"
                parts[0].first().isDigit() -> "#"
                parts.size == 1 -> parts[0].take(1).uppercase()
                else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            }
        }
}
