package com.siae.biometricsiae.data.repository

import com.siae.biometricsiae.data.local.dao.DeviceDao
import com.siae.biometricsiae.data.local.entity.DeviceEntity
import com.siae.biometricsiae.data.remote.api.AsistenciasApi
import com.siae.biometricsiae.data.remote.firebase.FirestoreManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepository @Inject constructor(
    private val deviceDao: DeviceDao,
    private val firestoreManager: FirestoreManager,
    private val api: AsistenciasApi
) {
    fun getActiveDevices(): Flow<List<DeviceEntity>> {
        return deviceDao.getActiveDevices()
    }

    suspend fun getDeviceById(id: String): DeviceEntity? {
        return deviceDao.getDeviceById(id)
    }

    fun getDevicesByBranch(branchId: String): Flow<List<DeviceEntity>> {
        return deviceDao.getDevicesByBranch(branchId)
    }

    suspend fun syncDeviceConfig(deviceId: String) {
        try {
            val response = api.getDeviceConfig(deviceId)
            if (response.isSuccessful) {
                val config = response.body() ?: return
                
                val entity = DeviceEntity(
                    id = config.deviceId,
                    tenantId = "",
                    name = config.name,
                    branchId = config.branchId,
                    type = config.type,
                    biometricCapabilities = emptyList(),
                    cameraAvailable = false,
                    gpsAvailable = false,
                    lastSyncAt = null,
                    active = true,
                    configJson = response.body().toString(),
                    createdAt = ""
                )

                deviceDao.insertDevice(entity)
            }
        } catch (e: Exception) {
            // Log error
        }
    }

    suspend fun updateLastSync(deviceId: String) {
        deviceDao.updateLastSync(deviceId, Instant.now().toString())
    }

    suspend fun insertDevice(device: DeviceEntity) {
        deviceDao.insertDevice(device)
    }

    suspend fun deleteAllDevices() {
        deviceDao.deleteAllDevices()
    }
}

private fun java.time.Instant.toString(): String {
    return this.toString()
}
