package com.siae.biometricsiae.data

import com.google.firebase.Timestamp

data class EnrollmentEvent(
    val id: String = "",
    val personId: String = "",
    val rfc: String = "",
    val fullNameSnapshot: String = "",
    val deviceId: String = "",
    val eventType: String = "enroll",
    val authResult: String = "success",
    val timestamp: Timestamp = Timestamp.now(),
    val appVersion: String = "1.0.0"
)
