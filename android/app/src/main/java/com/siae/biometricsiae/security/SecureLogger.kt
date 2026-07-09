package com.siae.biometricsiae.security

import android.util.Log
import com.siae.biometricsiae.BuildConfig

object SecureLogger {
    private const val TAG = "BiometricSIAE"
    private val sensitivePatterns = listOf(
        Regex("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), // Email
        Regex("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"), // Credit card
        Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b"), // SSN
        Regex("Bearer\\s+[A-Za-z0-9\\-._~+/]+=*"), // JWT tokens
        Regex("password\\s*[=:]\\s*\\S+", RegexOption.IGNORE_CASE) // Passwords
    )

    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, redactSensitive(message))
        }
    }

    fun i(message: String, tag: String = TAG) {
        Log.i(tag, redactSensitive(message))
    }

    fun w(message: String, tag: String = TAG) {
        Log.w(tag, redactSensitive(message))
    }

    fun e(message: String, tag: String = TAG, throwable: Throwable? = null) {
        Log.e(tag, redactSensitive(message), throwable)
    }

    private fun redactSensitive(message: String): String {
        var redacted = message
        for (pattern in sensitivePatterns) {
            redacted = pattern.replace(redacted) { matchResult ->
                val matched = matchResult.value
                if (matched.length > 8) {
                    matched.take(4) + "*".repeat(matched.length - 8) + matched.takeLast(4)
                } else {
                    "*".repeat(matched.length)
                }
            }
        }
        return redacted
    }
}
