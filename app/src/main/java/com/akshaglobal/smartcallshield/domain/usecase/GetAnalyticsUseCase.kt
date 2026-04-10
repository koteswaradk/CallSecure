package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.data.repository.SpamReportRepository
import com.akshaglobal.smartcallshield.data.repository.DrivingModeLogRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAnalyticsUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val spamReportRepository: SpamReportRepository,
    private val drivingModeRepository: DrivingModeLogRepository
) {
    fun getBlockedCallsCount(): Flow<Long> =
        callLogRepository.getBlockedCallsCount()
    fun getSpamCallsCount(): Flow<Long> =
        callLogRepository.getSpamCallsCount()
    fun getDrivingModeRepliesCount(): Flow<Long> =
        drivingModeRepository.getSuccessfulAutoRepliesCount()
    fun getSpamTrends(): Flow<List<com.akshaglobal.smartcallshield.data.model.SpamReportEntity>> =
        spamReportRepository.getHighRiskNumbers()
}

