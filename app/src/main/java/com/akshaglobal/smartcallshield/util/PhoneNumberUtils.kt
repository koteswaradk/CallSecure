package com.akshaglobal.smartcallshield.util

object PhoneNumberUtils {
    // Simple normalization: keep leading + and digits, remove other characters
    fun normalize(phoneNumber: String?): String {
        if (phoneNumber.isNullOrBlank()) return ""
        // Remove common extension markers like ext, x, etc
        val withoutExt = phoneNumber.replace(Regex("(ext\\.|ext|x)\\s*\\d+", RegexOption.IGNORE_CASE), "")
        val normalized = withoutExt.trim().replace(Regex("[^+0-9]"), "")
        return normalized
    }
}

