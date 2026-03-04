package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.model.CallLogEntity
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.data.model.SpamDetectionResult
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.data.repository.ContactRepository
import com.akshaglobal.smartcallshield.data.repository.SpamReportRepository
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.service.ai.SpamDetectionModel
import com.akshaglobal.smartcallshield.data.repository.ModeRepository
import com.akshaglobal.smartcallshield.data.contacts.DeviceContactsProvider
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

    fun getAllCallLogs(): Flow<List<CallLogEntity>> = callLogRepository.getAllCallLogs()

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
    private val preferencesManager: PreferencesManager,
    private val modeRepository: ModeRepository,
    private val deviceContactsProvider: DeviceContactsProvider // <-- Injected
) {
    suspend operator fun invoke(phoneNumber: String): CallDecision {
        // --- ADDED: Allow all calls if app is disabled ---
        val appEnabled = preferencesManager.isAppEnabled.first()
        if (!appEnabled) {
            println("[DEBUG] App is disabled, allowing all calls.")
            return CallDecision.ALLOW
        }
        // --- END ADDED ---
        val currentMode = preferencesManager.currentMode.first().uppercase()
        val normalizedNumber = phoneNumber.replace(Regex("[^+0-9]"), "")

        println("[DEBUG] Current mode: $currentMode, Incoming: $normalizedNumber")

        // 1. Check if allowed by mode
        val allowedByMode = when (currentMode) {
            "NORMAL" -> {
                try {
                    val deviceContacts = deviceContactsProvider.fetchDeviceContacts()
                    println("[DEBUG] Device contacts: " + deviceContacts.map { it.phoneNumber })
                    val allowed = deviceContacts.any {
                        val contactNormalized = it.phoneNumber.replace(Regex("[^+0-9]"), "")
                        println("[DEBUG] Comparing incoming $normalizedNumber to device contact ${it.phoneNumber} (normalized: $contactNormalized)")
                        contactNormalized.endsWith(normalizedNumber) || normalizedNumber.endsWith(contactNormalized)
                    }
                    println("[DEBUG] Allowed by NORMAL: $allowed")
                    allowed
                } catch (e: Exception) {
                    println("[ERROR] Could not load device contacts: ${e.message}")
                    true // fallback: allow if contacts cannot be loaded
                }
            }
            else -> {
                val activeMode = modeRepository.getActiveMode().first()
                if (activeMode == null) {
                    println("[DEBUG] No active mode found!")
                    true // fallback: allow if no active mode
                } else {
                    val modeWithContacts = modeRepository.getModeWithContacts(activeMode.id)
                    val contacts = modeWithContacts?.contacts ?: emptyList()
                    println("[DEBUG] Active mode: ${activeMode.name} (id=${activeMode.id})")
                    println("[DEBUG] All contacts for mode: " + contacts.map { c -> "${c.displayName} (${c.phoneNumber}) [${c.category}]" })
                    println("[DEBUG] Incoming number (normalized): $normalizedNumber")
                    val filteredContacts = contacts.filter { it.category.equals(activeMode.name, ignoreCase = true) }
                    println("[DEBUG] Filtered contacts for ${activeMode.name}: " + filteredContacts.map { c -> "${c.displayName} (${c.phoneNumber})" })
                    val allowed = filteredContacts.any { c ->
                        val contactNormalized = c.phoneNumber.replace(Regex("[^+0-9]"), "")
                        println("[DEBUG] Comparing incoming $normalizedNumber to contact ${c.phoneNumber} (normalized: $contactNormalized)")
                        contactNormalized.endsWith(normalizedNumber) || normalizedNumber.endsWith(contactNormalized)
                    }
                    println("[DEBUG] Allowed by $currentMode: $allowed")
                    allowed
                }
            }
        }
        if (!allowedByMode) {
            println("[DEBUG] Call REJECTED by mode filter.")
            return CallDecision.REJECT
        }

        // 2. Get spam detection result
        val spamResult = detectSpamUseCase(phoneNumber)

        // 3. Check auto-reject spam/unknown settings
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
