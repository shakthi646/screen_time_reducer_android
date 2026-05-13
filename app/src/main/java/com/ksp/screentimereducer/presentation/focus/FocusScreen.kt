package com.ksp.screentimereducer.presentation.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ksp.screentimereducer.core.time.TimeFormatter
import com.ksp.screentimereducer.domain.model.FocusMode
import com.ksp.screentimereducer.domain.model.FocusSession
import com.ksp.screentimereducer.presentation.components.GradientCard
import com.ksp.screentimereducer.presentation.components.SectionHeader
import com.ksp.screentimereducer.ui.theme.AppGradients
import com.ksp.screentimereducer.ui.theme.SoftPurpleLight
import kotlinx.coroutines.delay

@Composable
fun FocusScreen() {
    val vm: FocusViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var endDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    "Focus",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
            item {
                ActiveOrCta(
                    active = state.active,
                    streak = state.streak,
                    completed = state.completedThisWeek,
                    onRequestStop = { endDialog = true },
                )
            }
            if (state.active == null) {
                item { SectionHeader("Choose a mode") }
                item { ModePicker(onStart = { mode, mins -> vm.start(mode, mins) }) }
            }
        }
    }

    if (endDialog) {
        AlertDialog(
            onDismissRequest = { endDialog = false },
            title = { Text("End focus session?") },
            text = { Text("Your session won't count toward your streak unless the full duration completes.") },
            confirmButton = {
                Button(onClick = {
                    endDialog = false
                    vm.stop()
                }) { Text("End session") }
            },
            dismissButton = {
                TextButton(onClick = { endDialog = false }) { Text("Keep going") }
            }
        )
    }
}

@Composable
private fun ActiveOrCta(
    active: FocusSession?,
    streak: Int,
    completed: Int,
    onRequestStop: () -> Unit,
) {
    GradientCard(brush = AppGradients.Focus, modifier = Modifier.fillMaxWidth(), contentPadding = 24.dp) {
        Column {
            if (active != null) {
                // Tick once a second so the countdown re-renders smoothly without a VM ping.
                var nowMs by remember(active.id) { mutableLongStateOf(System.currentTimeMillis()) }
                LaunchedEffect(active.id) {
                    while (true) {
                        nowMs = System.currentTimeMillis()
                        delay(1_000)
                    }
                }
                val remaining = (active.startTime + active.plannedDurationMs - nowMs).coerceAtLeast(0L)
                Text(
                    text = active.mode.name.lowercase().replaceFirstChar { it.uppercase() } + " mode",
                    color = SoftPurpleLight,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formatCountdown(remaining),
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onRequestStop,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) {
                    Text("End session", color = Color.White)
                }
            } else {
                Text(
                    "Build a focus habit",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                Row {
                    Stat("Streak", "$streak d")
                    Spacer(Modifier.width(24.dp))
                    Stat("This week", "$completed")
                }
            }
        }
    }
}

private fun formatCountdown(ms: Long): String {
    val totalSec = (ms / 1_000).toInt()
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(label.uppercase(), color = SoftPurpleLight, style = MaterialTheme.typography.labelSmall)
        Text(value, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ModePicker(onStart: (FocusMode, Int) -> Unit) {
    var selected by remember { mutableStateOf(FocusMode.WORK) }
    var minutes by remember { mutableIntStateOf(25) }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FocusMode.values().forEach { mode ->
                ModeRow(
                    mode = mode,
                    selected = mode == selected,
                    onSelect = { selected = mode },
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Duration",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            DurationPicker(minutes = minutes, onChange = { minutes = it })
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onStart(selected, minutes) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("Start focus", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ModeRow(mode: FocusMode, selected: Boolean, onSelect: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(onClick = onSelect)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = mode.label(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
        )
        SelectionIndicator(selected = selected)
    }
}

@Composable
private fun SelectionIndicator(selected: Boolean) {
    if (selected) {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp),
            )
        }
    } else {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .border(width = 2.dp, color = MaterialTheme.colorScheme.outline, shape = CircleShape),
        )
    }
}

private fun FocusMode.label(): String = name.lowercase().replaceFirstChar { it.uppercase() }

@Composable
private fun DurationPicker(minutes: Int, onChange: (Int) -> Unit) {
    val options = listOf(15, 25, 45, 60, 90)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { m ->
            DurationChip(label = "${m}m", selected = m == minutes, onClick = { onChange(m) })
        }
    }
}

@Composable
private fun DurationChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
