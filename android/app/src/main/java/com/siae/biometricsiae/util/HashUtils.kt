package com.siae.biometricsiae.util

import java.security.MessageDigest
import java.security.SecureRandom

object HashUtils {

    fun generateHash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateIdempotencyKey(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32).map { chars.random() }.joinToString("")
    }

    fun verifyHash(input: String, hash: String): Boolean {
        return generateHash(input) == hash
    }

    fun generateDeviceId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..16).map { chars.random() }.joinToString("")
    }
}
