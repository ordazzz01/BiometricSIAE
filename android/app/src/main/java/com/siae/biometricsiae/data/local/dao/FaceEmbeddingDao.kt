package com.siae.biometricsiae.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.siae.biometricsiae.data.local.entity.FaceEmbeddingEntity

@Dao
interface FaceEmbeddingDao {

    @Query("SELECT * FROM face_embeddings")
    suspend fun getAll(): List<FaceEmbeddingEntity>

    @Query("SELECT * FROM face_embeddings WHERE personId = :personId")
    suspend fun getByPersonId(personId: String): FaceEmbeddingEntity?

    @Query("SELECT * FROM face_embeddings WHERE rfc = :rfc")
    suspend fun getByRfc(rfc: String): FaceEmbeddingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(embedding: FaceEmbeddingEntity)

    @Update
    suspend fun update(embedding: FaceEmbeddingEntity)

    @Query("DELETE FROM face_embeddings WHERE personId = :personId")
    suspend fun deleteByPersonId(personId: String)

    @Query("DELETE FROM face_embeddings")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM face_embeddings")
    suspend fun getCount(): Int
}
