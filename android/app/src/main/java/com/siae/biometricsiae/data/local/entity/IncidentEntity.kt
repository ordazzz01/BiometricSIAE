package com.siae.biometricsiae.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val employeeId: String,
    val branchId: String,
    val deviceId: String,
    val type: String,
    val description: String,
    val authorizedBy: String?,
    val status: String,
    val syncStatus: String,
    val createdAt: String
)
