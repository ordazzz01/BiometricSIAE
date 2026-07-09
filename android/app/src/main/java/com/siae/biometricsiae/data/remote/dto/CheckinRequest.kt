package com.siae.biometricsiae.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CheckinRequest(
    @SerializedName("employeeId") val employeeId: String,
    @SerializedName("branchId") val branchId: String,
    @SerializedName("type") val type: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("method") val method: String,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("location") val location: LocationDto?,
    @SerializedName("faceEvidenceId") val faceEvidenceId: String?,
    @SerializedName("hash") val hash: String
)

data class LocationDto(
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lng") val longitude: Double
)
