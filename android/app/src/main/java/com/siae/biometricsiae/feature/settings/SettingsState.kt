package com.siae.biometricsiae.feature.settings

data class SettingsState(
    val currentBranch: String = "",
    val deviceId: String = "",
    val biometricType: String = "",
    val isKioskMode: Boolean = true,
    val faceRequired: Boolean = false,
    val geofenceEnabled: Boolean = false,
    val duplicateWindowMinutes: Int = 5,
    val isLoading: Boolean = false,
    val error: String? = null
)
