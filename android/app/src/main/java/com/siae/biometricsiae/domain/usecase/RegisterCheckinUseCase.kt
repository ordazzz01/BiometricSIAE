package com.siae.biometricsiae.domain.usecase

import com.siae.biometricsiae.data.local.entity.AttendanceEntity
import com.siae.biometricsiae.data.repository.AttendanceRepository
import com.siae.biometricsiae.domain.model.CheckinResult
import com.siae.biometricsiae.security.KeystoreManager
import javax.inject.Inject

class RegisterCheckinUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val keyStoreManager: KeystoreManager
) {
    suspend operator fun invoke(
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
        isOffline: Boolean
    ): CheckinResult {
        val tenantId = keyStoreManager.getTenantId() ?: return CheckinResult.Error(
            message = "No hay sesión activa",
            code = "NO_SESSION"
        )

        val appVersion = "1.0.0"

        return attendanceRepository.registerCheckin(
            employeeId = employeeId,
            employeeName = employeeName,
            branchId = branchId,
            branchName = branchName,
            deviceId = deviceId,
            type = type,
            method = method,
            latitude = latitude,
            longitude = longitude,
            faceEvidenceUrl = faceEvidenceUrl,
            biometricAuthEventId = biometricAuthEventId,
            tenantId = tenantId,
            appVersion = appVersion,
            isOffline = isOffline
        )
    }
}
