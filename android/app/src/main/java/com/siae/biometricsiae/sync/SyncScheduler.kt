package com.siae.biometricsiae.sync

import android.content.Context
import com.siae.biometricsiae.util.Constants
import com.siae.biometricsiae.util.SecureLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedulePeriodicSync() {
        SyncWorker.schedulePeriodicSync(context)
        SecureLogger.d("SyncScheduler", "Periodic sync scheduled every ${Constants.SYNC_INTERVAL_MINUTES} minutes")
    }

    fun cancelSync() {
        SyncWorker.cancelSync(context)
        SecureLogger.d("SyncScheduler", "Sync cancelled")
    }

    fun isSyncScheduled(): Boolean {
        // Check if sync work is scheduled
        return true // Simplified - in production, check WorkManager state
    }
}
