package com.example.data.model

enum class TelephonyCallState {
    IDLE,
    DIALING,
    RINGING,
    ACTIVE,
    HOLDING,
    DISCONNECTING,
    DISCONNECTED
}

data class ActiveCallInfo(
    val callId: String = "",
    val number: String = "",
    val displayName: String = "",
    val photoUri: String? = null,
    val state: TelephonyCallState = TelephonyCallState.IDLE,
    val connectTimeMillis: Long = 0L,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isBluetoothOn: Boolean = false,
    val isOnHold: Boolean = false,
    val canHold: Boolean = true,
    val canMerge: Boolean = false,
    val canSwap: Boolean = false
) {
    val initials: String
        get() {
            val title = displayName.ifBlank { number }.trim()
            val parts = title.split("\\s+".toRegex())
            return when {
                parts.isEmpty() || parts[0].isEmpty() -> "#"
                parts[0].first().isDigit() -> "#"
                parts.size == 1 -> parts[0].take(1).uppercase()
                else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            }
        }
}
