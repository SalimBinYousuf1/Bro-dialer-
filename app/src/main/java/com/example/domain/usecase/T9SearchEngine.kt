package com.example.domain.usecase

import com.example.data.model.ContactItem

object T9SearchEngine {

    private val CHAR_TO_T9 = mapOf(
        'a' to '2', 'b' to '2', 'c' to '2',
        'd' to '3', 'e' to '3', 'f' to '3',
        'g' to '4', 'h' to '4', 'i' to '4',
        'j' to '5', 'k' to '5', 'l' to '5',
        'm' to '6', 'n' to '6', 'o' to '6',
        'p' to '7', 'q' to '7', 'r' to '7', 's' to '7',
        't' to '8', 'u' to '8', 'v' to '8',
        'w' to '9', 'x' to '9', 'y' to '9', 'z' to '9'
    )

    fun stringToT9(input: String): String {
        val sb = StringBuilder()
        for (ch in input.lowercase()) {
            val t9 = CHAR_TO_T9[ch]
            if (t9 != null) {
                sb.append(t9)
            } else if (ch.isDigit()) {
                sb.append(ch)
            } else if (ch.isWhitespace()) {
                sb.append(' ')
            }
        }
        return sb.toString()
    }

    data class T9MatchResult(
        val contact: ContactItem,
        val matchedInName: Boolean,
        val matchedInNumber: Boolean,
        val matchedPhone: String? = null
    )

    fun search(contacts: List<ContactItem>, queryDigits: String): List<T9MatchResult> {
        val cleanQuery = queryDigits.filter { it.isDigit() || it == '*' || it == '#' }
        if (cleanQuery.isEmpty()) return emptyList()

        val results = mutableListOf<T9MatchResult>()

        for (contact in contacts) {
            // Check name via T9
            val nameT9 = stringToT9(contact.name)
            // Word boundary match or substring match
            val words = nameT9.split(" ")
            val matchedWord = words.any { it.startsWith(cleanQuery) }
            val matchedNameSub = nameT9.replace(" ", "").contains(cleanQuery)

            var matchedPhone: String? = null
            for (phone in contact.numbers) {
                val digitsOnly = phone.number.filter { it.isDigit() }
                if (digitsOnly.contains(cleanQuery)) {
                    matchedPhone = phone.number
                    break
                }
            }

            if (matchedWord || matchedNameSub || matchedPhone != null) {
                results.add(
                    T9MatchResult(
                        contact = contact,
                        matchedInName = matchedWord || matchedNameSub,
                        matchedInNumber = matchedPhone != null,
                        matchedPhone = matchedPhone
                    )
                )
            }
        }

        // Sort: prefix matches first, then favorite contacts
        return results.sortedWith(
            compareByDescending<T9MatchResult> { it.contact.isFavorite }
                .thenByDescending { it.matchedInName }
                .thenBy { it.contact.name }
        )
    }
}
