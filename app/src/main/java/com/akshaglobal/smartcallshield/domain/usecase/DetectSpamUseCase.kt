package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.model.SpamDetectionResult
import com.akshaglobal.smartcallshield.data.repository.ContactRepository
import com.akshaglobal.smartcallshield.data.repository.SpamReportRepository
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.service.ai.SpamDetectionModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DetectSpamUseCase @Inject constructor(
    private val spamModel: SpamDetectionModel,
    private val contactRepository: ContactRepository,
    private val spamReportRepository: SpamReportRepository,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(phoneNumber: String): SpamDetectionResult {
        val isWhitelisted = contactRepository.isWhitelisted(phoneNumber).first()
        if (isWhitelisted) {
            return SpamDetectionResult(
                phoneNumber = phoneNumber,
                isSpam = false,
                confidence = 0.0f,
                category = "WHITELISTED"
            )
        }
        val isBlacklisted = contactRepository.isBlacklisted(phoneNumber).first()
        if (isBlacklisted) {
            return SpamDetectionResult(
                phoneNumber = phoneNumber,
                isSpam = true,
                confidence = 1.0f,
                category = "BLACKLISTED"
            )
        }
        val spamReport = spamReportRepository.getSpamReport(phoneNumber).first()
        if (spamReport != null && spamReport.confidence > preferencesManager.spamConfidenceThreshold.first()) {
            return SpamDetectionResult(
                phoneNumber = phoneNumber,
                isSpam = true,
                confidence = spamReport.confidence,
                category = spamReport.spamCategory
            )
        }
        val aiScore = spamModel.detectSpam(phoneNumber)
        return aiScore
    }
}

