package com.siae.biometricsiae.data.repository

import com.siae.biometricsiae.data.local.dao.IncidentDao
import com.siae.biometricsiae.data.local.entity.IncidentEntity
import com.siae.biometricsiae.data.remote.api.AsistenciasApi
import com.siae.biometricsiae.data.remote.dto.IncidentRequest
import com.siae.biometricsiae.data.remote.firebase.FirestoreManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentRepository @Inject constructor(
    private val incidentDao: IncidentDao,
    private val firestoreManager: FirestoreManager,
    private val api: AsistenciasApi
) {
    fun getAllIncidents(): Flow<List<IncidentEntity>> {
        return incidentDao.getAllIncidents()
    }

    fun getIncidentsByEmployee(employeeId: String): Flow<List<IncidentEntity>> {
        return incidentDao.getIncidentsByEmployee(employeeId)
    }

    suspend fun createIncident(
        employeeId: String,
        branchId: String,
        deviceId: String,
        type: String,
        description: String,
        authorizedBy: String?,
        tenantId: String
    ): Result<IncidentEntity> {
        return try {
            val id = java.util.UUID.randomUUID().toString()
            val timestamp = java.time.Instant.now().toString()

            val entity = IncidentEntity(
                id = id,
                tenantId = tenantId,
                employeeId = employeeId,
                branchId = branchId,
                deviceId = deviceId,
                type = type,
                description = description,
                authorizedBy = authorizedBy,
                status = "PENDING",
                syncStatus = "PENDING",
                createdAt = timestamp
            )

            incidentDao.insertIncident(entity)

            // Try to sync with server
            try {
                val request = IncidentRequest(
                    employeeId = employeeId,
                    branchId = branchId,
                    deviceId = deviceId,
                    type = type,
                    description = description,
                    authorizedBy = authorizedBy,
                    timestamp = timestamp
                )

                val response = api.createIncident(request)
                if (response.isSuccessful) {
                    incidentDao.updateSyncStatus(id, "SYNCED")
                }
            } catch (e: Exception) {
                // Will be retried
            }

            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingIncidents(): List<IncidentEntity> {
        return incidentDao.getPendingIncidents()
    }

    suspend fun deleteAllIncidents() {
        incidentDao.deleteAllIncidents()
    }
}
