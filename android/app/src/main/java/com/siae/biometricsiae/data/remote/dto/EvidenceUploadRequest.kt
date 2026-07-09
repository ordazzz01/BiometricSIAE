package com.siae.biometricsiae.data.remote.dto

import com.google.gson.annotations.SerializedName

data class EvidenceUploadRequest(
    @SerializedName("attendanceRecordId") val attendanceRecordId: String,
    @SerializedName("tenantId") val tenantId: String,
    @SerializedName("employeeId") val employeeId: String,
    @SerializedName("imageBase64") val imageBase64: String,
    @SerializedName("faceDetected") val faceDetected: Boolean,
    @SerializedName("faceCentered") val faceCentered: Boolean,
    @SerializedName("eyesVisible") val eyesVisible: Boolean,
    @SerializedName("livenessScore") val livenessScore: Float?,
    @SerializedName("timestamp") val timestamp: String
)

data class EvidenceUploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("evidenceId") val evidenceId: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("error") val error: String?
)
