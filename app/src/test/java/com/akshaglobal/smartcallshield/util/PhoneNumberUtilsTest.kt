package com.akshaglobal.smartcallshield.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneNumberUtilsTest {
    @Test
    fun normalize_removesFormattingAndExtensions() {
        val input = "+1 (234) 567-8900 ext.123"
        val expected = "+12345678900"
        val out = PhoneNumberUtils.normalize(input)
        assertEquals(expected, out)
    }

    @Test
    fun normalize_handlesEmptyAndNull() {
        assertEquals("", PhoneNumberUtils.normalize(null))
        assertEquals("", PhoneNumberUtils.normalize("   "))
    }
}

