package com.akshaglobal.smartcallshield.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.data.model.DrivingModeLogEntity
import com.akshaglobal.smartcallshield.data.model.SpamReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("SELECT * FROM contacts WHERE id = :id")
    fun getContactById(id: Long): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts WHERE phoneNumber = :phoneNumber")
    fun getContactByPhoneNumber(phoneNumber: String): Flow<ContactEntity?>

    @Query("SELECT * FROM contacts WHERE category = :category")
    fun getContactsByCategory(category: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE isEmergency = 1")
    fun getEmergencyContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY displayName ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("DELETE FROM contacts WHERE phoneNumber = :phoneNumber")
    suspend fun deleteByPhoneNumber(phoneNumber: String)
}

@Dao
interface CallLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogEntity)

    @Query("SELECT * FROM call_logs WHERE id = :id")
    fun getCallLogById(id: Long): Flow<CallLogEntity?>

    @Query("SELECT * FROM call_logs WHERE phoneNumber = :phoneNumber ORDER BY timestamp DESC")
    fun getCallLogsByPhoneNumber(phoneNumber: String): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentCallLogs(limit: Int = 50): Flow<List<CallLogEntity>>

    @Query("SELECT COUNT(*) FROM call_logs WHERE wasBlocked = 1")
    fun getBlockedCallsCount(): Flow<Long>

    @Query("SELECT COUNT(*) FROM call_logs WHERE isSpam = 1")
    fun getSpamCallsCount(): Flow<Long>

    @Query("SELECT COUNT(*) FROM call_logs WHERE timestamp > :since")
    fun getCallsCountSince(since: Long): Flow<Long>

    @Query("DELETE FROM call_logs WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldCallLogs(beforeTimestamp: Long)

    @Query("SELECT * FROM call_logs WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getCallLogsBetween(startTime: Long, endTime: Long): Flow<List<CallLogEntity>>
}

@Dao
interface SpamReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpamReport(report: SpamReportEntity)

    @Update
    suspend fun updateSpamReport(report: SpamReportEntity)

    @Query("SELECT * FROM spam_reports WHERE phoneNumber = :phoneNumber")
    fun getSpamReport(phoneNumber: String): Flow<SpamReportEntity?>

    @Query("SELECT * FROM spam_reports ORDER BY confidence DESC")
    fun getHighRiskNumbers(): Flow<List<SpamReportEntity>>

    @Query("SELECT COUNT(*) FROM spam_reports")
    fun getSpamReportsCount(): Flow<Long>

    @Query("UPDATE spam_reports SET reportCount = reportCount + 1, lastReportedAt = :timestamp WHERE phoneNumber = :phoneNumber")
    suspend fun incrementReportCount(phoneNumber: String, timestamp: Long)

    @Query("DELETE FROM spam_reports WHERE lastReportedAt < :olderThan")
    suspend fun deleteOldReports(olderThan: Long)
}

@Dao
interface DrivingModeLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivingModeLog(log: DrivingModeLogEntity)

    @Query("SELECT * FROM driving_mode_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentDrivingModeLogs(limit: Int = 50): Flow<List<DrivingModeLogEntity>>

    @Query("SELECT COUNT(*) FROM driving_mode_logs WHERE status = 'SENT'")
    fun getSuccessfulAutoRepliesCount(): Flow<Long>

    @Query("SELECT * FROM driving_mode_logs WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getDrivingModeLogsBetween(startTime: Long, endTime: Long): Flow<List<DrivingModeLogEntity>>

    @Query("DELETE FROM driving_mode_logs WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldDrivingModeLogs(beforeTimestamp: Long)
}

