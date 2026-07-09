package com.siae.biometricsiae.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String,
    val tenantId: String,
    val code: String,
    val name: String,
    val department: String?,
    val position: String?,
    val photoUrl: String?,
    val branchIds: List<String>,
    val scheduleId: String?,
    val biometricEnrolled: Boolean,
    val faceRegistered: Boolean,
    val pin: String?,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String
)
