package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.repository.SpamReportRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSpamReportsCountUseCase @Inject constructor(
    private val repository: SpamReportRepository
) {
    operator fun invoke(): Flow<Long> = repository.getSpamReportsCount()
}

