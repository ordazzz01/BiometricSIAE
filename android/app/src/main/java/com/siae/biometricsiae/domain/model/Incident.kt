package com.siae.biometricsiae.domain.model

data class Incident(
    val id: String,
    val tenantId: String,
    val employeeId: String,
    val branchId: String,
    val deviceId: String,
    val type: IncidentType,
    val description: String,
    val authorizedBy: String?,
    val status: IncidentStatus,
    val syncStatus: SyncStatus,
    val createdAt: String
)

enum class IncidentType {
    MANUAL,
    LATE_ARRIVAL,
    EARLY_EXIT,
    MISSING_CHECKOUT
}

enum class IncidentStatus {
    PENDING,
    APPROVED,
    REJECTED
}
