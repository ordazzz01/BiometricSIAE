package com.siae.biometricsiae.data

import com.google.firebase.Timestamp

data class Person(
    val id: String = "",
    val fullName: String = "",
    val rfc: String = "",
    val biometricEnabled: Boolean = false,
    val status: String = "active",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val createdByDeviceId: String = "",
    val lastUpdatedByDeviceId: String = ""
)
