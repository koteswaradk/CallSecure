package com.akshaglobal.smartcallshield.service

import android.telecom.CallScreeningService
import android.telecom.Call
import com.akshaglobal.smartcallshield.domain.usecase.HandleCallUseCase
import com.akshaglobal.smartcallshield.domain.usecase.CallDecision
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class SmartCallScreeningService : CallScreeningService() {
    @Inject
    lateinit var handleCallUseCase: HandleCallUseCase

    override fun onScreenCall(callDetails: Call.Details) {
        android.util.Log.d("SmartCallScreeningService", "onScreenCall invoked for: ${callDetails.handle?.schemeSpecificPart}")
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""
        val shouldBlock = runBlocking {
            handleCallUseCase(phoneNumber) == CallDecision.REJECT
        }
        android.util.Log.d("SmartCallScreeningService", "shouldBlock=$shouldBlock for $phoneNumber")
        val response = CallResponse.Builder()
            .setDisallowCall(shouldBlock)
            .setRejectCall(shouldBlock)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()
        android.util.Log.d("SmartCallScreeningService", "respondToCall: disallow=$shouldBlock reject=$shouldBlock")
        respondToCall(callDetails, response)
    }
}
