package com.ksp.screentimereducer.presentation.delay

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ksp.screentimereducer.R
import com.ksp.screentimereducer.presentation.components.AppIcon
import com.ksp.screentimereducer.presentation.components.CountdownRing
import com.ksp.screentimereducer.ui.theme.AppGradients
import com.ksp.screentimereducer.ui.theme.SoftPurpleLight
import kotlinx.coroutines.delay

private val MOTIVATIONAL = listOf(
    "What were you about to do?",
    "Is this the best use of your next 10 minutes?",
    "Your future self is watching.",
    "Notice the urge — it will pass.",
    "Pause. You don't have to scroll.",
    "Choose, don't react.",
    "A breath is a small rebellion.",
)

@Composable
fun DelayOverlayScreen(
    packageName: String,
    seconds: Int,
    reason: String,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
) {
    val context = LocalContext.current
    val label = remember(packageName) {
        runCatching {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        }.getOrDefault(packageName)
    }
    val message = remember { MOTIVATIONAL.random() }
    var remaining by remember { mutableIntStateOf(seconds) }
    var done by remember { mutableStateOf(false) }

    val progress by animateFloatAsState(
        targetValue = if (done) 1f else (seconds - remaining).toFloat() / seconds,
        animationSpec = tween(durationMillis = 950, easing = LinearEasing),
        label = "ring"
    )

    LaunchedEffect(Unit) {
        while (remaining > 0) {
            delay(1000)
            remaining -= 1
        }
        done = true
    }

    val reasonLabel = when (reason) {
        DelayOverlayActivity.REASON_FOCUS -> "Focus mode is on"
        DelayOverlayActivity.REASON_LIMIT -> "Daily limit reached"
        else -> "Mindful pause"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppGradients.Hero)
            .padding(WindowInsets.systemBars.asPaddingValues())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = reasonLabel,
                    color = SoftPurpleLight,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.delay_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.Center) {
                    CountdownRing(progress = progress, modifier = Modifier.size(220.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AppIcon(packageName = packageName, sizeDp = 64)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (done) "0" else remaining.toString(),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(Modifier.height(28.dp))
                Text(
                    text = stringResource(R.string.delay_question, label),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    color = SoftPurpleLight,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (done) 1f else 0.55f)
            ) {
                Button(
                    onClick = onContinue,
                    enabled = done,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(stringResource(R.string.delay_open), fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onCancel) {
                    Text(stringResource(R.string.delay_cancel), color = Color.White)
                }
            }
        }
    }
}
