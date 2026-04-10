package com.akshaglobal.smartcallshield.data.repository

import com.akshaglobal.smartcallshield.data.dao.CallLogDao
import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallLogRepository @Inject constructor(
    private val callLogDao: CallLogDao
) {
    suspend fun addCallLog(callLog: CallLogEntity) = callLogDao.insertCallLog(callLog)
    fun getCallLogsByPhoneNumber(phoneNumber: String): Flow<List<CallLogEntity>> =
        callLogDao.getCallLogsByPhoneNumber(phoneNumber)
    fun getRecentCallLogs(limit: Int = 50): Flow<List<CallLogEntity>> =
        callLogDao.getRecentCallLogs(limit)
    fun getBlockedCallsCount(): Flow<Long> =
        callLogDao.getBlockedCallsCount()
    fun getSpamCallsCount(): Flow<Long> =
        callLogDao.getSpamCallsCount()
    fun getCallsCountSince(since: Long): Flow<Long> =
        callLogDao.getCallsCountSince(since)
    suspend fun deleteOldCallLogs(beforeTimestamp: Long) =
        callLogDao.deleteOldCallLogs(beforeTimestamp)
    fun getCallLogsBetween(startTime: Long, endTime: Long): Flow<List<CallLogEntity>> =
        callLogDao.getCallLogsBetween(startTime, endTime)
    fun getAllCallLogs(): Flow<List<CallLogEntity>> = callLogDao.getAllCallLogs()
}

