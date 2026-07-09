package com.siae.biometricsiae.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthTokens(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("tenantId") val tenantId: String,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("branchId") val branchId: String
)
