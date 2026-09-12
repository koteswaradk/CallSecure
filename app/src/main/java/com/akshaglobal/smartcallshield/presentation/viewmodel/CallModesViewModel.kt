package com.akshaglobal.smartcallshield.presentation.viewmodel

import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshaglobal.smartcallshield.data.contacts.DeviceContactsProvider
import com.akshaglobal.smartcallshield.data.model.DeviceContact
import com.akshaglobal.smartcallshield.data.model.ModeEntity
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.data.repository.ModeRepository
import com.akshaglobal.smartcallshield.data.repository.ContactRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.akshaglobal.smartcallshield.util.PhoneNumberUtils

@HiltViewModel
class CallModesViewModel @Inject constructor(
    private val deviceContactsProvider: DeviceContactsProvider,
    private val modeRepository: ModeRepository,
    private val contactRepository: ContactRepository
) : ViewModel() {

    private val _deviceContacts = MutableStateFlow<List<DeviceContact>>(emptyList())
    val deviceContacts: StateFlow<List<DeviceContact>> = _deviceContacts.asStateFlow()

    private val _modes = MutableStateFlow<List<ModeEntity>>(emptyList())
    val modes: StateFlow<List<ModeEntity>> = _modes.asStateFlow()

    private val _enabledModes = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val enabledModes: StateFlow<Map<String, Boolean>> = _enabledModes.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadModes()
        // Observe contacts table and update enabledModes on any change
        viewModelScope.launch {
            contactRepository.getAllContacts().collect {
                updateEnabledModes()
            }
        }
    }

    fun loadDeviceContacts() {
        viewModelScope.launch {
            try {
                val contacts = deviceContactsProvider.fetchDeviceContacts()
                _deviceContacts.value = contacts
            } catch (e: SecurityException) {
                // Notify UI to show permission request or error message
                _deviceContacts.value = emptyList()
                _errorMessage.value = "Permission to read contacts is required. Please grant access in settings."
            }
        }
    }

    private fun updateEnabledModes() {
        viewModelScope.launch {
            val modeMap = mutableMapOf<String, Boolean>()
            for (mode in _modes.value) {
                val category = mode.name.trim().uppercase()
                if (category == "NORMAL") {
                    // Always enable NORMAL if mode exists
                    modeMap[category] = true
                } else {
                    val contacts = contactRepository.getContactsByCategory(category).firstOrNull() ?: emptyList()
                    // Check contact category robustly
                    val hasContacts = contacts.any { it.category.trim().uppercase() == category }
                    modeMap[category] = hasContacts
                }
            }
            _enabledModes.value = modeMap
        }
    }

    fun loadModes() {
        viewModelScope.launch {
            try {
                modeRepository.getActiveMode() // just to ensure DB is initialized
                // Load all modes
                val allModes = modeRepository.getAllModes().firstOrNull() ?: emptyList()
                _modes.value = allModes
                updateEnabledModes()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun createPredefinedMode(name: String) {
        viewModelScope.launch {
            try {
                // Check if mode already exists
                val existingModes = _modes.value
                if (existingModes.any { it.name.equals(name, ignoreCase = true) }) {
                    return@launch // Mode already exists
                }

                val mode = ModeEntity(name = name, isActive = false)
                val modeId = modeRepository.createMode(mode)

                // Add to local state
                val updatedModes = _modes.value.toMutableList()
                updatedModes.add(mode.copy(id = modeId))
                _modes.value = updatedModes
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun addContactToModeByPhone(modeId: Long, phoneNumber: String, category: String = "FAMILY") {
        viewModelScope.launch {
            try {
                val normalizedPhone = PhoneNumberUtils.normalize(phoneNumber)
                val existingList = contactRepository.getContactsByCategory(category.uppercase()).firstOrNull() ?: emptyList()
                val duplicate = existingList.any { it.phoneNumber == normalizedPhone }
                if (duplicate) {
                    _toastEvent.emit("CONTACTS TRYING TO ADD ALREADY EXISTS in the TABLE")
                    return@launch
                }
                val contact = ContactEntity(
                    phoneNumber = normalizedPhone,
                    displayName = _deviceContacts.value.find { PhoneNumberUtils.normalize(it.phoneNumber) == normalizedPhone }?.displayName ?: "Unknown",
                    category = category,
                    isEmergency = (category.uppercase() == "EMERGENCY")
                )
                val contactId = contactRepository.addContact(contact)
                modeRepository.addContactToMode(modeId, contactId)
                updateEnabledModes()
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun createMode(name: String, selectedContactPhones: Set<String> = emptySet()) {
        viewModelScope.launch {
            try {
                val mode = ModeEntity(name = name, isActive = false)
                val modeId = modeRepository.createMode(mode)

                // Map mode name to contact category and emergency flag
                val (category, isEmergency) = when (name.uppercase()) {
                    "FAMILY" -> "FAMILY" to false
                    "EMERGENCY" -> "EMERGENCY" to true
                    "NORMAL" -> "NORMAL" to false
                    else -> "NORMAL" to false
                }

                // Add selected contacts to the mode
                selectedContactPhones.forEach { phoneNumber ->
                    try {
                        val normalizedPhone = PhoneNumberUtils.normalize(phoneNumber)
                        val existingList = contactRepository.getContactsByCategory(category.uppercase()).firstOrNull() ?: emptyList()
                        val duplicate = existingList.any { it.phoneNumber == normalizedPhone }
                        if (duplicate) {
                            _toastEvent.emit("CONTACTS TRYING TO ADD ALREADY EXISTS in THE TABLE")
                            return@forEach
                        }
                        val contact = ContactEntity(
                            phoneNumber = normalizedPhone,
                            displayName = _deviceContacts.value.find { PhoneNumberUtils.normalize(it.phoneNumber) == normalizedPhone }?.displayName ?: "Unknown",
                            category = category,
                            isEmergency = isEmergency
                        )
                        val contactId = contactRepository.addContact(contact)
                        modeRepository.addContactToMode(modeId, contactId)
                    } catch (e: Exception) {
                        // Log error silently
                    }
                }

                // Add to local state
                val updatedModes = _modes.value.toMutableList()
                updatedModes.add(mode.copy(id = modeId))
                _modes.value = updatedModes
                updateEnabledModes()
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun setActiveMode(modeId: Long) {
        viewModelScope.launch {
            modeRepository.setActiveMode(modeId)
            loadModes() // Refresh modes state after changing active mode
            // Ensure all contacts with the correct category are associated with the mode
            val mode = _modes.value.find { it.id == modeId }
            if (mode != null) {
                ensureContactsAssociatedWithMode(mode.name)
            }
        }
    }

    fun addContactToMode(modeId: Long, contactId: Long) {
        viewModelScope.launch {
            modeRepository.addContactToMode(modeId, contactId)
        }
    }

    fun removeContactFromMode(modeId: Long, contactId: Long) {
        viewModelScope.launch {
            modeRepository.removeContactFromMode(modeId, contactId)
            updateEnabledModes()
        }
    }

    suspend fun getModeWithContacts(modeId: Long): com.akshaglobal.smartcallshield.data.model.ModeWithContacts? {
        return modeRepository.getModeWithContacts(modeId)
    }

    fun ensureContactsAssociatedWithMode(modeName: String) {
        viewModelScope.launch {
            val mode = _modes.value.find { it.name.equals(modeName, ignoreCase = true) }
            if (mode != null) {
                val contacts = contactRepository.getContactsByCategory(modeName.uppercase()).firstOrNull() ?: emptyList()
                val modeWithContacts = getModeWithContacts(mode.id)
                val alreadyAssociatedIds = modeWithContacts?.contacts?.map { it.id }?.toSet() ?: emptySet()
                for (contact in contacts) {
                    if (contact.id !in alreadyAssociatedIds) {
                        modeRepository.addContactToMode(mode.id, contact.id)
                    }
                }
                updateEnabledModes()
            }
        }
    }

    fun printModesAndContacts() {
        viewModelScope.launch {
            for (mode in _modes.value) {
                val modeWithContacts = getModeWithContacts(mode.id)
                val contactList = modeWithContacts?.contacts?.joinToString { it.displayName + ":" + it.phoneNumber } ?: "None"
                println("Mode: ${mode.name}, Contacts: $contactList")
            }
        }
    }

    fun refreshEnabledModes() {
        updateEnabledModes()
    }

    suspend fun syncModesAndContacts() {
        loadDeviceContacts()
        loadModes()
        modes.value.forEach { mode ->
            ensureContactsAssociatedWithMode(mode.name)
        }
        refreshEnabledModes()
    }

    fun createOrActivateMode(name: String) {
        viewModelScope.launch {
            // Check if mode exists
            val existing = _modes.value.find { it.name.equals(name, ignoreCase = true) }
            if (existing == null) {
                // Create the mode (no contacts by default)
                createMode(name)
                // Wait for mode to be created and loaded
                loadModes()
                val newMode = _modes.value.find { it.name.equals(name, ignoreCase = true) }
                if (newMode != null) {
                    modeRepository.setActiveMode(newMode.id)
                }
            } else {
                modeRepository.setActiveMode(existing.id)
            }
            // Refresh modes state
            loadModes()
        }
    }
}
