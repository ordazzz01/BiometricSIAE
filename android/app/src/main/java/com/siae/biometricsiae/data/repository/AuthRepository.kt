package com.siae.biometricsiae.data.repository

import com.siae.biometricsiae.data.remote.api.AsistenciasApi
import com.siae.biometricsiae.data.remote.dto.AuthTokens
import com.siae.biometricsiae.data.remote.dto.DeviceLoginRequest
import com.siae.biometricsiae.data.remote.firebase.FirebaseAuthManager
import com.siae.biometricsiae.security.KeystoreManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuthManager: FirebaseAuthManager,
    private val api: AsistenciasApi,
    private val keyStoreManager: KeystoreManager
) {
    suspend fun deviceLogin(
        deviceId: String,
        deviceName: String,
        branchId: String,
        email: String,
        password: String,
        fcmToken: String?
    ): Result<AuthTokens> {
        return try {
            val request = DeviceLoginRequest(
                deviceId = deviceId,
                deviceName = deviceName,
                branchId = branchId,
                email = email,
                password = password,
                fcmToken = fcmToken,
                capabilities = emptyList()
            )

            val response = api.deviceLogin(request)
            if (response.isSuccessful) {
                val tokens = response.body()!!
                keyStoreManager.saveTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    tenantId = tokens.tenantId,
                    deviceId = tokens.deviceId
                )
                Result.success(tokens)
            } else {
                Result.failure(Exception("Error de autenticación: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshToken(): Result<AuthTokens> {
        return try {
            val refreshToken = keyStoreManager.getRefreshToken()
                ?: return Result.failure(Exception("No hay refresh token"))

            val response = api.refreshToken(refreshToken)
            if (response.isSuccessful) {
                val tokens = response.body()!!
                keyStoreManager.saveTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken,
                    tenantId = tokens.tenantId,
                    deviceId = tokens.deviceId
                )
                Result.success(tokens)
            } else {
                Result.failure(Exception("Error al refrescar token: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        keyStoreManager.clearTokens()
        firebaseAuthManager.signOut()
    }

    fun isAuthenticated(): Boolean {
        return keyStoreManager.getAccessToken() != null
    }
}
