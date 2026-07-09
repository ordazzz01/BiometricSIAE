package com.siae.biometricsiae.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EmployeeDto(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("department") val department: String?,
    @SerializedName("position") val position: String?,
    @SerializedName("photoUrl") val photoUrl: String?,
    @SerializedName("branchIds") val branchIds: List<String>,
    @SerializedName("scheduleId") val scheduleId: String?,
    @SerializedName("biometricEnrolled") val biometricEnrolled: Boolean,
    @SerializedName("faceRegistered") val faceRegistered: Boolean,
    @SerializedName("active") val active: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)
