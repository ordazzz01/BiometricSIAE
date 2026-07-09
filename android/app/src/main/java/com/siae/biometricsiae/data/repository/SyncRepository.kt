package com.siae.biometricsiae.data.repository

import com.siae.biometricsiae.data.local.dao.SyncQueueDao
import com.siae.biometricsiae.data.local.entity.SyncQueueEntity
import com.siae.biometricsiae.data.remote.api.AsistenciasApi
import com.siae.biometricsiae.data.remote.dto.AttendanceSyncRequest
import com.siae.biometricsiae.data.remote.dto.CheckinRequest
import com.siae.biometricsiae.data.remote.firebase.FirestoreManager
import com.siae.biometricsiae.data.remote.firebase.StorageManager
import com.siae.biometricsiae.util.SecureLogger
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val syncQueueDao: SyncQueueDao,
    private val firestoreManager: FirestoreManager,
    private val storageManager: StorageManager,
    private val api: AsistenciasApi
) {
    fun getPendingSyncItems(): Flow<List<SyncQueueEntity>> {
        return syncQueueDao.getPendingItemsFlow()
    }

    suspend fun addToSyncQueue(
        entityType: String,
        entityId: String,
        action: String,
        payload: String
    ) {
        val item = SyncQueueEntity(
            id = UUID.randomUUID().toString(),
            entityType = entityType,
            entityId = entityId,
            action = action,
            payload = payload,
            attempts = 0,
            maxAttempts = 10,
            nextRetryAt = Instant.now().toString(),
            status = "PENDING",
            createdAt = Instant.now().toString()
        )

        syncQueueDao.insertItem(item)
    }

    suspend fun syncPendingItems(): SyncResult {
        val pendingItems = syncQueueDao.getPendingItems()
        var successCount = 0
        var failCount = 0

        for (item in pendingItems) {
            try {
                when (item.entityType) {
                    "ATTENDANCE" -> {
                        syncAttendanceItem(item)
                    }
                    "EVIDENCE" -> {
                        syncEvidenceItem(item)
                    }
                    "INCIDENT" -> {
                        syncIncidentItem(item)
                    }
                }
                syncQueueDao.updateStatus(item.id, "COMPLETED")
                successCount++
            } catch (e: Exception) {
                SecureLogger.e("Sync", "Failed to sync item ${item.id}", e)
                
                if (item.attempts >= item.maxAttempts - 1) {
                    syncQueueDao.updateStatus(item.id, "FAILED")
                } else {
                    val nextRetry = Instant.now().plusSeconds(
                        calculateBackoff(item.attempts)
                    )
                    syncQueueDao.scheduleRetry(item.id, nextRetry.toString())
                }
                failCount++
            }
        }

        return SyncResult(successCount, failCount)
    }

    private suspend fun syncAttendanceItem(item: SyncQueueEntity) {
        // Parse payload and sync to server
        // This is a simplified version - in production, parse JSON properly
        SecureLogger.d("Sync", "Syncing attendance item: ${item.entityId}")
    }

    private suspend fun syncEvidenceItem(item: SyncQueueEntity) {
        SecureLogger.d("Sync", "Syncing evidence item: ${item.entityId}")
    }

    private suspend fun syncIncidentItem(item: SyncQueueEntity) {
        SecureLogger.d("Sync", "Syncing incident item: ${item.entityId}")
    }

    private fun calculateBackoff(attempt: Int): Long {
        // Exponential backoff: 1s, 2s, 4s, 8s, 16s, 30s (max)
        return minOf(1000L * (1L shl attempt), 30000L)
    }

    suspend fun getPendingCount(): Int {
        return syncQueueDao.getPendingCount()
    }

    suspend fun cleanupOldItems() {
        val thirtyDaysAgo = Instant.now().minusSeconds(30 * 24 * 60 * 60).toString()
        syncQueueDao.cleanupCompleted(thirtyDaysAgo)
    }
}

data class SyncResult(
    val successCount: Int,
    val failCount: Int
)
