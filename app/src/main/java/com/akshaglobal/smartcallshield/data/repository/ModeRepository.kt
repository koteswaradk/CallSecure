package com.akshaglobal.smartcallshield.data.repository

import com.akshaglobal.smartcallshield.data.dao.ModeDao
import com.akshaglobal.smartcallshield.data.dao.ContactDao
import com.akshaglobal.smartcallshield.data.model.ModeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModeRepository @Inject constructor(
    private val modeDao: ModeDao,
    private val contactDao: ContactDao
) {
    suspend fun createMode(mode: ModeEntity): Long = modeDao.insertMode(mode)

    fun getActiveMode(): Flow<ModeEntity?> = modeDao.getActiveMode()

    suspend fun setActiveMode(modeId: Long) = modeDao.setActiveMode(modeId)

    suspend fun addContactToMode(modeId: Long, contactId: Long) =
        modeDao.insertModeContactCrossRef(com.akshaglobal.smartcallshield.data.model.ModeContactCrossRef(modeId, contactId))

    suspend fun removeContactFromMode(modeId: Long, contactId: Long) =
        modeDao.removeModeContactCrossRef(modeId, contactId)

    suspend fun isPhoneAllowedInActiveMode(phoneNumber: String): Boolean {
        val active = modeDao.getActiveMode().first()
        if (active == null) return true // no active mode => allow

        // For EMERGENCY and FAMILY semantics, we'll check ContactEntity flags
        val contact = contactDao.getContactByPhoneNumber(phoneNumber).first()
        return when (active.name.uppercase()) {
            "EMERGENCY" -> contact?.isEmergency == true
            "FAMILY" -> contact?.category == "FAMILY" || contact?.isEmergency == true
            "DRIVING" -> contact?.isEmergency == true || contact?.category == "FAMILY"
            else -> true
        }
    }
}

