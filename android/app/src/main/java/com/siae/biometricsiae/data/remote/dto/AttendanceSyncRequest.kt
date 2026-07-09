package com.siae.biometricsiae.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AttendanceSyncRequest(
    @SerializedName("records") val records: List<CheckinRequest>
)
