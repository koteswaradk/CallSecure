package com.akshaglobal.smartcallshield.presentation.viewmodel

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
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    init {
        loadModes()
    }

    fun loadDeviceContacts() {
        viewModelScope.launch {
            val contacts = deviceContactsProvider.fetchDeviceContacts()
            _deviceContacts.value = contacts
        }
    }

    fun loadModes() {
        viewModelScope.launch {
            try {
                modeRepository.getActiveMode() // just to ensure DB is initialized
                // Load all modes
                val allModes = mutableListOf<ModeEntity>()
                _modes.value = allModes
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

    fun createMode(name: String, selectedContactPhones: Set<String> = emptySet()) {
        viewModelScope.launch {
            try {
                val mode = ModeEntity(name = name, isActive = false)
                val modeId = modeRepository.createMode(mode)

                // Add selected contacts to the mode
                selectedContactPhones.forEach { phoneNumber ->
                    try {
                        val normalizedPhone = phoneNumber.replace(Regex("[^+0-9]"), "")
                        // Create or find contact and associate with mode
                        val contact = ContactEntity(
                            phoneNumber = normalizedPhone,
                            displayName = _deviceContacts.value.find { it.phoneNumber == normalizedPhone }?.displayName ?: "Unknown",
                            category = "FAMILY",
                            isEmergency = false
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
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun addContactToModeByPhone(modeId: Long, phoneNumber: String) {
        viewModelScope.launch {
            try {
                val normalizedPhone = phoneNumber.replace(Regex("[^+0-9]"), "")
                val contact = ContactEntity(
                    phoneNumber = normalizedPhone,
                    displayName = _deviceContacts.value.find { it.phoneNumber == normalizedPhone }?.displayName ?: "Unknown",
                    category = "FAMILY",
                    isEmergency = false
                )
                val contactId = contactRepository.addContact(contact)
                modeRepository.addContactToMode(modeId, contactId)
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    fun setActiveMode(modeId: Long) {
        viewModelScope.launch {
            modeRepository.setActiveMode(modeId)
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
        }
    }
}


