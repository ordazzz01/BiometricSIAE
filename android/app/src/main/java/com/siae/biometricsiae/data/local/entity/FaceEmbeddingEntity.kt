package com.siae.biometricsiae.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "face_embeddings")
data class FaceEmbeddingEntity(
    @PrimaryKey val personId: String,
    val rfc: String,
    val fullName: String,
    val vector: String, // JSON array de floats
    val quality: Float,
    val deviceId: String,
    val enrolledAt: String
)
