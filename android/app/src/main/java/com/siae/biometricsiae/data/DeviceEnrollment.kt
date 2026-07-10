package com.siae.biometricsiae.data

import com.google.firebase.Timestamp

data class DeviceEnrollment(
    val id: String = "",
    val deviceId: String = "",
    val enrolledAt: Timestamp = Timestamp.now(),
    val authMethod: String = "biometric",
    val status: String = "active",
    val appVersion: String = "1.0.0",
    val notes: String = ""
)
