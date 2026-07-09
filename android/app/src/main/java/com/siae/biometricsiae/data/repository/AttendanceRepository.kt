package com.siae.biometricsiae.data.repository

import com.siae.biometricsiae.data.local.dao.AttendanceDao
import com.siae.biometricsiae.data.local.entity.AttendanceEntity
import com.siae.biometricsiae.data.remote.api.AsistenciasApi
import com.siae.biometricsiae.data.remote.dto.CheckinRequest
import com.siae.biometricsiae.data.remote.dto.LocationDto
import com.siae.biometricsiae.data.remote.firebase.FirestoreManager
import com.siae.biometricsiae.domain.model.AttendanceRecord
import com.siae.biometricsiae.domain.model.CheckinResult
import com.siae.biometricsiae.domain.model.SyncStatus
import com.siae.biometricsiae.util.HashUtils
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao,
    private val firestoreManager: FirestoreManager,
    private val api: AsistenciasApi
) {
    fun getRecentRecords(limit: Int = 100): Flow<List<AttendanceEntity>> {
        return attendanceDao.getRecentRecords(limit)
    }

    fun getRecordsByEmployee(employeeId: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getRecordsByEmployee(employeeId)
    }

    fun getRecordsByBranchAndDate(
        branchId: String,
        startDate: String,
        endDate: String
    ): Flow<List<AttendanceEntity>> {
        return attendanceDao.getRecordsByBranchAndDate(branchId, startDate, endDate)
    }

    fun getPendingSyncRecords(): Flow<List<AttendanceEntity>> {
        return attendanceDao.getPendingSyncRecordsFlow()
    }

    suspend fun registerCheckin(
        employeeId: String,
        employeeName: String,
        branchId: String,
        branchName: String,
        deviceId: String,
        type: String,
        method: String,
        latitude: Double?,
        longitude: Double?,
        faceEvidenceUrl: String?,
        biometricAuthEventId: String?,
        tenantId: String,
        appVersion: String,
        isOffline: Boolean
    ): CheckinResult {
        val now = Instant.now()
        val timezone = ZoneId.systemDefault().id
        val timestamp = now.toString()

        // Check for duplicate within 5-minute window
        val fiveMinutesAgo = now.minusSeconds(300).toString()
        val existingRecord = attendanceDao.findDuplicate(
            employeeId = employeeId,
            type = type,
            startTime = fiveMinutesAgo,
            endTime = timestamp
        )

        if (existingRecord != null) {
            return CheckinResult.Duplicate(
                record = mapToDomain(existingRecord)
            )
        }

        val id = UUID.randomUUID().toString()
        val hash = HashUtils.generateHash("$employeeId-$type-$timestamp-$deviceId")

        val entity = AttendanceEntity(
            id = id,
            tenantId = tenantId,
            employeeId = employeeId,
            branchId = branchId,
            deviceId = deviceId,
            type = type,
            timestamp = timestamp,
            timezone = timezone,
            latitude = latitude,
            longitude = longitude,
            method = method,
            faceEvidenceUrl = faceEvidenceUrl,
            biometricAuthEventId = biometricAuthEventId,
            syncStatus = if (isOffline) "PENDING" else "PENDING",
            serverTimestamp = null,
            hash = hash,
            appVersion = appVersion,
            offline = isOffline,
            retryCount = 0,
            createdAt = timestamp,
            updatedAt = timestamp
        )

        attendanceDao.insertRecord(entity)

        // Try to sync immediately if online
        if (!isOffline) {
            try {
                val request = CheckinRequest(
                    employeeId = employeeId,
                    branchId = branchId,
                    type = type,
                    timestamp = timestamp,
                    method = method,
                    deviceId = deviceId,
                    location = if (latitude != null && longitude != null) {
                        LocationDto(latitude, longitude)
                    } else null,
                    faceEvidenceId = null,
                    hash = hash
                )

                val response = api.registerCheckin(request)
                if (response.isSuccessful) {
                    val responseBody = response.body()!!
                    if (responseBody.success) {
                        attendanceDao.updateSyncStatus(id, "SYNCED", responseBody.serverTimestamp)
                        return CheckinResult.Success(
                            record = mapToDomain(entity.copy(
                                syncStatus = "SYNCED",
                                serverTimestamp = responseBody.serverTimestamp
                            ))
                        )
                    } else if (responseBody.duplicate) {
                        return CheckinResult.Duplicate(
                            record = mapToDomain(entity)
                        )
                    }
                }
            } catch (e: Exception) {
                // Will be retried by WorkManager
            }
        }

        return if (isOffline) {
            CheckinResult.Offline(record = mapToDomain(entity))
        } else {
            CheckinResult.Success(record = mapToDomain(entity))
        }
    }

    suspend fun syncPendingRecords(): Int {
        val pendingRecords = attendanceDao.getPendingSyncRecords()
        var syncCount = 0

        for (record in pendingRecords) {
            try {
                val request = CheckinRequest(
                    employeeId = record.employeeId,
                    branchId = record.branchId,
                    type = record.type,
                    timestamp = record.timestamp,
                    method = record.method,
                    deviceId = record.deviceId,
                    location = if (record.latitude != null && record.longitude != null) {
                        LocationDto(record.latitude, record.longitude)
                    } else null,
                    faceEvidenceId = null,
                    hash = record.hash
                )

                val response = api.registerCheckin(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    attendanceDao.updateSyncStatus(
                        record.id,
                        "SYNCED",
                        response.body()?.serverTimestamp
                    )
                    syncCount++
                } else {
                    attendanceDao.incrementRetryCount(record.id)
                }
            } catch (e: Exception) {
                attendanceDao.incrementRetryCount(record.id)
            }
        }

        return syncCount
    }

    suspend fun getPendingCount(): Int {
        return attendanceDao.getPendingCount()
    }

    private fun mapToDomain(entity: AttendanceEntity): AttendanceRecord {
        return AttendanceRecord(
            id = entity.id,
            tenantId = entity.tenantId,
            employeeId = entity.employeeId,
            employeeName = "",
            branchId = entity.branchId,
            branchName = "",
            deviceId = entity.deviceId,
            type = com.siae.biometricsiae.domain.model.AttendanceType.valueOf(entity.type),
            timestamp = entity.timestamp,
            timezone = ZoneId.of(entity.timezone),
            latitude = entity.latitude,
            longitude = entity.longitude,
            method = com.siae.biometricsiae.domain.model.AuthMethod.valueOf(entity.method),
            faceEvidenceUrl = entity.faceEvidenceUrl,
            biometricAuthEventId = entity.biometricAuthEventId,
            syncStatus = SyncStatus.valueOf(entity.syncStatus),
            serverTimestamp = entity.serverTimestamp,
            hash = entity.hash,
            offline = entity.offline
        )
    }
}
