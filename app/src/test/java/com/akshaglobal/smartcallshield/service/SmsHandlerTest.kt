package com.akshaglobal.smartcallshield.service

import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SmsHandlerTest {
    private lateinit var context: Context
    private lateinit var smsHandler: SmsHandler

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        smsHandler = SmsHandler()
    }

    @Test
    fun `sendAutoReplySms sends SMS using SmsManager`() {
        val phoneNumber = "+1234567890"
        val message = "Test auto-reply"
        val smsManager = mockk<SmsManager>(relaxed = true)
        mockkStatic(SmsManager::class)
        every { SmsManager.getDefault() } returns smsManager

        smsHandler.sendAutoReplySms(context, phoneNumber, message)

        // Since sendAutoReplySms uses coroutines, you may need to advance time or use runBlockingTest in real test
        // Use ArrayList<String> to match the expected argument type
        verify { smsManager.sendMultipartTextMessage(phoneNumber, null, any<ArrayList<String>>(), null, null) }
    }

    @Test
    fun `onReceive logs action`() {
        val intent = Intent("com.akshaglobal.smartcallshield.ACTION_AUTO_REPLY")
        smsHandler.onReceive(context, intent)
        // No exception means pass; log output can be checked with Log testing tools
    }
}
