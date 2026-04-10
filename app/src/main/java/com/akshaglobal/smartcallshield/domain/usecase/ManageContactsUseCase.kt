package com.akshaglobal.smartcallshield.domain.usecase

import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.data.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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

