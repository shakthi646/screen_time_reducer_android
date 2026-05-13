package com.ksp.screentimereducer.presentation.delay

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ksp.screentimereducer.ui.theme.ScreenTimeReducerTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Translucent activity hosted on top of the launched app. Renders the
 * "Take a breath" countdown in Compose. Backed by an Activity (rather
 * than a TYPE_APPLICATION_OVERLAY window) because:
 *
 * 1. Activities reliably draw above other apps on modern Android without
 *    SAW heuristics.
 * 2. We get input handling, system bars, and Compose for free.
 * 3. We avoid the increasingly restrictive overlay permission UX on
 *    Android 14+ (where SYSTEM_ALERT_WINDOW is reserved for accessibility
 *    apps anyway — which we are).
 */
@AndroidEntryPoint
class DelayOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(false)
        }

        val packageName = intent.getStringExtra(EXTRA_PACKAGE) ?: run { finish(); return }
        val seconds = intent.getIntExtra(EXTRA_SECONDS, 5)
        val reason = intent.getStringExtra(EXTRA_REASON) ?: REASON_DELAY

        setContent {
            ScreenTimeReducerTheme(darkTheme = true) {
                DelayOverlayScreen(
                    packageName = packageName,
                    seconds = seconds,
                    reason = reason,
                    onContinue = {
                        // The user explicitly chose to open the app — close
                        // the overlay; the launcher intent is already in
                        // the back stack.
                        finishAndRemoveTask()
                    },
                    onCancel = {
                        // Take them back home instead of into the app.
                        val home = Intent(Intent.ACTION_MAIN)
                            .addCategory(Intent.CATEGORY_HOME)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(home)
                        finishAndRemoveTask()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE = "pkg"
        const val EXTRA_SECONDS = "sec"
        const val EXTRA_REASON = "reason"

        const val REASON_DELAY = "delay"
        const val REASON_FOCUS = "focus"
        const val REASON_LIMIT = "limit"
    }
}
