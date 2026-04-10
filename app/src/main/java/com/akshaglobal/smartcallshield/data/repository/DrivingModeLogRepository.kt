package com.akshaglobal.smartcallshield.data.repository

import com.akshaglobal.smartcallshield.data.dao.DrivingModeLogDao
import com.akshaglobal.smartcallshield.data.model.DrivingModeLogEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

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

