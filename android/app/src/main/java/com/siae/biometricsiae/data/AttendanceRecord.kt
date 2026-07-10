package com.siae.biometricsiae.data

import com.google.firebase.Timestamp

data class AttendanceRecord(
    val id: String = "",
    val employeeId: String = "",
    val employeeName: String = "",
    val rfc: String = "",
    val branchId: String = "",
    val type: String = "check_in",
    val timestamp: Timestamp = Timestamp.now(),
    val method: String = "fingerprint",
    val deviceId: String = ""
)
