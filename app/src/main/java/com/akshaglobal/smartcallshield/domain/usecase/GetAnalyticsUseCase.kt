package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.data.repository.SpamReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAnalyticsUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val spamReportRepository: SpamReportRepository
) {
    fun getBlockedCallsCount(): Flow<Long> =
        callLogRepository.getBlockedCallsCount()
    fun getSpamCallsCount(): Flow<Long> =
        callLogRepository.getSpamCallsCount()
    fun getSpamTrends(): Flow<List<com.akshaglobal.smartcallshield.data.model.SpamReportEntity>> =
        spamReportRepository.getHighRiskNumbers()
}
