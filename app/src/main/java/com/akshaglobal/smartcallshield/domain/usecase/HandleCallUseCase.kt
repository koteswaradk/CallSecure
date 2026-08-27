package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.data.repository.ContactRepository
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.data.repository.ModeRepository
import com.akshaglobal.smartcallshield.data.contacts.DeviceContactsProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class HandleCallUseCase @Inject constructor(
    private val detectSpamUseCase: DetectSpamUseCase,
    private val preferencesManager: PreferencesManager,
    private val modeRepository: ModeRepository,
    private val deviceContactsProvider: DeviceContactsProvider
) {
    suspend operator fun invoke(phoneNumber: String): CallDecision {
        val appEnabled = preferencesManager.isAppEnabled.first()
        if (!appEnabled) {
            println("[DEBUG] App is disabled, allowing all calls.")
            return CallDecision.ALLOW
        }
        val currentMode = preferencesManager.currentMode.first().uppercase()
        val normalizedNumber = phoneNumber.replace(Regex("[^+0-9]"), "")
        println("[DEBUG] Current mode: $currentMode, Incoming: $normalizedNumber")


        if (currentMode == "FAMILY" || currentMode == "EMERGENCY") {
            val activeMode = modeRepository.getActiveMode().first()
            if (activeMode != null && activeMode.name.uppercase() == currentMode) {
                val modeWithContacts = modeRepository.getModeWithContacts(activeMode.id)
                val contacts = modeWithContacts?.contacts ?: emptyList()
                val isListedContact = contacts.any { c ->
                    val contactNormalized = c.phoneNumber.replace(Regex("[^+0-9]"), "")
                    contactNormalized.endsWith(normalizedNumber) || normalizedNumber.endsWith(contactNormalized)
                }
                
                return if (isListedContact) {
                    println("[DEBUG] Contact listed in $currentMode. Decision: ALLOW")
                    CallDecision.ALLOW
                } else {
                    println("[DEBUG] Contact NOT listed in $currentMode. Decision: REJECT")
                    CallDecision.REJECT
                }
            }
        }

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
                    true
                }
            }
            else -> {
                val activeMode = modeRepository.getActiveMode().first()
                if (activeMode == null) {
                    println("[DEBUG] No active mode found!")
                    true
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
        val spamResult = detectSpamUseCase(phoneNumber)
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
