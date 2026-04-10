package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCallHistoryUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository
) {
    fun getRecentCalls(limit: Int = 50): Flow<List<CallLogEntity>> =
        callLogRepository.getRecentCallLogs(limit)
    fun getCallsByNumber(phoneNumber: String): Flow<List<CallLogEntity>> =
        callLogRepository.getCallLogsByPhoneNumber(phoneNumber)
    fun getCallsInTimeRange(startTime: Long, endTime: Long): Flow<List<CallLogEntity>> =
        callLogRepository.getCallLogsBetween(startTime, endTime)
    fun getAllCallLogs(): Flow<List<CallLogEntity>> = callLogRepository.getAllCallLogs()
    suspend fun logCall(callLog: CallLogEntity) =
        callLogRepository.addCallLog(callLog)
}

