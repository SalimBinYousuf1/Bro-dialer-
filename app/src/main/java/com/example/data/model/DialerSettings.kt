package com.example.data.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    SALIM
}

enum class ContactSortOrder {
    FIRST_NAME,
    LAST_NAME
}

data class DialerSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticFeedback: Boolean = true,
    val dialpadTones: Boolean = true,
    val vibrateOnConnect: Boolean = true,
    val vibrateOnDisconnect: Boolean = true,
    val callConfirmation: Boolean = false,
    val autoSpeaker: Boolean = false,
    val contactSortOrder: ContactSortOrder = ContactSortOrder.FIRST_NAME,
    val showContactPhotos: Boolean = true,
    val t9SearchEnabled: Boolean = true,
    val groupCallsByDate: Boolean = true,
    val confirmDeleteCallLog: Boolean = true,
    val voicemailNumber: String = "",
    val blockUnknownNumbers: Boolean = false,
    val defaultStartTab: String = "dialpad", // "dialpad", "recents", "contacts", "home", "more"
    val callBackgroundUri: String? = null,
    val defaultRingtoneUri: String? = null,
    val defaultRingtoneTitle: String? = "Default",
    val quickMessages: List<String> = listOf(
        "Can't talk right now. What's up?",
        "I'll call you right back.",
        "On my way.",
        "Sorry, I'm in a meeting."
    )
) {
    val darkTheme: Boolean
        get() = themeMode == ThemeMode.DARK
}
