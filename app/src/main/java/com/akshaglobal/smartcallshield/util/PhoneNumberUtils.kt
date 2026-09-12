package com.akshaglobal.smartcallshield.util

object PhoneNumberUtils {
    fun normalize(phoneNumber: String?): String {
        if (phoneNumber.isNullOrBlank()) return ""
        val withoutExt = phoneNumber.replace(Regex("(ext\\.|ext|x)\\s*\\d+", RegexOption.IGNORE_CASE), "")
        val normalized = withoutExt.trim().replace(Regex("[^+0-9]"), "")
        return normalized
    }
}

