package com.siae.biometricsiae.data.remote.api.interceptors

import com.siae.biometricsiae.util.SecureLogger
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoggingInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()

        SecureLogger.d("API", "Request: ${request.method} ${request.url}")

        val response = chain.proceed(request)
        val duration = System.currentTimeMillis() - startTime

        SecureLogger.d(
            "API",
            "Response: ${response.code} ${request.url} (${duration}ms)"
        )

        // Log headers safely (redact sensitive data)
        response.headers.forEach { (name, value) ->
            if (name.lowercase() != "authorization") {
                SecureLogger.d("API", "Header: $name: $value")
            }
        }

        return response
    }
}
