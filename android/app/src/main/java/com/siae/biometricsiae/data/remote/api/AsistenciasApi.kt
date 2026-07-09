package com.siae.biometricsiae.data.remote.api

import com.siae.biometricsiae.data.remote.dto.AttendanceSyncRequest
import com.siae.biometricsiae.data.remote.dto.AuthTokens
import com.siae.biometricsiae.data.remote.dto.CheckinRequest
import com.siae.biometricsiae.data.remote.dto.CheckinResponse
import com.siae.biometricsiae.data.remote.dto.DeviceConfigResponse
import com.siae.biometricsiae.data.remote.dto.DeviceLoginRequest
import com.siae.biometricsiae.data.remote.dto.EvidenceUploadResponse
import com.siae.biometricsiae.data.remote.dto.IncidentRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AsistenciasApi {

    @POST("api/v1/auth/device-login")
    suspend fun deviceLogin(@Body request: DeviceLoginRequest): Response<AuthTokens>

    @POST("api/v1/auth/refresh")
    suspend fun refreshToken(@Body refreshToken: String): Response<AuthTokens>

    @POST("api/v1/checkins")
    suspend fun registerCheckin(@Body request: CheckinRequest): Response<CheckinResponse>

    @POST("api/v1/checkins/bulk-sync")
    suspend fun bulkSync(@Body request: AttendanceSyncRequest): Response<List<CheckinResponse>>

    @GET("api/v1/employees/{id}")
    suspend fun getEmployee(@Path("id") employeeId: String): Response<EmployeeDto>

    @GET("api/v1/employees")
    suspend fun getEmployeesByBranch(@Query("branch") branchId: String): Response<List<EmployeeDto>>

    @GET("api/v1/device-config/{deviceId}")
    suspend fun getDeviceConfig(@Path("deviceId") deviceId: String): Response<DeviceConfigResponse>

    @POST("api/v1/incidents")
    suspend fun createIncident(@Body request: IncidentRequest): Response<IncidentResponse>

    @POST("api/v1/evidence/upload")
    suspend fun uploadEvidence(@Body evidence: EvidenceUploadRequest): Response<EvidenceUploadResponse>

    @GET("api/v1/schedules/{branchId}")
    suspend fun getSchedules(@Path("branchId") branchId: String): Response<List<ScheduleDto>>
}
