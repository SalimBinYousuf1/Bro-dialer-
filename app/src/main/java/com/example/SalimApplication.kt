package com.example

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.example.data.local.PreferencesManager
import com.example.data.local.SalimDatabase
import com.example.data.repository.BlockedRepository
import com.example.data.repository.CallLogRepository
import com.example.data.repository.CallNoteRepository
import com.example.data.repository.CallRecordingRepository
import com.example.data.repository.ContactAvatarRepository
import com.example.data.repository.ContactCustomizationRepository
import com.example.data.repository.ContactsRepository
import com.example.data.repository.RecentlyDeletedRepository
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

    lateinit var contactCustomizationRepository: ContactCustomizationRepository
        private set

    lateinit var callNoteRepository: CallNoteRepository
        private set

    lateinit var recentlyDeletedRepository: RecentlyDeletedRepository
        private set

    lateinit var callRecordingRepository: CallRecordingRepository
        private set

    var isAppInForeground: Boolean = false
        private set

    private var foregroundActivityCount = 0

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
        contactCustomizationRepository = ContactCustomizationRepository(this)
        callNoteRepository = CallNoteRepository(database.callNoteDao())
        recentlyDeletedRepository = RecentlyDeletedRepository(database.recentlyDeletedContactDao())
        callRecordingRepository = CallRecordingRepository(this, database.callRecordingDao())

        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                foregroundActivityCount++
                isAppInForeground = foregroundActivityCount > 0
            }
            override fun onActivityResumed(activity: Activity) {
                isAppInForeground = true
            }
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                foregroundActivityCount = maxOf(0, foregroundActivityCount - 1)
                isAppInForeground = foregroundActivityCount > 0
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    companion object {
        lateinit var instance: SalimApplication
            private set
    }
}
