package com.siae.biometricsiae.domain.model

data class Employee(
    val id: String,
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
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String
)
