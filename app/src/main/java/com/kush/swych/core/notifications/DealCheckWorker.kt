package com.kush.swych.core.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kush.swych.core.data.DealRepository
import com.kush.swych.core.network.SupabaseManager
import com.kush.swych.core.util.SettingsManager
import io.github.jan.supabase.auth.auth
import java.util.concurrent.TimeUnit

/** Background check for deal changes; Android runs it about every 15 minutes. */
class DealCheckWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        SettingsManager.init(applicationContext)
        if (!SettingsManager.notificationsEnabled.value) return Result.success()

        SupabaseManager.client.auth.awaitInitialization()
        val uid = SupabaseManager.currentUid ?: return Result.success()

        val deals = DealRepository(applicationContext).getMyDeals().getOrElse { return Result.retry() }
        DealNotifier.processChanges(applicationContext, deals, uid)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "deal-check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<DealCheckWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
