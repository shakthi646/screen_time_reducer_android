package com.ksp.screentimereducer.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compact Compose-only bar chart used on the dashboard. We render with
 * Canvas rather than pulling in a charting library to keep APK size
 * small and respect the calm/minimal design language.
 */
@Composable
fun BarChart(
    values: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
    barColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    highlightIndex: Int? = null,
) {
    val maxValue = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)
    val animated by animateFloatAsState(targetValue = 1f, animationSpec = tween(700), label = "bars")
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier.fillMaxWidth().height(height).padding(top = 4.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height - 18.dp)) {
            if (values.isEmpty()) return@Canvas
            val barCount = values.size
            val gap = 6.dp.toPx()
            val totalGap = gap * (barCount - 1).coerceAtLeast(0)
            val barWidth = (size.width - totalGap) / barCount
            val maxBarHeight = size.height

            values.forEachIndexed { i, v ->
                val frac = (v / maxValue).coerceIn(0f, 1f) * animated
                val barH = maxBarHeight * frac
                val x = i * (barWidth + gap)
                val y = maxBarHeight - barH

                // Track
                drawRoundedBar(
                    color = trackColor,
                    topLeft = Offset(x, 0f),
                    size = Size(barWidth, maxBarHeight),
                )
                // Value bar
                val brush = if (highlightIndex == i) {
                    Brush.verticalGradient(listOf(barColor, barColor.copy(alpha = 0.6f)))
                } else {
                    Brush.verticalGradient(listOf(barColor.copy(alpha = 0.9f), barColor.copy(alpha = 0.4f)))
                }
                drawRoundedBar(brush = brush, topLeft = Offset(x, y), size = Size(barWidth, barH))
            }
        }
        // Labels row
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().padding(top = height - 16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        ) {
            labels.forEach {
                Text(
                    text = it,
                    style = TextStyle(fontSize = 10.sp, color = labelColor),
                )
            }
        }
    }
}

private fun DrawScope.drawRoundedBar(
    color: Color? = null,
    brush: Brush? = null,
    topLeft: Offset,
    size: Size,
) {
    val radius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx(), 8.dp.toPx())
    if (brush != null) {
        drawRoundRect(brush = brush, topLeft = topLeft, size = size, cornerRadius = radius)
    } else if (color != null) {
        drawRoundRect(color = color, topLeft = topLeft, size = size, cornerRadius = radius)
    }
}
