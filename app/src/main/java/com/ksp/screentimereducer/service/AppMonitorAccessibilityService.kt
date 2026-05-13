package com.ksp.screentimereducer.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.ksp.screentimereducer.domain.repository.AppRuleRepository
import com.ksp.screentimereducer.domain.repository.FocusRepository
import com.ksp.screentimereducer.domain.repository.UsageRepository
import com.ksp.screentimereducer.presentation.delay.DelayOverlayActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Watches foreground app changes and triggers the App Delay overlay when
 * the user opens a watched app — the heart of the "gentle intervention"
 * UX.
 *
 * Battery considerations:
 * - We subscribe only to TYPE_WINDOW_STATE_CHANGED, which is far cheaper
 *   than content events.
 * - Per-app rule lookups are done off the main thread.
 * - We suppress re-triggers while the user stays inside the same app —
 *   once they navigate away to a different package, the suppression for
 *   the previous app clears, so re-opening it shows the overlay again.
 */
@AndroidEntryPoint
class AppMonitorAccessibilityService : AccessibilityService() {

    @Inject lateinit var appRuleRepo: AppRuleRepository
    @Inject lateinit var focusRepo: FocusRepository
    @Inject lateinit var usageRepo: UsageRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Packages we've already shown the overlay for during their current
    // foreground session. Entries are cleared when the user navigates
    // away to a different package, so re-opening always re-triggers.
    private val handledWhileForeground = HashSet<String>()

    @Volatile private var lastForeground: String? = null

    // Captures every device unlock for the dashboard's unlock counter. We register
    // it on the always-running accessibility service rather than the focus
    // service so unlocks are recorded even when no focus session is active.
    private val unlockReceiver = UnlockReceiver()
    private var unlockReceiverRegistered = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Don't replace serviceInfo with a fresh AccessibilityServiceInfo()
        // here — doing so drops the isAccessibilityTool=true flag that the
        // XML config sets, and on Android 12+ that flag is what authorizes
        // this service to launch the DelayOverlayActivity from background.
        // The XML already configures eventTypes/feedbackType/notificationTimeout.
        if (!unlockReceiverRegistered) {
            ContextCompat.registerReceiver(
                this,
                unlockReceiver,
                IntentFilter(Intent.ACTION_USER_PRESENT),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            unlockReceiverRegistered = true
        }
        instance.value = this
        Log.i(TAG, "Accessibility service connected.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg == packageName) return
        if (pkg == lastForeground) return

        // Any other package taking foreground — including systemui (recents,
        // notification shade) — counts as the user leaving the previous
        // foreground app. We clear the suppression for the previous app so
        // that returning to it will re-trigger the overlay every time.
        lastForeground?.let { handledWhileForeground.remove(it) }
        lastForeground = pkg

        // Don't show the overlay for the system shell itself.
        if (pkg.startsWith("com.android.systemui")) return

        if (pkg in handledWhileForeground) return

        scope.launch { handlePackage(pkg) }
    }

    private suspend fun handlePackage(pkg: String) {
        val active = focusRepo.activeOnce()
        val blockedByFocus = active?.isActive == true && pkg in active.blockedPackages

        val rule = appRuleRepo.ruleFor(pkg)
        val needsDelay = rule?.delayEnabled == true
        val limitMs = rule?.dailyLimitMs ?: 0L
        // Pull fresh usage from UsageStatsManager before checking the limit —
        // the Room cache is only refreshed by the dashboard / 15-min worker,
        // so without this the limit check reads stale (often zero) usage.
        val overLimit = if (limitMs > 0) {
            runCatching { usageRepo.syncToday() }
            usageRepo.usageForApp(pkg) >= limitMs
        } else false

        if (!blockedByFocus && !needsDelay && !overLimit) return

        handledWhileForeground.add(pkg)
        val intent = Intent(applicationContext, DelayOverlayActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            )
            putExtra(DelayOverlayActivity.EXTRA_PACKAGE, pkg)
            putExtra(DelayOverlayActivity.EXTRA_SECONDS, rule?.delaySeconds ?: 5)
            putExtra(DelayOverlayActivity.EXTRA_REASON, when {
                blockedByFocus -> DelayOverlayActivity.REASON_FOCUS
                overLimit -> DelayOverlayActivity.REASON_LIMIT
                else -> DelayOverlayActivity.REASON_DELAY
            })
        }
        runCatching { startActivity(intent) }
            .onFailure { Log.w(TAG, "Failed to launch delay overlay", it) }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (unlockReceiverRegistered) {
            runCatching { unregisterReceiver(unlockReceiver) }
            unlockReceiverRegistered = false
        }
        instance.value = null
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AppMonitorA11y"

        /** Lets the rest of the app observe whether the service is alive. */
        val instance = MutableStateFlow<AppMonitorAccessibilityService?>(null)
    }
}

