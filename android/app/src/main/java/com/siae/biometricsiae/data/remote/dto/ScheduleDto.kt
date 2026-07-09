package com.siae.biometricsiae.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ScheduleDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("branchId") val branchId: String?,
    @SerializedName("rules") val rules: List<ScheduleRuleDto>,
    @SerializedName("toleranceMinutes") val toleranceMinutes: Int,
    @SerializedName("active") val active: Boolean,
    @SerializedName("createdAt") val createdAt: String
)

data class ScheduleRuleDto(
    @SerializedName("day") val day: Int,
    @SerializedName("entryTime") val entryTime: String,
    @SerializedName("exitTime") val exitTime: String,
    @SerializedName("breakStartTime") val breakStartTime: String?,
    @SerializedName("breakEndTime") val breakEndTime: String?
)
