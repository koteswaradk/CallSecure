package com.akshaglobal.smartcallshield.data.repository

import com.akshaglobal.smartcallshield.data.dao.SpamReportDao
import com.akshaglobal.smartcallshield.data.model.SpamReportEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

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

