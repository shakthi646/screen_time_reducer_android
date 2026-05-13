package com.ksp.screentimereducer.presentation.onboarding

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ksp.screentimereducer.R
import com.ksp.screentimereducer.core.permissions.PermissionChecker
import com.ksp.screentimereducer.ui.theme.AppGradients
import com.ksp.screentimereducer.ui.theme.SoftPurpleLight

private data class Page(val title: Int, val body: Int, val showPermissions: Boolean = false)

private val PAGES = listOf(
    Page(R.string.onboarding_welcome_title, R.string.onboarding_welcome_body),
    Page(R.string.onboarding_delay_title, R.string.onboarding_delay_body),
    Page(R.string.onboarding_focus_title, R.string.onboarding_focus_body),
    Page(R.string.onboarding_permissions_title, R.string.onboarding_permissions_body, showPermissions = true),
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val vm: OnboardingViewModel = hiltViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    var index by remember { mutableIntStateOf(0) }
    val current = PAGES[index]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGradients.Hero)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (index < PAGES.lastIndex) {
                    TextButton(onClick = { index = PAGES.lastIndex }) {
                        Text(stringResource(R.string.onboarding_skip), color = Color.White)
                    }
                }
            }

            AnimatedContent(
                targetState = index,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally(tween(350)) { it } + fadeIn() togetherWith
                            slideOutHorizontally(tween(350)) { -it } + fadeOut()
                    } else {
                        slideInHorizontally(tween(350)) { -it } + fadeIn() togetherWith
                            slideOutHorizontally(tween(350)) { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "onboarding"
            ) { i ->
                val page = PAGES[i]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(page.title),
                        color = Color.White,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(page.body),
                        color = SoftPurpleLight,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                    if (page.showPermissions) {
                        Spacer(Modifier.height(28.dp))
                        PermissionsList()
                    }
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Dots(count = PAGES.size, selected = index)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (index < PAGES.lastIndex) index += 1
                        else {
                            vm.complete()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    val label = if (index == PAGES.lastIndex)
                        stringResource(R.string.onboarding_get_started)
                    else stringResource(R.string.onboarding_continue)
                    Text(label, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    LaunchedEffect(state.onboardingComplete) {
        if (state.onboardingComplete) onFinished()
    }
}

@Composable
private fun Dots(count: Int, selected: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { i ->
            val active = i == selected
            Box(
                Modifier
                    .height(8.dp)
                    .width(if (active) 24.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (active) Color.White else Color.White.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
private fun PermissionsList() {
    val context = LocalContext.current
    val usage by rememberPermissionState { PermissionChecker.hasUsageAccess(context) }
    val accessibility by rememberPermissionState { PermissionChecker.hasAccessibility(context) }
    val overlay by rememberPermissionState { PermissionChecker.hasOverlay(context) }

    PermissionRow(
        title = stringResource(R.string.perm_usage_title),
        body = stringResource(R.string.perm_usage_body),
        granted = usage,
        onGrant = {
            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    )
    Spacer(Modifier.height(12.dp))
    PermissionRow(
        title = stringResource(R.string.perm_accessibility_title),
        body = stringResource(R.string.perm_accessibility_body),
        granted = accessibility,
        onGrant = {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    )
    Spacer(Modifier.height(12.dp))
    PermissionRow(
        title = stringResource(R.string.perm_overlay_title),
        body = stringResource(R.string.perm_overlay_body),
        granted = overlay,
        onGrant = {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.packageName),
            )
            context.startActivity(intent)
        }
    )
}

/**
 * Re-checks a permission whenever the host re-enters the foreground (ON_RESUME),
 * so the tick appears as soon as the user returns from the system Settings screen.
 */
@Composable
private fun rememberPermissionState(check: () -> Boolean): State<Boolean> {
    val state = remember { mutableStateOf(check()) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                state.value = check()
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    return state
}

@Composable
private fun PermissionRow(title: String, body: String, granted: Boolean, onGrant: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(Color.White.copy(alpha = 0.1f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(body, color = SoftPurpleLight, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(12.dp))
        if (granted) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
            }
        } else {
            OutlinedButton(onClick = onGrant) {
                Text(stringResource(R.string.perm_grant), color = Color.White)
            }
        }
    }
}

