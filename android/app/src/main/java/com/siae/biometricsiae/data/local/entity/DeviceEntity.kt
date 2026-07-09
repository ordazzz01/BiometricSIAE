package com.siae.biometricsiae.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val name: String,
    val branchId: String,
    val type: String,
    val biometricCapabilities: List<String>,
    val cameraAvailable: Boolean,
    val gpsAvailable: Boolean,
    val lastSyncAt: String?,
    val active: Boolean,
    val configJson: String?,
    val createdAt: String
)
