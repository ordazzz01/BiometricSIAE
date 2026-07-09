package com.siae.biometricsiae.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "face_evidence")
data class FaceEvidenceEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val attendanceRecordId: String,
    val localPath: String,
    val remoteUrl: String?,
    val faceDetected: Boolean,
    val faceCentered: Boolean,
    val eyesVisible: Boolean,
    val livenessScore: Float?,
    val compressedSizeBytes: Long,
    val syncStatus: String,
    val createdAt: String
)
