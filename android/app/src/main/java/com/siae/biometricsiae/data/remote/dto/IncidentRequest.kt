package com.siae.biometricsiae.data.remote.dto

import com.google.gson.annotations.SerializedName

data class IncidentRequest(
    @SerializedName("employeeId") val employeeId: String,
    @SerializedName("branchId") val branchId: String,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String,
    @SerializedName("authorizedBy") val authorizedBy: String?,
    @SerializedName("timestamp") val timestamp: String
)
