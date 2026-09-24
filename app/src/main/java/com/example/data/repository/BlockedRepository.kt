package com.example.data.repository

import android.content.Context
import com.example.data.local.SalimDatabase
import com.example.data.model.BlockedNumber
import com.example.domain.usecase.PhoneNumberHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BlockedRepository(context: Context) {

    private val dao = SalimDatabase.getDatabase(context).blockedNumberDao()

    val allBlocked: Flow<List<BlockedNumber>> = dao.getAllBlocked()

    suspend fun blockNumber(number: String, contactName: String? = null): Long = withContext(Dispatchers.IO) {
        val norm = PhoneNumberHelper.normalizeNumber(number)
        dao.insert(
            BlockedNumber(
                number = number.trim(),
                normalizedNumber = norm,
                contactName = contactName
            )
        )
    }

    suspend fun unblock(blockedNumber: BlockedNumber) = withContext(Dispatchers.IO) {
        dao.delete(blockedNumber)
    }

    suspend fun unblockByNumber(number: String) = withContext(Dispatchers.IO) {
        val norm = PhoneNumberHelper.normalizeNumber(number)
        dao.deleteByNumber(norm)
    }

    suspend fun isNumberBlocked(number: String): Boolean = withContext(Dispatchers.IO) {
        val norm = PhoneNumberHelper.normalizeNumber(number)
        dao.isBlocked(norm)
    }
}
