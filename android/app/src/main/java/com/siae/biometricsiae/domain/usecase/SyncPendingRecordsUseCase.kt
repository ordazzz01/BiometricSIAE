package com.siae.biometricsiae.domain.usecase

import com.siae.biometricsiae.data.repository.AttendanceRepository
import com.siae.biometricsiae.data.repository.SyncRepository
import com.siae.biometricsiae.util.SecureLogger
import javax.inject.Inject

class SyncPendingRecordsUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val syncRepository: SyncRepository
) {
    data class SyncResult(
        val attendanceSynced: Int,
        val queueSynced: Int,
        val totalPending: Int
    )

    suspend operator fun invoke(): SyncResult {
        SecureLogger.d("Sync", "Starting sync of pending records")

        val attendanceSynced = attendanceRepository.syncPendingRecords()
        val queueResult = syncRepository.syncPendingItems()
        
        val totalPending = attendanceRepository.getPendingCount() + syncRepository.getPendingCount()

        SecureLogger.d("Sync", "Sync completed: $attendanceSynced attendance, ${queueResult.successCount} queue items")

        return SyncResult(
            attendanceSynced = attendanceSynced,
            queueSynced = queueResult.successCount,
            totalPending = totalPending
        )
    }
}
