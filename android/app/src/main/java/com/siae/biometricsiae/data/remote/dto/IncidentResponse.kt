package com.siae.biometricsiae.data.remote.dto

import com.google.gson.annotations.SerializedName

data class IncidentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("incidentId") val incidentId: String?,
    @SerializedName("error") val error: String?
)
