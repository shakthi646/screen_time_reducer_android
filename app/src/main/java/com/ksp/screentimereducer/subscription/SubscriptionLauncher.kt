package com.ksp.screentimereducer.subscription

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.ksp.screentimereducer.BuildConfig
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith

object SubscriptionLauncher {

    fun open(context: Context) {
        if (BuildConfig.REVENUECAT_API_KEY.isBlank()) {
            openPlayStoreSubscriptions(context)
            return
        }
        Purchases.sharedInstance.getOfferingsWith(
            onError = { _: PurchasesError -> openPlayStoreSubscriptions(context) },
            onSuccess = { offerings ->
                val current = offerings.current
                val pkg = current?.availablePackages?.firstOrNull()
                if (pkg == null) {
                    openPlayStoreSubscriptions(context)
                } else {
                    val intent = PaywallActivity.intent(context, pkg.identifier)
                    context.startActivity(intent)
                }
            },
        )
    }

    private fun openPlayStoreSubscriptions(context: Context) {
        val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=${context.packageName}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Unable to open subscription manager", Toast.LENGTH_SHORT).show()
        }
    }
}
