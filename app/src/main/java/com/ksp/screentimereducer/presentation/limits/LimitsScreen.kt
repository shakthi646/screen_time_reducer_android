package com.ksp.screentimereducer.presentation.limits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ksp.screentimereducer.core.time.TimeFormatter
import com.ksp.screentimereducer.domain.model.AppRule
import com.ksp.screentimereducer.domain.model.InstalledApp
import com.ksp.screentimereducer.presentation.components.AccessibilityRequiredBanner
import com.ksp.screentimereducer.presentation.components.AppIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LimitsScreen() {
    val vm: LimitsViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<AppRule?>(null) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { vm.openPicker() },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("Add app") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Daily limits",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            item { AccessibilityRequiredBanner() }
            if (state.rules.isEmpty()) {
                item {
                    Text(
                        "No limits set. Tap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.rules, key = { it.packageName }) { rule ->
                RuleRow(
                    rule = rule,
                    onClick = { editing = rule },
                    onToggleDelay = { vm.toggleDelay(rule.packageName, it) },
                )
            }
        }
    }

    if (state.pickerOpen) {
        AppPickerSheet(
            apps = state.installed,
            selectedPackages = state.rules.map { it.packageName }.toSet(),
            onPick = { vm.addApp(it.packageName) },
            onDismiss = { vm.closePicker() },
        )
    }

    editing?.let { rule ->
        EditRuleDialog(
            rule = rule,
            onSave = { delay, limitMin ->
                vm.setDelayAndLimit(rule.packageName, delay, limitMin)
                editing = null
            },
            onDismiss = { editing = null }
        )
    }
}

@Composable
private fun RuleRow(
    rule: AppRule,
    onClick: () -> Unit,
    onToggleDelay: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(packageName = rule.packageName, sizeDp = 40)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = rule.packageName.substringAfterLast('.'),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                val sub = buildString {
                    if (rule.dailyLimitMs > 0) append("Limit ${TimeFormatter.durationCompact(rule.dailyLimitMs)}")
                    if (rule.delayEnabled) {
                        if (isNotEmpty()) append(" • ")
                        append("${rule.delaySeconds}s pause")
                    }
                    if (isEmpty()) append("No restrictions")
                }
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = rule.delayEnabled, onCheckedChange = onToggleDelay)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPickerSheet(
    apps: List<InstalledApp>,
    selectedPackages: Set<String>,
    onPick: (InstalledApp) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered by remember(apps, query) {
        derivedStateOf {
            val q = query.trim()
            if (q.isEmpty()) apps
            else apps.filter {
                it.label.contains(q, ignoreCase = true) ||
                    it.packageName.contains(q, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            Text(
                "Pick an app",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text("Search apps") },
                shape = MaterialTheme.shapes.large,
            )
            Spacer(Modifier.height(12.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 480.dp),
            ) {
                items(filtered, key = { it.packageName }) { app ->
                    val alreadyAdded = app.packageName in selectedPackages
                    AppPickerRow(
                        app = app,
                        alreadyAdded = alreadyAdded,
                        onClick = {
                            if (!alreadyAdded) onPick(app)
                        }
                    )
                }
                if (filtered.isEmpty()) {
                    item {
                        Text(
                            "No matches",
                            modifier = Modifier.padding(vertical = 24.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppPickerRow(
    app: InstalledApp,
    alreadyAdded: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !alreadyAdded, onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(packageName = app.packageName, sizeDp = 36)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                app.label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (alreadyAdded) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Already added",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun EditRuleDialog(
    rule: AppRule,
    onSave: (delay: Int, limitMinutes: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var delay by remember { mutableStateOf(rule.delaySeconds) }
    var minutes by remember { mutableStateOf((rule.dailyLimitMs / 60_000L).toInt()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Stepper(label = "Mindful pause", suffix = "s", value = delay, range = 3..30) { delay = it }
                Stepper(label = "Daily limit", suffix = "m", value = minutes, range = 0..720, step = 5) { minutes = it }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(delay, minutes) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun Stepper(
    label: String,
    suffix: String,
    value: Int,
    range: IntRange,
    step: Int = 1,
    onChange: (Int) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        TextButton(onClick = { onChange((value - step).coerceAtLeast(range.first)) }) { Text("−") }
        Box(
            modifier = Modifier
                .width(64.dp)
                .height(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center,
        ) {
            Text("$value$suffix", fontWeight = FontWeight.SemiBold)
        }
        TextButton(onClick = { onChange((value + step).coerceAtMost(range.last)) }) { Text("+") }
    }
}
