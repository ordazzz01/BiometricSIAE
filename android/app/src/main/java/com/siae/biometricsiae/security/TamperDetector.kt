package com.siae.biometricsiae.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Debug
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TamperDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isTampered(): Boolean {
        return isDebuggable() ||
            isRunningOnEmulator() ||
            isDebuggerAttached() ||
            isRunningInBadTime() ||
            isInstalledFromUnknownSource()
    }

    private fun isDebuggable(): Boolean {
        return (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun isRunningOnEmulator(): Boolean {
        return android.os.Build.FINGERPRINT.startsWith("generic")
            || android.os.Build.FINGERPRINT.startsWith("unknown")
            || android.os.Build.MODEL.contains("google_sdk")
            || android.os.Build.MODEL.contains("Emulator")
            || android.os.Build.MODEL.contains("Android SDK built for x86")
            || android.os.Build.MANUFACTURER.contains("Genymotion")
            || android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic")
            || "google_sdk" == android.os.Build.PRODUCT
    }

    private fun isDebuggerAttached(): Boolean {
        return Debug.isDebuggerConnected()
    }

    private fun isRunningInBadTime(): Boolean {
        // Check if device time is significantly different from server time
        // This is a basic check - in production, compare with NTP server
        val currentTime = System.currentTimeMillis()
        val installedTime = getInstallTime()
        
        // If app was installed in the future, something is wrong
        if (installedTime > currentTime + 24 * 60 * 60 * 1000) {
            return true
        }
        
        return false
    }

    private fun getInstallTime(): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.firstInstallTime
        } catch (e: PackageManager.NameNotFoundException) {
            System.currentTimeMillis()
        }
    }

    private fun isInstalledFromUnknownSource(): Boolean {
        // In production, check installer package name
        // For now, return false
        return false
    }

    fun getTamperStatus(): TamperStatus {
        return TamperStatus(
            isDebuggable = isDebuggable(),
            isEmulator = isRunningOnEmulator(),
            isDebuggerAttached = isDebuggerAttached(),
            isBadTime = isRunningInBadTime(),
            isUnknownSource = isInstalledFromUnknownSource()
        )
    }
}

data class TamperStatus(
    val isDebuggable: Boolean,
    val isEmulator: Boolean,
    val isDebuggerAttached: Boolean,
    val isBadTime: Boolean,
    val isUnknownSource: Boolean
) {
    val isCompromised: Boolean
        get() = isDebuggable || isEmulator || isDebuggerAttached || isBadTime || isUnknownSource
}
