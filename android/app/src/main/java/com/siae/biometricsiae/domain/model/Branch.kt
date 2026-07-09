package com.siae.biometricsiae.domain.model

data class Branch(
    val id: String,
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
