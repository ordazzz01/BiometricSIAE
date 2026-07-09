package com.siae.biometricsiae.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val name: String,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val geofenceRadiusMeters: Int?,
    val timezone: String,
    val active: Boolean,
    val createdAt: String
)
