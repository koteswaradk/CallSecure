package com.akshaglobal.smartcallshield.data.repository

import com.akshaglobal.smartcallshield.data.dao.CallLogDao
import com.akshaglobal.smartcallshield.data.dao.ContactDao
import com.akshaglobal.smartcallshield.data.dao.DrivingModeLogDao
import com.akshaglobal.smartcallshield.data.dao.SpamReportDao
import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.model.CallStatistics
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.data.model.DrivingModeLogEntity
import com.akshaglobal.smartcallshield.data.model.SpamReportEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val contactDao: ContactDao
) {
    suspend fun addContact(contact: ContactEntity) = contactDao.insertContact(contact)

    suspend fun updateContact(contact: ContactEntity) = contactDao.updateContact(contact)

    suspend fun deleteContact(contact: ContactEntity) = contactDao.deleteContact(contact)

    fun getContactByPhoneNumber(phoneNumber: String): Flow<ContactEntity?> =
        contactDao.getContactByPhoneNumber(phoneNumber)

    fun getContactsByCategory(category: String): Flow<List<ContactEntity>> =
        contactDao.getContactsByCategory(category)

    fun getEmergencyContacts(): Flow<List<ContactEntity>> =
        contactDao.getEmergencyContacts()

    fun getAllContacts(): Flow<List<ContactEntity>> =
        contactDao.getAllContacts()

    suspend fun deleteByPhoneNumber(phoneNumber: String) =
        contactDao.deleteByPhoneNumber(phoneNumber)

    fun isWhitelisted(phoneNumber: String): Flow<Boolean> =
        contactDao.getContactByPhoneNumber(phoneNumber).map { contact ->
            contact != null && contact.category == "WHITELIST"
        }

    fun isBlacklisted(phoneNumber: String): Flow<Boolean> =
        contactDao.getContactByPhoneNumber(phoneNumber).map { contact ->
            contact != null && contact.category == "BLACKLIST"
        }
}

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

@Singleton
class SpamReportRepository @Inject constructor(
    private val spamReportDao: SpamReportDao
) {
    suspend fun addSpamReport(report: SpamReportEntity) = spamReportDao.insertSpamReport(report)

    suspend fun updateSpamReport(report: SpamReportEntity) = spamReportDao.updateSpamReport(report)

    fun getSpamReport(phoneNumber: String): Flow<SpamReportEntity?> =
        spamReportDao.getSpamReport(phoneNumber)

    fun getHighRiskNumbers(): Flow<List<SpamReportEntity>> =
        spamReportDao.getHighRiskNumbers()

    fun getSpamReportsCount(): Flow<Long> =
        spamReportDao.getSpamReportsCount()

    suspend fun incrementReportCount(phoneNumber: String, timestamp: Long) =
        spamReportDao.incrementReportCount(phoneNumber, timestamp)

    suspend fun deleteOldReports(olderThan: Long) =
        spamReportDao.deleteOldReports(olderThan)
}

@Singleton
class DrivingModeLogRepository @Inject constructor(
    private val drivingModeLogDao: DrivingModeLogDao
) {
    suspend fun addDrivingModeLog(log: DrivingModeLogEntity) = drivingModeLogDao.insertDrivingModeLog(log)

    fun getRecentDrivingModeLogs(limit: Int = 50): Flow<List<DrivingModeLogEntity>> =
        drivingModeLogDao.getRecentDrivingModeLogs(limit)

    fun getSuccessfulAutoRepliesCount(): Flow<Long> =
        drivingModeLogDao.getSuccessfulAutoRepliesCount()

    fun getDrivingModeLogsBetween(startTime: Long, endTime: Long): Flow<List<DrivingModeLogEntity>> =
        drivingModeLogDao.getDrivingModeLogsBetween(startTime, endTime)

    suspend fun deleteOldDrivingModeLogs(beforeTimestamp: Long) =
        drivingModeLogDao.deleteOldDrivingModeLogs(beforeTimestamp)
}

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
