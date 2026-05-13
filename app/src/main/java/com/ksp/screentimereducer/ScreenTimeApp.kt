package com.ksp.screentimereducer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ksp.screentimereducer.core.notification.NotificationChannels
import com.ksp.screentimereducer.core.work.WorkScheduler
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point.
 *
 * - Bootstraps Hilt.
 * - Provides the WorkManager configuration so workers can use injected dependencies.
 * - Configures RevenueCat for subscriptions (key is read from BuildConfig at runtime — see README).
 * - Creates persistent notification channels.
 * - Schedules recurring background work (daily rollover, usage aggregation, etc.).
 */
@HiltAndroidApp
class ScreenTimeApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var workScheduler: WorkScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        setupNotificationChannels()
        setupRevenueCat()
        workScheduler.scheduleRecurringJobs()
    }

    private fun setupNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.FOCUS,
                getString(R.string.notif_channel_focus),
                NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                NotificationChannels.REMINDERS,
                getString(R.string.notif_channel_reminders),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }

    private fun setupRevenueCat() {
        // BuildConfig.REVENUECAT_API_KEY should be set via local.properties or environment variable.
        // For local dev, an empty key disables purchases gracefully.
        val key = BuildConfig.REVENUECAT_API_KEY.takeIf { it.isNotBlank() } ?: return
        Purchases.logLevel = LogLevel.WARN
        Purchases.configure(
            PurchasesConfiguration.Builder(this, key).build()
        )
    }
}
