package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.repository.SpamReportRepository
import javax.inject.Inject

class DeleteOldSpamReportsUseCase @Inject constructor(
    private val repository: SpamReportRepository
) {
    suspend operator fun invoke(olderThan: Long) = repository.deleteOldReports(olderThan)
}

