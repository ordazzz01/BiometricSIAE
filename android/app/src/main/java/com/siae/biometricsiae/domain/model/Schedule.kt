package com.siae.biometricsiae.domain.model

data class Schedule(
    val id: String,
    val tenantId: String,
    val name: String,
    val branchId: String?,
    val rules: List<ScheduleRule>,
    val toleranceMinutes: Int,
    val active: Boolean,
    val createdAt: String
)

data class ScheduleRule(
    val day: Int, // 1=Monday, 7=Sunday
    val entryTime: String, // "08:00"
    val exitTime: String, // "17:00"
    val breakStartTime: String?, // "12:00"
    val breakEndTime: String? // "13:00"
)
