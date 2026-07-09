package com.siae.biometricsiae.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeviceConfigResponse(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("name") val name: String,
    @SerializedName("branchId") val branchId: String,
    @SerializedName("type") val type: String,
    @SerializedName("features") val features: DeviceFeatures,
    @SerializedName("policies") val policies: DevicePolicies,
    @SerializedName("ui") val ui: DeviceUiConfig
)

data class DeviceFeatures(
    @SerializedName("faceRequired") val faceRequired: Boolean,
    @SerializedName("geofenceEnabled") val geofenceEnabled: Boolean,
    @SerializedName("locationRequired") val locationRequired: Boolean,
    @SerializedName("kioskMode") val kioskMode: Boolean
)

data class DevicePolicies(
    @SerializedName("duplicateWindowMinutes") val duplicateWindowMinutes: Int,
    @SerializedName("maxRetries") val maxRetries: Int,
    @SerializedName("lockoutAfterFailedAttempts") val lockoutAfterFailedAttempts: Int
)

data class DeviceUiConfig(
    @SerializedName("theme") val theme: String,
    @SerializedName("language") val language: String,
    @SerializedName("showEmployeeList") val showEmployeeList: Boolean
)
