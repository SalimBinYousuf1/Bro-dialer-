package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CallbackReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface CallbackReminderDao {

    @Query("SELECT * FROM callback_reminders WHERE isCompleted = 0 ORDER BY remindTimeMillis ASC")
    fun getAllPendingReminders(): Flow<List<CallbackReminder>>

    @Query("SELECT * FROM callback_reminders WHERE isCompleted = 0 AND remindTimeMillis > :now ORDER BY remindTimeMillis ASC")
    suspend fun getUpcomingPendingReminders(now: Long): List<CallbackReminder>

    @Query("SELECT * FROM callback_reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CallbackReminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: CallbackReminder): Long

    @Query("UPDATE callback_reminders SET isCompleted = 1 WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Delete
    suspend fun delete(reminder: CallbackReminder)

    @Query("DELETE FROM callback_reminders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
