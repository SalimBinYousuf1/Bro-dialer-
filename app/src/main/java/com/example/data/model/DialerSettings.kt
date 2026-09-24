package com.example.data.model

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class ContactSortOrder {
    FIRST_NAME,
    LAST_NAME
}

data class DialerSettings(
    val themeMode: ThemeMode = ThemeMode.LIGHT,
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
    val blockUnknownNumbers: Boolean = false
) {
    val darkTheme: Boolean
        get() = themeMode == ThemeMode.DARK
}
