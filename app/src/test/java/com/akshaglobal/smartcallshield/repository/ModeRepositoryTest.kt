package com.akshaglobal.smartcallshield.repository

import com.akshaglobal.smartcallshield.data.model.ModeEntity
import com.akshaglobal.smartcallshield.data.model.ContactEntity
import com.akshaglobal.smartcallshield.data.repository.ModeRepository
import com.akshaglobal.smartcallshield.data.dao.ModeDao
import com.akshaglobal.smartcallshield.data.dao.ContactDao
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class ModeRepositoryTest {
    @Test
    fun isPhoneAllowedInActiveMode_checksEmergencyAndFamily() = runBlocking {
        val modeDao = mockk<ModeDao>()
        val contactDao = mockk<ContactDao>()

        // Active family mode
        coEvery { modeDao.getActiveMode() } returns flow { emit(ModeEntity(id = 1, name = "FAMILY", isActive = true)) }

        // Contacts
        val familyContact = ContactEntity(id = 1, phoneNumber = "+123", displayName = "A", category = "FAMILY", isEmergency = false)
        val emergencyContact = ContactEntity(id = 2, phoneNumber = "+999", displayName = "E", category = "WHITELIST", isEmergency = true)

        coEvery { contactDao.getContactByPhoneNumber("+123") } returns flow { emit(familyContact) }
        coEvery { contactDao.getContactByPhoneNumber("+999") } returns flow { emit(emergencyContact) }
        coEvery { contactDao.getContactByPhoneNumber("+444") } returns flow { emit(null) }

        val repo = ModeRepository(modeDao, contactDao)

        val allowedFamily = repo.isPhoneAllowedInActiveMode("+123")
        assertTrue(allowedFamily)

        val allowedEmergency = repo.isPhoneAllowedInActiveMode("+999")
        assertTrue(allowedEmergency)

        val notAllowed = repo.isPhoneAllowedInActiveMode("+444")
        assertTrue(!notAllowed)
    }
}
