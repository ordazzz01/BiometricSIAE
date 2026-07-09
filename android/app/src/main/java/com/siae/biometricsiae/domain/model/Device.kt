package com.siae.biometricsiae.domain.model

data class Device(
    val id: String,
    val tenantId: String,
    val name: String,
    val branchId: String,
    val type: DeviceType,
    val biometricCapabilities: List<BioCapability>,
    val cameraAvailable: Boolean,
    val gpsAvailable: Boolean,
    val lastSyncAt: String?,
    val active: Boolean,
    val configJson: String?,
    val createdAt: String
)

enum class DeviceType {
    KIOSK,
    MOBILE,
    TABLET
}

enum class BioCapability {
    FINGERPRINT,
    FACE,
    NONE
}
