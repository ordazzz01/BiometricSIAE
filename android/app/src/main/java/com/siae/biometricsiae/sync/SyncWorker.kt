package com.siae.biometricsiae.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.siae.biometricsiae.domain.usecase.SyncPendingRecordsUseCase
import com.siae.biometricsiae.util.Constants
import com.siae.biometricsiae.util.SecureLogger
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncPendingRecordsUseCase: SyncPendingRecordsUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            SecureLogger.d("SyncWorker", "Starting sync")

            val result = syncPendingRecordsUseCase()

            SecureLogger.d(
                "SyncWorker",
                "Sync completed: ${result.attendanceSynced} attendance, ${result.queueSynced} queue items, ${result.totalPending} total pending"
            )

            if (result.totalPending > 0) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (e: Exception) {
            SecureLogger.e("SyncWorker", "Sync failed", e)
            if (runAttemptCount < Constants.MAX_SYNC_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        fun schedulePeriodicSync(context: Context) {
            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
                Constants.SYNC_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            )
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    Constants.SYNC_BACKOFF_DELAY_SECONDS,
                    TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                Constants.SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(Constants.SYNC_WORK_NAME)
        }
    }
}
