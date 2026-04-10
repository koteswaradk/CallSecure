package com.akshaglobal.smartcallshield.data.repository

import com.akshaglobal.smartcallshield.data.dao.ContactDao
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val contactDao: ContactDao
) {
    suspend fun addContact(contact: ContactEntity) = contactDao.insertContact(contact)
    suspend fun updateContact(contact: ContactEntity) = contactDao.updateContact(contact)
    suspend fun deleteContact(contact: ContactEntity) = contactDao.deleteContact(contact)
    fun getContactByPhoneNumber(phoneNumber: String): Flow<ContactEntity?> =
        contactDao.getContactByPhoneNumber(phoneNumber)
    fun getContactsByCategory(category: String): Flow<List<ContactEntity>> =
        contactDao.getContactsByCategory(category)
    fun getEmergencyContacts(): Flow<List<ContactEntity>> =
        contactDao.getEmergencyContacts()
    fun getAllContacts(): Flow<List<ContactEntity>> =
        contactDao.getAllContacts()
    suspend fun deleteByPhoneNumber(phoneNumber: String) =
        contactDao.deleteByPhoneNumber(phoneNumber)
    fun isWhitelisted(phoneNumber: String): Flow<Boolean> =
        contactDao.getContactByPhoneNumber(phoneNumber).map { contact ->
            contact != null && contact.category == "WHITELIST"
        }
    fun isBlacklisted(phoneNumber: String): Flow<Boolean> =
        contactDao.getContactByPhoneNumber(phoneNumber).map { contact ->
            contact != null && contact.category == "BLACKLIST"
        }
}

