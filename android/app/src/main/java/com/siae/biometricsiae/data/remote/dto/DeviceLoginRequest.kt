package com.siae.biometricsiae.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeviceLoginRequest(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceName") val deviceName: String,
    @SerializedName("branchId") val branchId: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("fcmToken") val fcmToken: String?,
    @SerializedName("capabilities") val capabilities: List<String>
)
