package com.example.data.repository

import com.example.data.model.CallRecord
import com.example.data.model.CallType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AnalyticsTimeRange {
    LAST_7_DAYS,
    LAST_30_DAYS,
    ALL_TIME
}

data class DailyCallCount(
    val dayLabel: String,
    val dateMillis: Long,
    val incoming: Int,
    val outgoing: Int,
    val missed: Int
) {
    val total: Int get() = incoming + outgoing + missed
}

data class ContactCallStat(
    val number: String,
    val name: String,
    val photoUri: String?,
    val callCount: Int,
    val totalDurationSeconds: Long
) {
    val formattedDuration: String
        get() {
            val mins = totalDurationSeconds / 60
            val secs = totalDurationSeconds % 60
            return if (mins >= 60) {
                val hours = mins / 60
                val remMins = mins % 60
                "${hours}h ${remMins}m"
            } else {
                "${mins}m ${secs}s"
            }
        }
}

data class CallAnalyticsSummary(
    val timeRange: AnalyticsTimeRange,
    val totalCalls: Int,
    val incomingCalls: Int,
    val outgoingCalls: Int,
    val missedCalls: Int,
    val rejectedCalls: Int,
    val blockedCalls: Int,
    val totalDurationSeconds: Long,
    val averageDurationSeconds: Long,
    val dailyBreakdown: List<DailyCallCount>,
    val topContactsByFrequency: List<ContactCallStat>,
    val topContactsByDuration: List<ContactCallStat>
) {
    val formattedTotalDuration: String
        get() {
            val mins = totalDurationSeconds / 60
            return if (mins >= 60) {
                val hours = mins / 60
                val remMins = mins % 60
                "${hours}h ${remMins}m"
            } else {
                "${mins}m"
            }
        }

    val formattedAvgDuration: String
        get() {
            val mins = averageDurationSeconds / 60
            val secs = averageDurationSeconds % 60
            return "%02d:%02d".format(mins, secs)
        }
}

class CallAnalyticsRepository(private val callLogRepository: CallLogRepository) {

    suspend fun getAnalytics(range: AnalyticsTimeRange): CallAnalyticsSummary = withContext(Dispatchers.Default) {
        val allLogs = callLogRepository.loadCallLogs()

        val now = System.currentTimeMillis()
        val cutoffTime = when (range) {
            AnalyticsTimeRange.LAST_7_DAYS -> now - (7L * 24 * 60 * 60 * 1000)
            AnalyticsTimeRange.LAST_30_DAYS -> now - (30L * 24 * 60 * 60 * 1000)
            AnalyticsTimeRange.ALL_TIME -> 0L
        }

        val filteredLogs = if (cutoffTime > 0) {
            allLogs.filter { it.date >= cutoffTime }
        } else {
            allLogs
        }

        var incoming = 0
        var outgoing = 0
        var missed = 0
        var rejected = 0
        var blocked = 0
        var totalDuration = 0L

        filteredLogs.forEach { log ->
            when (log.type) {
                CallType.INCOMING -> incoming++
                CallType.OUTGOING -> outgoing++
                CallType.MISSED -> missed++
                CallType.REJECTED -> rejected++
                CallType.BLOCKED -> blocked++
                CallType.VOICEMAIL -> {}
            }
            totalDuration += log.durationSeconds
        }

        val totalCalls = filteredLogs.size
        val avgDuration = if (incoming + outgoing > 0) {
            totalDuration / (incoming + outgoing)
        } else 0L

        // Daily breakdown (last 7 or 14 days)
        val daysCount = if (range == AnalyticsTimeRange.LAST_7_DAYS) 7 else 14
        val dailyList = mutableListOf<DailyCallCount>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in (daysCount - 1) downTo 0) {
            cal.timeInMillis = now - (i * 24L * 60 * 60 * 1000)
            val startOfDay = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = startOfDay + (24L * 60 * 60 * 1000) - 1
            val dayLabel = dayFormat.format(Date(startOfDay))

            val dayLogs = filteredLogs.filter { it.date in startOfDay..endOfDay }
            val dayIncoming = dayLogs.count { it.type == CallType.INCOMING }
            val dayOutgoing = dayLogs.count { it.type == CallType.OUTGOING }
            val dayMissed = dayLogs.count { it.type == CallType.MISSED || it.type == CallType.REJECTED }

            dailyList.add(
                DailyCallCount(
                    dayLabel = dayLabel,
                    dateMillis = startOfDay,
                    incoming = dayIncoming,
                    outgoing = dayOutgoing,
                    missed = dayMissed
                )
            )
        }

        // Group by contact / number
        val contactMap = mutableMapOf<String, MutableList<CallRecord>>()
        filteredLogs.forEach { log ->
            val key = log.number.trim()
            if (key.isNotBlank()) {
                val list = contactMap.getOrPut(key) { mutableListOf() }
                list.add(log)
            }
        }

        val contactStats = contactMap.map { (number, records) ->
            val firstRecord = records.first()
            val name = firstRecord.callerName?.ifBlank { null } ?: number
            val photoUri = firstRecord.photoUri
            val count = records.size
            val duration = records.sumOf { it.durationSeconds }
            ContactCallStat(
                number = number,
                name = name,
                photoUri = photoUri,
                callCount = count,
                totalDurationSeconds = duration
            )
        }

        val topByFrequency = contactStats.sortedByDescending { it.callCount }.take(5)
        val topByDuration = contactStats.sortedByDescending { it.totalDurationSeconds }.take(5)

        CallAnalyticsSummary(
            timeRange = range,
            totalCalls = totalCalls,
            incomingCalls = incoming,
            outgoingCalls = outgoing,
            missedCalls = missed,
            rejectedCalls = rejected,
            blockedCalls = blocked,
            totalDurationSeconds = totalDuration,
            averageDurationSeconds = avgDuration,
            dailyBreakdown = dailyList,
            topContactsByFrequency = topByFrequency,
            topContactsByDuration = topByDuration
        )
    }
}
