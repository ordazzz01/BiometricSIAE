package com.siae.biometricsiae.security

import android.content.Context
import android.os.Build
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootDetector @Inject constructor(
    private val context: Context
) {
    fun isDeviceRooted(): Boolean {
        return checkSuBinary() ||
            checkBusyBoxBinary() ||
            checkRootApps() ||
            checkRootProperties() ||
            checkRootBins() ||
            checkTestKeys()
    }

    private fun checkSuBinary(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val reader = process.inputStream.bufferedReader()
            val result = reader.readLine()
            reader.close()
            result != null
        } catch (e: Exception) {
            false
        }
    }

    private fun checkBusyBoxBinary(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "busybox"))
            val reader = process.inputStream.bufferedReader()
            val result = reader.readLine()
            reader.close()
            result != null
        } catch (e: Exception) {
            false
        }
    }

    private fun checkRootApps(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkRootProperties(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("getprop", "ro.build.tags"))
            val reader = process.inputStream.bufferedReader()
            val result = reader.readLine()
            reader.close()
            result?.contains("test-keys") == true
        } catch (e: Exception) {
            false
        }
    }

    private fun checkRootBins(): Boolean {
        val paths = arrayOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/su",
            "/su/bin/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkTestKeys(): Boolean {
        return Build.TAGS?.contains("test-keys") == true
    }

    fun getSecurityStatus(): SecurityStatus {
        val isRooted = isDeviceRooted()
        val isEmulator = checkIsEmulator()
        val isDebuggable = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0

        return SecurityStatus(
            isRooted = isRooted,
            isEmulator = isEmulator,
            isDebuggable = isDebuggable,
            isTampered = isRooted || isDebuggable
        )
    }

    private fun checkIsEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
            || "google_sdk" == Build.PRODUCT)
    }
}

data class SecurityStatus(
    val isRooted: Boolean,
    val isEmulator: Boolean,
    val isDebuggable: Boolean,
    val isTampered: Boolean
)
