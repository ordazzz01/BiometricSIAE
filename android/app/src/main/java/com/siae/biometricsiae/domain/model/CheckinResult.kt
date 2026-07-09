package com.siae.biometricsiae.domain.model

sealed class CheckinResult {
    data class Success(
        val record: AttendanceRecord,
        val isDuplicate: Boolean = false
    ) : CheckinResult()

    data class Duplicate(
        val existingRecord: AttendanceRecord
    ) : CheckinResult()

    data class Error(
        val message: String,
        val code: String
    ) : CheckinResult()

    data class Offline(
        val record: AttendanceRecord
    ) : CheckinResult()

    data class RequiresPin(
        val reason: String
    ) : CheckinResult()
}
