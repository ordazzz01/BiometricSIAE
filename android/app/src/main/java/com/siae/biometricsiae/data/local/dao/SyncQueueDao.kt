package com.siae.biometricsiae.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.siae.biometricsiae.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingItems(): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingItemsFlow(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue WHERE id = :id")
    suspend fun getItemById(id: String): SyncQueueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: SyncQueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<SyncQueueEntity>)

    @Update
    suspend fun updateItem(item: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = :status, attempts = attempts + 1 WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE sync_queue SET status = 'PENDING', nextRetryAt = :nextRetryAt WHERE id = :id")
    suspend fun scheduleRetry(id: String, nextRetryAt: String)

    @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED' AND createdAt < :olderThan")
    suspend fun cleanupCompleted(olderThan: String)

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING'")
    suspend fun getPendingCount(): Int
}
