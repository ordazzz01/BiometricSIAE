package com.siae.biometricsiae.domain.model

data class FaceEvidence(
    val id: String,
    val tenantId: String,
    val attendanceRecordId: String,
    val localPath: String,
    val remoteUrl: String?,
    val faceDetected: Boolean,
    val faceCentered: Boolean,
    val eyesVisible: Boolean,
    val livenessScore: Float?,
    val compressedSizeBytes: Long,
    val syncStatus: SyncStatus,
    val createdAt: String
)
