package com.ksp.screentimereducer.subscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ksp.screentimereducer.presentation.components.GradientCard
import com.ksp.screentimereducer.ui.theme.AppGradients
import com.ksp.screentimereducer.ui.theme.ScreenTimeReducerTheme
import com.ksp.screentimereducer.ui.theme.SoftPurpleLight
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith

class PaywallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferredId = intent.getStringExtra(EXTRA_PACKAGE_ID)
        setContent {
            ScreenTimeReducerTheme {
                PaywallContent(
                    preferredPackageId = preferredId,
                    onClose = { finish() },
                )
            }
        }
    }

    companion object {
        private const val EXTRA_PACKAGE_ID = "extra_package_id"

        fun intent(context: Context, packageId: String): Intent =
            Intent(context, PaywallActivity::class.java).apply {
                putExtra(EXTRA_PACKAGE_ID, packageId)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
    }
}

@Composable
private fun PaywallContent(preferredPackageId: String?, onClose: () -> Unit) {
    val activity = LocalActivityOrNull()
    var packages by remember { mutableStateOf<List<Package>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var purchasing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        Purchases.sharedInstance.getOfferingsWith(
            onError = { e: PurchasesError ->
                loading = false
                error = e.message
            },
            onSuccess = { offerings ->
                val list = offerings.current?.availablePackages.orEmpty()
                packages = list.sortedBy { p ->
                    if (p.identifier == preferredPackageId) 0 else 1
                }
                loading = false
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "ScreenTimeReducer Pro",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onClose) { Text("Close") }
        }
        Spacer(Modifier.height(12.dp))
        GradientCard(brush = AppGradients.Hero, modifier = Modifier.fillMaxWidth(), contentPadding = 20.dp) {
            Column {
                Text("Unlock everything", color = SoftPurpleLight, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Premium",
                    color = Color.White,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Unlimited focus modes • Detailed analytics • Custom challenges • Backup & restore",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        when {
            loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            error != null -> Text(
                "Couldn't load plans: ${error}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
            packages.isEmpty() -> Text(
                "No plans configured. Configure offerings in RevenueCat to enable purchases.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(packages, key = { it.identifier }) { pkg ->
                    PackageRow(
                        pkg = pkg,
                        purchasing = purchasing,
                        onSelect = {
                            if (activity != null) {
                                purchasing = true
                                Purchases.sharedInstance.purchaseWith(
                                    com.revenuecat.purchases.PurchaseParams.Builder(activity, pkg).build(),
                                    onError = { e, _ ->
                                        purchasing = false
                                        error = e.message
                                    },
                                    onSuccess = { _, _ ->
                                        purchasing = false
                                        onClose()
                                    },
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun Box(modifier: Modifier, contentAlignment: Alignment, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.Box(modifier = modifier, contentAlignment = contentAlignment) { content() }
}

@Composable
private fun PackageRow(pkg: Package, purchasing: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(pkg.product.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                pkg.product.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    pkg.product.price.formatted,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Button(
                    onClick = onSelect,
                    enabled = !purchasing,
                    colors = ButtonDefaults.buttonColors(),
                ) {
                    Text(if (purchasing) "..." else "Choose")
                }
            }
        }
    }
}

@Composable
private fun LocalActivityOrNull(): Activity? {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    var c: Context? = ctx
    while (c != null) {
        if (c is Activity) return c
        c = (c as? android.content.ContextWrapper)?.baseContext
    }
    return null
}
