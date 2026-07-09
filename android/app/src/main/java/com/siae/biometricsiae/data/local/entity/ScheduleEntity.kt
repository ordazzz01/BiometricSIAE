package com.siae.biometricsiae.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val name: String,
    val branchId: String?,
    val rules: String,
    val toleranceMinutes: Int,
    val active: Boolean,
    val createdAt: String
)
