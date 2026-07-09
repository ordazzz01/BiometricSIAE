package com.siae.biometricsiae.data.remote.api.interceptors

import com.siae.biometricsiae.security.KeystoreManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val keyStoreManager: KeystoreManager
) : Interceptor {

    companion object {
        private const val AUTH_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val DEVICE_ID_HEADER = "X-Device-Id"
        private const val TENANT_ID_HEADER = "X-Tenant-Id"
        private const val APP_VERSION_HEADER = "X-App-Version"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth for login endpoint
        if (originalRequest.url.encodedPath.contains("auth/device-login")) {
            return chain.proceed(originalRequest)
        }

        val accessToken = keyStoreManager.getAccessToken()
        val deviceId = keyStoreManager.getDeviceId()
        val tenantId = keyStoreManager.getTenantId()

        val authenticatedRequest = originalRequest.newBuilder()
            .apply {
                if (accessToken != null) {
                    addHeader(AUTH_HEADER, "$BEARER_PREFIX$accessToken")
                }
                if (deviceId != null) {
                    addHeader(DEVICE_ID_HEADER, deviceId)
                }
                if (tenantId != null) {
                    addHeader(TENANT_ID_HEADER, tenantId)
                }
                addHeader(APP_VERSION_HEADER, "1.0.0")
            }
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
