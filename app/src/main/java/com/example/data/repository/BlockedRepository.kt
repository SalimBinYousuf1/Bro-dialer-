package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.BlockedNumberContract
import com.example.data.local.SalimDatabase
import com.example.data.model.BlockedNumber
import com.example.domain.usecase.PhoneNumberHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BlockedRepository(private val context: Context) {

    private val dao = SalimDatabase.getDatabase(context).blockedNumberDao()

    val allBlocked: Flow<List<BlockedNumber>> = dao.getAllBlocked()

    suspend fun blockNumber(number: String, contactName: String? = null): Long = withContext(Dispatchers.IO) {
        val cleanNumber = number.trim()
        val norm = PhoneNumberHelper.normalizeNumber(cleanNumber)
        
        // 1. Sync with Android System BlockedNumberContract if default dialer
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                BlockedNumberContract.canCurrentUserBlockNumbers(context)) {
                val values = ContentValues().apply {
                    put(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER, cleanNumber)
                }
                context.contentResolver.insert(BlockedNumberContract.BlockedNumbers.CONTENT_URI, values)
            }
        } catch (_: Exception) {}

        // 2. Persist in local database with rich contact metadata
        dao.insert(
            BlockedNumber(
                number = cleanNumber,
                normalizedNumber = norm,
                contactName = contactName
            )
        )
    }

    suspend fun unblock(blockedNumber: BlockedNumber) = withContext(Dispatchers.IO) {
        // Remove from system BlockedNumberContract
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                BlockedNumberContract.canCurrentUserBlockNumbers(context)) {
                val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
                val selection = "${BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER} = ? OR ${BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER} = ?"
                context.contentResolver.delete(uri, selection, arrayOf(blockedNumber.number, blockedNumber.normalizedNumber))
            }
        } catch (_: Exception) {}

        dao.delete(blockedNumber)
    }

    suspend fun unblockByNumber(number: String) = withContext(Dispatchers.IO) {
        val norm = PhoneNumberHelper.normalizeNumber(number)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                BlockedNumberContract.canCurrentUserBlockNumbers(context)) {
                val uri = BlockedNumberContract.BlockedNumbers.CONTENT_URI
                val selection = "${BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER} = ? OR ${BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER} = ?"
                context.contentResolver.delete(uri, selection, arrayOf(number.trim(), norm))
            }
        } catch (_: Exception) {}

        dao.deleteByNumber(norm)
    }

    suspend fun isNumberBlocked(number: String): Boolean = withContext(Dispatchers.IO) {
        val norm = PhoneNumberHelper.normalizeNumber(number)
        
        // Check local database first
        val isLocallyBlocked = dao.isBlocked(norm)
        if (isLocallyBlocked) return@withContext true

        // Check Android system blocked list
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                BlockedNumberContract.canCurrentUserBlockNumbers(context)) {
                return@withContext BlockedNumberContract.isBlocked(context, number)
            }
        } catch (_: Exception) {}

        false
    }
}
