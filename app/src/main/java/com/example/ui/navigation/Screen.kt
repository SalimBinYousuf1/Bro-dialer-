package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Recents : Screen("recents")
    data object Contacts : Screen("contacts")
    data object Dialpad : Screen("dialpad")
    data object More : Screen("more")
    data object Favorites : Screen("favorites")
    data object Voicemail : Screen("voicemail")
    data object BlockedNumbers : Screen("blocked_numbers")
    data object Settings : Screen("settings")
    data object ContactDetail : Screen("contact_detail/{contactId}") {
        fun createRoute(contactId: Long) = "contact_detail/$contactId"
    }
    data object ContactEdit : Screen("contact_edit/{contactId}") {
        fun createRoute(contactId: Long) = "contact_edit/$contactId"
    }
    data object ContactsSettings : Screen("contacts_settings")
    data object Permissions : Screen("permissions")
}
