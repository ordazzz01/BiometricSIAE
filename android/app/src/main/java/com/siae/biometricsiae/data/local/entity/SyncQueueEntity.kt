package com.siae.biometricsiae.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val entityType: String,
    val entityId: String,
    val action: String,
    val payload: String,
    val attempts: Int = 0,
    val maxAttempts: Int = 10,
    val nextRetryAt: String,
    val status: String,
    val createdAt: String
)
