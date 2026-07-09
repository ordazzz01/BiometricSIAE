package com.siae.biometricsiae.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CheckinResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("recordId") val recordId: String?,
    @SerializedName("serverTimestamp") val serverTimestamp: String?,
    @SerializedName("duplicate") val duplicate: Boolean,
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("warnings") val warnings: List<String>?
)
