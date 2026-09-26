package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_customizations")
data class ContactCustomization(
    @PrimaryKey
    val contactId: Long,
    val ringtoneUri: String? = null,
    val ringtoneTitle: String? = null,
    val callBackgroundUri: String? = null,
    val defaultSimId: Int = -1 // -1 = System default / Always ask, 1 = SIM 1, 2 = SIM 2
)
