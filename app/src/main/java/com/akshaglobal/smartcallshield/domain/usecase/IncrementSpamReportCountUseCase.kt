package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.repository.SpamReportRepository
import javax.inject.Inject

class IncrementSpamReportCountUseCase @Inject constructor(
    private val repository: SpamReportRepository
) {
    suspend operator fun invoke(phoneNumber: String, timestamp: Long) = repository.incrementReportCount(phoneNumber, timestamp)
}

