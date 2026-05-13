package com.ksp.screentimereducer.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ksp.screentimereducer.core.time.TimeFormatter
import com.ksp.screentimereducer.domain.model.DailyTotals
import com.ksp.screentimereducer.domain.model.WeeklyPoint
import com.ksp.screentimereducer.presentation.components.AccessibilityRequiredBanner
import com.ksp.screentimereducer.presentation.components.AppIcon
import com.ksp.screentimereducer.presentation.components.BarChart
import com.ksp.screentimereducer.presentation.components.GradientCard
import com.ksp.screentimereducer.presentation.components.SectionHeader
import com.ksp.screentimereducer.presentation.components.StatCard
import com.ksp.screentimereducer.ui.theme.AppGradients
import com.ksp.screentimereducer.ui.theme.SoftPurpleLight

@Composable
fun DashboardScreen() {
    val vm: DashboardViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    // Pull fresh usage from UsageStatsManager every time the user re-enters the screen,
    // so the totals reflect time spent since last open without waiting on the 15-min worker.
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) vm.refresh()
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                FilledTonalIconButton(
                    onClick = { vm.refresh() },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Refresh")
                }
            }

            AccessibilityRequiredBanner()

            when (val s = state) {
                is DashboardUiState.Loading -> LoadingCard()
                is DashboardUiState.Empty -> EmptyCard(s.message)
                is DashboardUiState.Ready -> ReadyDashboard(totals = s.totals, weekly = s.weekly)
            }
        }
    }
}

@Composable
private fun ReadyDashboard(totals: DailyTotals, weekly: List<WeeklyPoint>) {
    HeroTotal(totalMs = totals.totalScreenTimeMs)
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            label = "Unlocks",
            value = totals.unlockCount.toString(),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Avg. session",
            value = TimeFormatter.durationCompact(totals.avgSessionMs),
            modifier = Modifier.weight(1f),
        )
    }

    SectionHeader(title = "Hourly usage")
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box(Modifier.padding(16.dp)) {
            BarChart(
                values = totals.hourly.map { it.totalMs.toFloat() },
                labels = listOf("0", "6", "12", "18", "23"),
            )
        }
    }

    SectionHeader(title = "This week")
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box(Modifier.padding(16.dp)) {
            BarChart(
                values = weekly.map { it.totalMs.toFloat() },
                labels = weekly.map { it.dayLabel },
                barColor = MaterialTheme.colorScheme.tertiary,
                highlightIndex = weekly.indices.lastOrNull(),
            )
        }
    }

    SectionHeader(title = "Most used")
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            totals.topApps.forEach { app ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(packageName = app.packageName, sizeDp = 36)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(app.label, style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = "${app.launchCount} opens",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = TimeFormatter.durationCompact(app.totalMs),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            if (totals.topApps.isEmpty()) {
                Text(
                    text = "No top apps yet today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))
}

@Composable
private fun HeroTotal(totalMs: Long) {
    GradientCard(brush = AppGradients.Hero, modifier = Modifier.fillMaxWidth(), contentPadding = 24.dp) {
        Column {
            Text(
                text = "Screen time".uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = SoftPurpleLight,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = TimeFormatter.durationCompact(totalMs),
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Synced just now",
                    color = SoftPurpleLight,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun EmptyCard(message: String) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(24.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingCard() {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(Modifier.fillMaxWidth().height(120.dp))
    }
}

