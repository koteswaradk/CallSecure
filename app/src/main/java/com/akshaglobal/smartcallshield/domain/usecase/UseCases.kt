package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.data.model.SpamDetectionResult
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.data.repository.ContactRepository
import com.akshaglobal.smartcallshield.data.repository.SpamReportRepository
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.service.ai.SpamDetectionModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DetectSpamUseCase @Inject constructor(
    private val spamModel: SpamDetectionModel,
    private val contactRepository: ContactRepository,
    private val spamReportRepository: SpamReportRepository,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(phoneNumber: String): SpamDetectionResult {
        // Check if number is whitelisted
        val isWhitelisted = contactRepository.isWhitelisted(phoneNumber).first()
        if (isWhitelisted) {
            return SpamDetectionResult(
                phoneNumber = phoneNumber,
                isSpam = false,
                confidence = 0.0f,
                category = "WHITELISTED"
            )
        }

        // Check if number is blacklisted
        val isBlacklisted = contactRepository.isBlacklisted(phoneNumber).first()
        if (isBlacklisted) {
            return SpamDetectionResult(
                phoneNumber = phoneNumber,
                isSpam = true,
                confidence = 1.0f,
                category = "BLACKLISTED"
            )
        }

        // Check existing spam reports
        val spamReport = spamReportRepository.getSpamReport(phoneNumber).first()
        if (spamReport != null && spamReport.confidence > preferencesManager.spamConfidenceThreshold.first()) {
            return SpamDetectionResult(
                phoneNumber = phoneNumber,
                isSpam = true,
                confidence = spamReport.confidence,
                category = spamReport.spamCategory
            )
        }

        // Run TensorFlow Lite model
        val aiScore = spamModel.detectSpam(phoneNumber)
        return aiScore
    }
}

class ManageContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend fun addContact(contact: ContactEntity) = contactRepository.addContact(contact)

    suspend fun updateContact(contact: ContactEntity) = contactRepository.updateContact(contact)

    suspend fun deleteContact(contact: ContactEntity) = contactRepository.deleteContact(contact)

    fun getContactByPhoneNumber(phoneNumber: String): Flow<ContactEntity?> =
        contactRepository.getContactByPhoneNumber(phoneNumber)

    fun getAllContacts(): Flow<List<ContactEntity>> =
        contactRepository.getAllContacts()

    fun getEmergencyContacts(): Flow<List<ContactEntity>> =
        contactRepository.getEmergencyContacts()

    fun getWhitelistedContacts(): Flow<List<ContactEntity>> =
        contactRepository.getContactsByCategory("WHITELIST")

    fun getBlacklistedContacts(): Flow<List<ContactEntity>> =
        contactRepository.getContactsByCategory("BLACKLIST")
}

class GetCallHistoryUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository
) {
    fun getRecentCalls(limit: Int = 50): Flow<List<CallLogEntity>> =
        callLogRepository.getRecentCallLogs(limit)

    fun getCallsByNumber(phoneNumber: String): Flow<List<CallLogEntity>> =
        callLogRepository.getCallLogsByPhoneNumber(phoneNumber)

    fun getCallsInTimeRange(startTime: Long, endTime: Long): Flow<List<CallLogEntity>> =
        callLogRepository.getCallLogsBetween(startTime, endTime)

    suspend fun logCall(callLog: CallLogEntity) =
        callLogRepository.addCallLog(callLog)
}

class GetAnalyticsUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val spamReportRepository: SpamReportRepository,
    private val drivingModeRepository: com.akshaglobal.smartcallshield.data.repository.DrivingModeLogRepository
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

class HandleCallUseCase @Inject constructor(
    private val detectSpamUseCase: DetectSpamUseCase,
    private val callLogRepository: CallLogRepository,
    private val contactRepository: ContactRepository,
    private val preferencesManager: PreferencesManager
) {
    suspend operator fun invoke(phoneNumber: String): CallDecision {
        // Get spam detection result
        val spamResult = detectSpamUseCase(phoneNumber)

        // Check auto-reject spam setting
        val autoRejectSpam = preferencesManager.autoRejectSpam.first()
        val autoRejectUnknown = preferencesManager.autoRejectUnknown.first()

        return when {
            spamResult.isSpam && autoRejectSpam -> CallDecision.REJECT
            spamResult.category == "WHITELISTED" -> CallDecision.ALLOW
            spamResult.category == "EMERGENCY" -> CallDecision.ALLOW
            autoRejectUnknown -> CallDecision.REJECT
            else -> CallDecision.ALLOW
        }
    }
}

enum class CallDecision {
    ALLOW,
    REJECT,
    SILENT,
    REPLY_SMS
}

