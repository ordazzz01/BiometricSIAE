package com.siae.biometricsiae.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeystoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "biometricsiae_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TENANT_ID = "tenant_id"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_BRANCH_ID = "branch_id"
        private const val KEY_USER_EMAIL = "user_email"
    }

    fun saveTokens(
        accessToken: String,
        refreshToken: String,
        tenantId: String,
        deviceId: String
    ) {
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_TENANT_ID, tenantId)
            .putString(KEY_DEVICE_ID, deviceId)
            .apply()
    }

    fun getAccessToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getTenantId(): String? {
        return encryptedPrefs.getString(KEY_TENANT_ID, null)
    }

    fun getDeviceId(): String? {
        return encryptedPrefs.getString(KEY_DEVICE_ID, null)
    }

    fun getBranchId(): String? {
        return encryptedPrefs.getString(KEY_BRANCH_ID, null)
    }

    fun saveBranchId(branchId: String) {
        encryptedPrefs.edit()
            .putString(KEY_BRANCH_ID, branchId)
            .apply()
    }

    fun saveUserEmail(email: String) {
        encryptedPrefs.edit()
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUserEmail(): String? {
        return encryptedPrefs.getString(KEY_USER_EMAIL, null)
    }

    fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TENANT_ID)
            .remove(KEY_DEVICE_ID)
            .remove(KEY_BRANCH_ID)
            .apply()
    }

    fun hasValidSession(): Boolean {
        return getAccessToken() != null && getTenantId() != null
    }
}
