package com.example

import android.app.Application
import com.example.data.local.PreferencesManager
import com.example.data.local.SalimDatabase
import com.example.data.repository.BlockedRepository
import com.example.data.repository.CallLogRepository
import com.example.data.repository.CallNoteRepository
import com.example.data.repository.ContactAvatarRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.TelecomRepository

class SalimApplication : Application() {

    lateinit var database: SalimDatabase
        private set

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var contactsRepository: ContactsRepository
        private set

    lateinit var callLogRepository: CallLogRepository
        private set

    lateinit var blockedRepository: BlockedRepository
        private set

    lateinit var telecomRepository: TelecomRepository
        private set

    lateinit var contactAvatarRepository: ContactAvatarRepository
        private set

    lateinit var callNoteRepository: CallNoteRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = SalimDatabase.getDatabase(this)
        preferencesManager = PreferencesManager(this)
        contactsRepository = ContactsRepository(this)
        callLogRepository = CallLogRepository(this)
        blockedRepository = BlockedRepository(this)
        telecomRepository = TelecomRepository(this)
        contactAvatarRepository = ContactAvatarRepository(database.contactAvatarDao())
        callNoteRepository = CallNoteRepository(database.callNoteDao())
    }

    companion object {
        lateinit var instance: SalimApplication
            private set
    }
}
