package com.akshaglobal.smartcallshield.data.repository

import com.akshaglobal.smartcallshield.data.dao.CallLogDao
import com.akshaglobal.smartcallshield.data.dao.SpamReportDao
import com.akshaglobal.smartcallshield.data.dao.DrivingModeLogDao
import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.model.CallStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepository @Inject constructor(
    private val callLogDao: CallLogDao,
    private val spamReportDao: SpamReportDao,
    private val drivingModeLogDao: DrivingModeLogDao
) {
    fun getCallStatistics(): Flow<CallStatistics> {
        return combine(
            callLogDao.getBlockedCallsCount(),
            callLogDao.getSpamCallsCount(),
            drivingModeLogDao.getSuccessfulAutoRepliesCount()
        ) { blocked, spam, replies ->
            CallStatistics(
                blockedCalls = blocked,
                spamCallsPrevented = spam,
                drivingModeRepliesSent = replies
            )
        }
    }
    fun getCallsInTimeRange(startTime: Long, endTime: Long): Flow<List<CallLogEntity>> =
        callLogDao.getCallLogsBetween(startTime, endTime)
}

