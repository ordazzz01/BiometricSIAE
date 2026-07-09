package com.siae.biometricsiae.domain.model

data class AttendanceRecord(
    val id: String,
    val tenantId: String,
    val employeeId: String,
    val employeeName: String,
    val branchId: String,
    val branchName: String,
    val deviceId: String,
    val type: AttendanceType,
    val timestamp: String,
    val timezone: String,
    val latitude: Double?,
    val longitude: Double?,
    val method: AuthMethod,
    val faceEvidenceUrl: String?,
    val biometricAuthEventId: String?,
    val syncStatus: SyncStatus,
    val serverTimestamp: String?,
    val hash: String,
    val appVersion: String,
    val offline: Boolean,
    val retryCount: Int = 0,
    val createdAt: String,
    val updatedAt: String
)

enum class AttendanceType {
    ENTRY,
    EXIT,
    BREAK,
    BREAK_RETURN
}

enum class AuthMethod {
    BIOMETRIC,
    FACE,
    PIN_FALLBACK,
    QR,
    MANUAL
}

enum class SyncStatus {
    PENDING,
    SYNCED,
    CONFLICT,
    FAILED
}
