package com.siae.biometricsiae.data.remote.api.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null

        repeat(MAX_RETRIES) { attempt ->
            try {
                response?.close()
                response = chain.proceed(request)

                // Retry on server errors (5xx)
                if (response!!.code in 500..599 && attempt < MAX_RETRIES - 1) {
                    Thread.sleep(RETRY_DELAY_MS * (attempt + 1))
                    return@repeat
                }

                return response!!
            } catch (e: IOException) {
                exception = e
                if (attempt < MAX_RETRIES - 1) {
                    Thread.sleep(RETRY_DELAY_MS * (attempt + 1))
                }
            }
        }

        response?.close()
        throw exception ?: IOException("Max retries exceeded")
    }
}
