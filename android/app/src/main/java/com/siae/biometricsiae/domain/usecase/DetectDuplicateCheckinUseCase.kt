package com.siae.biometricsiae.domain.usecase

import com.siae.biometricsiae.data.local.dao.AttendanceDao
import com.siae.biometricsiae.data.local.entity.AttendanceEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DetectDuplicateCheckinUseCase @Inject constructor(
    private val attendanceDao: AttendanceDao
) {
    data class DuplicateCheckResult(
        val isDuplicate: Boolean,
        val existingRecord: AttendanceEntity?,
        val message: String
    )

    suspend operator fun invoke(
        employeeId: String,
        type: String,
        windowMinutes: Int = 5
    ): DuplicateCheckResult {
        val now = Instant.now()
        val windowStart = now.minusSeconds(windowMinutes * 60L)

        val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

        val existingRecord = attendanceDao.findDuplicate(
            employeeId = employeeId,
            type = type,
            startTime = formatter.format(windowStart),
            endTime = formatter.format(now)
        )

        return if (existingRecord != null) {
            DuplicateCheckResult(
                isDuplicate = true,
                existingRecord = existingRecord,
                message = "Ya existe un registro de tipo $type en los últimos $windowMinutes minutos"
            )
        } else {
            DuplicateCheckResult(
                isDuplicate = false,
                existingRecord = null,
                message = "No se detectó registro duplicado"
            )
        }
    }
}
