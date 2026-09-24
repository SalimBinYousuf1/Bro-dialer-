package com.example.data.model

data class ContactItem(
    val id: Long,
    val lookupKey: String,
    val name: String,
    val photoUri: String? = null,
    val numbers: List<ContactPhoneNumber> = emptyList(),
    val emails: List<String> = emptyList(),
    val organization: String? = null,
    val note: String? = null,
    val isFavorite: Boolean = false
) {
    val primaryNumber: String
        get() = numbers.firstOrNull { it.isPrimary }?.number
            ?: numbers.firstOrNull()?.number
            ?: ""

    val initials: String
        get() {
            val parts = name.trim().split("\\s+".toRegex())
            return when {
                parts.isEmpty() || parts[0].isEmpty() -> "?"
                parts.size == 1 -> parts[0].take(1).uppercase()
                else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
            }
        }
}

data class ContactPhoneNumber(
    val number: String,
    val normalizedNumber: String = "",
    val type: String = "Mobile",
    val isPrimary: Boolean = false
)
