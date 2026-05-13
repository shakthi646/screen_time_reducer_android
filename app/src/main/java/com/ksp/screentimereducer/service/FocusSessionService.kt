package com.ksp.screentimereducer.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.ksp.screentimereducer.MainActivity
import com.ksp.screentimereducer.R
import com.ksp.screentimereducer.core.notification.NotificationChannels
import com.ksp.screentimereducer.domain.model.FocusMode
import com.ksp.screentimereducer.domain.repository.FocusRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Long-running foreground service that keeps the active focus session
 * visible in the system tray while it's running. Stays alive when the
 * user leaves the app so the accessibility-service-based blocking still
 * works.
 *
 * Also subscribes to USER_PRESENT broadcasts here (rather than via a
 * manifest receiver) so we sidestep Android 8+ background broadcast
 * restrictions and only listen while focus is active.
 */
@AndroidEntryPoint
class FocusSessionService : android.app.Service() {

    @Inject lateinit var focusRepo: FocusRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var observeJob: Job? = null
    private val unlockReceiver = UnlockReceiver()

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        ContextCompat.registerReceiver(this, unlockReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NotificationChannels.FOCUS_SERVICE_ID, buildNotification(FocusMode.WORK))

        observeJob?.cancel()
        observeJob = scope.launch {
            focusRepo.observeActive().collectLatest { session ->
                if (session == null || !session.isActive) {
                    stopSelf()
                } else {
                    val nm = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                    nm.notify(NotificationChannels.FOCUS_SERVICE_ID, buildNotification(session.mode))
                }
            }
        }
        return START_STICKY
    }

    private fun buildNotification(mode: FocusMode): Notification {
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else PendingIntent.FLAG_UPDATE_CURRENT
        val pi = PendingIntent.getActivity(this, 0, intent, flags)
        return NotificationCompat.Builder(this, NotificationChannels.FOCUS)
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(getString(R.string.notif_focus_title))
            .setContentText(getString(R.string.notif_focus_body, mode.name.lowercase().replaceFirstChar { it.uppercase() }))
            .setContentIntent(pi)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(unlockReceiver) }
        observeJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FocusSessionService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, FocusSessionService::class.java))
        }
    }
}
