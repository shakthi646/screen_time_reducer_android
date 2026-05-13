package com.ksp.screentimereducer.core.permissions

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.ksp.screentimereducer.service.AppMonitorAccessibilityService

/**
 * Lightweight, side-effect-free checks for the four critical permissions
 * (usage access, accessibility, overlay, notifications). These are polled
 * by the onboarding flow and settings screen.
 */
object PermissionChecker {

    fun hasUsageAccess(context: Context): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ops.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun hasAccessibility(context: Context): Boolean {
        // Primary: the service publishes its own MutableStateFlow when
        // onServiceConnected fires. That's the authoritative live signal.
        if (AppMonitorAccessibilityService.instance.value != null) return true

        // Fallback for the case where the activity is asking before the
        // service has had a chance to connect (e.g. immediately on resume
        // after the user enabled the toggle): ask AccessibilityManager
        // directly. Avoid parsing the raw Settings.Secure string — its
        // format varies across OEMs (some store short form, some leave
        // ACCESSIBILITY_ENABLED at 0 while individual services run fine).
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            ?: return false
        val ourPkg = context.packageName
        val ourClass = AppMonitorAccessibilityService::class.java.name
        val enabled = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        return enabled.any { info ->
            val svc = info.resolveInfo?.serviceInfo ?: return@any false
            svc.packageName == ourPkg && svc.name == ourClass
        }
    }

    fun hasOverlay(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun hasNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    }
}
