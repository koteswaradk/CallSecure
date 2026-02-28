package com.akshaglobal.smartcallshield.usecase

import com.akshaglobal.smartcallshield.domain.usecase.DetectSpamUseCase
import com.akshaglobal.smartcallshield.domain.usecase.HandleCallUseCase
import com.akshaglobal.smartcallshield.domain.usecase.CallDecision
import com.akshaglobal.smartcallshield.data.repository.CallLogRepository
import com.akshaglobal.smartcallshield.data.repository.ContactRepository
import com.akshaglobal.smartcallshield.data.repository.SpamReportRepository
import com.akshaglobal.smartcallshield.data.preferences.PreferencesManager
import com.akshaglobal.smartcallshield.service.ai.SpamDetectionModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class HandleCallUseCaseTest {
    @Test
    fun whenNumberIsSpam_andAutoReject_thenReject() = runBlocking {
        // Mock dependencies
        val spamModel = mockk<SpamDetectionModel>()
        coEvery { spamModel.detectSpam(any()) } returns com.akshaglobal.smartcallshield.data.model.SpamDetectionResult(
            phoneNumber = "+123",
            isSpam = true,
            confidence = 0.9f,
            category = "SCAM"
        )

        val contactRepo = mockk<ContactRepository>()
        coEvery { contactRepo.isWhitelisted(any()) } returns flow { emit(false) }
        coEvery { contactRepo.isBlacklisted(any()) } returns flow { emit(false) }

        val spamReportRepo = mockk<SpamReportRepository>()
        coEvery { spamReportRepo.getSpamReport(any()) } returns flow { emit(null) }

        val prefs = mockk<PreferencesManager>()
        coEvery { prefs.autoRejectSpam } returns flow { emit(true) }
        coEvery { prefs.autoRejectUnknown } returns flow { emit(false) }
        coEvery { prefs.spamConfidenceThreshold } returns flow { emit(0.5f) }

        val detectUseCase = DetectSpamUseCase(spamModel, contactRepo, spamReportRepo, prefs)
        val callLogRepo = mockk<CallLogRepository>(relaxed = true)
        val modeRepo = mockk<com.akshaglobal.smartcallshield.data.repository.ModeRepository>()
        coEvery { modeRepo.isPhoneAllowedInActiveMode(any()) } returns true

        val handle = HandleCallUseCase(detectUseCase, callLogRepo, contactRepo, prefs, modeRepo)

        val decision = handle.invoke("+123")
        assertEquals(CallDecision.REJECT, decision)
    }
}
