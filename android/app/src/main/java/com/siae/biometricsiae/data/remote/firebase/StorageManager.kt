package com.siae.biometricsiae.data.remote.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageManager @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadEvidence(
        orgId: String,
        employeeId: String,
        imageFile: File
    ): Result<String> {
        return try {
            val timestamp = System.currentTimeMillis()
            val path = "organizations/$orgId/evidence/$employeeId/$timestamp.jpg"
            val reference = storage.reference.child(path)

            val uri = Uri.fromFile(imageFile)
            reference.putFile(uri).await()

            val downloadUrl = reference.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadEvidenceFromBytes(
        orgId: String,
        employeeId: String,
        imageBytes: ByteArray
    ): Result<String> {
        return try {
            val timestamp = System.currentTimeMillis()
            val path = "organizations/$orgId/evidence/$employeeId/$timestamp.jpg"
            val reference = storage.reference.child(path)

            reference.putBytes(imageBytes).await()

            val downloadUrl = reference.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvidence(url: String): Result<Unit> {
        return try {
            storage.getReferenceFromUrl(url).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
