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
    data object ContactDetail : Screen("contact_detail/{contactId}?number={number}") {
        fun createRoute(contactId: Long, number: String = "") =
            "contact_detail/$contactId?number=${android.net.Uri.encode(number)}"
    }
    data object ContactEdit : Screen("contact_edit/{contactId}?number={number}") {
        fun createRoute(contactId: Long, number: String = "") =
            "contact_edit/$contactId?number=${android.net.Uri.encode(number)}"
    }
    data object ContactsSettings : Screen("contacts_settings")
    data object CallRecordings : Screen("call_recordings")
    data object Analytics : Screen("analytics")
    data object Permissions : Screen("permissions")
}
