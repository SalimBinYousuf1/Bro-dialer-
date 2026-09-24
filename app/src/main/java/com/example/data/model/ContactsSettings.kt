package com.example.data.model

data class ContactsSettings(
    val displayProfilePicture: Boolean = true,
    val displayNumber: Boolean = true,
    val displayCompanyAndTitle: Boolean = true,
    val showNumbersOnly: Boolean = false,
    val displayByAccount: String = "All Accounts",
    val sortBy: String = "First name", // "First name" or "Last name"
    val saveLocation: String = "Phone" // "Phone", "SIM Card", "Device Storage"
)
