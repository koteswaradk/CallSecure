package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.model.SpamReportEntity
import com.akshaglobal.smartcallshield.data.repository.SpamReportRepository
import javax.inject.Inject

class AddSpamReportUseCase @Inject constructor(
    private val repository: SpamReportRepository
) {
    suspend operator fun invoke(report: SpamReportEntity) = repository.addSpamReport(report)
}

