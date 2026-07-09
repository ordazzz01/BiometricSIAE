package com.siae.biometricsiae.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val employeeId: String,
    val branchId: String,
    val deviceId: String,
    val type: String,
    val timestamp: String,
    val timezone: String,
    val latitude: Double?,
    val longitude: Double?,
    val method: String,
    val faceEvidenceUrl: String?,
    val biometricAuthEventId: String?,
    val syncStatus: String,
    val serverTimestamp: String?,
    val hash: String,
    val appVersion: String,
    val offline: Boolean,
    val retryCount: Int = 0,
    val createdAt: String,
    val updatedAt: String
)
