package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.BlockedNumberDao
import com.example.data.dao.CallNoteDao
import com.example.data.dao.ContactAvatarDao
import com.example.data.dao.RecentlyDeletedContactDao
import com.example.data.model.BlockedNumber
import com.example.data.model.CallNote
import com.example.data.model.ContactAvatar
import com.example.data.model.RecentlyDeletedContact

@Database(
    entities = [BlockedNumber::class, ContactAvatar::class, CallNote::class, RecentlyDeletedContact::class],
    version = 3,
    exportSchema = false
)
abstract class SalimDatabase : RoomDatabase() {

    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun contactAvatarDao(): ContactAvatarDao
    abstract fun callNoteDao(): CallNoteDao
    abstract fun recentlyDeletedContactDao(): RecentlyDeletedContactDao

    companion object {
        @Volatile
        private var INSTANCE: SalimDatabase? = null

        fun getDatabase(context: Context): SalimDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalimDatabase::class.java,
                    "salim_dialer.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
