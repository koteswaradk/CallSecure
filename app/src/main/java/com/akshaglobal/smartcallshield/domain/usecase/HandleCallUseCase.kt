package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.data.repository.ContactRepository
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.data.repository.ModeRepository
import com.akshaglobal.smartcallshield.data.contacts.DeviceContactsProvider
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class HandleCallUseCase @Inject constructor(
    private val contactRepository: ContactRepository,
    private val preferencesManager: PreferencesManager,
    private val modeRepository: ModeRepository,
    private val deviceContactsProvider: DeviceContactsProvider
) {
    suspend operator fun invoke(phoneNumber: String): CallDecision {
        val appEnabled = preferencesManager.isAppEnabled.first()
        if (!appEnabled) {
            return CallDecision.ALLOW
        }

        val currentMode = preferencesManager.currentMode.first().uppercase()
        val normalizedNumber = phoneNumber.replace(Regex("[^+0-9]"), "")
        
        // Skip processing for empty or invalid numbers
        if (normalizedNumber.isEmpty()) return CallDecision.ALLOW

        return when (currentMode) {
            "EMERGENCY" -> {
                val emergencyContacts = contactRepository.getContactsByCategory("EMERGENCY").first()
                val isEmergency = emergencyContacts.any { c ->
                    val contactNormalized = c.phoneNumber.replace(Regex("[^+0-9]"), "")
                    normalizedNumber.endsWith(contactNormalized) || contactNormalized.endsWith(normalizedNumber)
                }
                if (isEmergency) CallDecision.ALLOW else CallDecision.REJECT
            }

            "FAMILY" -> {
                val familyContacts = contactRepository.getContactsByCategory("FAMILY").first()
                val isFamily = familyContacts.any { c ->
                    val contactNormalized = c.phoneNumber.replace(Regex("[^+0-9]"), "")
                    normalizedNumber.endsWith(contactNormalized) || contactNormalized.endsWith(normalizedNumber)
                }
                if (isFamily) CallDecision.ALLOW else CallDecision.REJECT
            }

            "NORMAL" -> {
                // Check Emergency Contacts FIRST as they are likely a smaller list (DB query)
                val emergencyContacts = contactRepository.getContactsByCategory("EMERGENCY").first()
                val isEmergency = emergencyContacts.any { c ->
                    val contactNormalized = c.phoneNumber.replace(Regex("[^+0-9]"), "")
                    normalizedNumber.endsWith(contactNormalized) || contactNormalized.endsWith(normalizedNumber)
                }
                
                if (isEmergency) return CallDecision.ALLOW

                // Check Device Contacts (Cached)
                val deviceContacts = try {
                    deviceContactsProvider.fetchDeviceContacts()
                } catch (e: Exception) {
                    emptyList()
                }
                
                val isDeviceContact = deviceContacts.any {
                    // phoneNumber in DeviceContact is already partially normalized
                    normalizedNumber.endsWith(it.phoneNumber) || it.phoneNumber.endsWith(normalizedNumber)
                }

                if (isDeviceContact) {
                    CallDecision.ALLOW
                } else {
                    CallDecision.REJECT
                }
            }

            else -> {
                val activeMode = modeRepository.getActiveMode().first()
                if (activeMode != null) {
                    val modeWithContacts = modeRepository.getModeWithContacts(activeMode.id)
                    val contacts = modeWithContacts?.contacts ?: emptyList()
                    val isListed = contacts.any { c ->
                        val contactNormalized = c.phoneNumber.replace(Regex("[^+0-9]"), "")
                        normalizedNumber.endsWith(contactNormalized) || contactNormalized.endsWith(normalizedNumber)
                    }
                    if (isListed) CallDecision.ALLOW else CallDecision.REJECT
                } else {
                    CallDecision.ALLOW
                }
            }
        }
    }
}
