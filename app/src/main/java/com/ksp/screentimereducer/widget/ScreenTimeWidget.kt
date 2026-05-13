package com.ksp.screentimereducer.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.ksp.screentimereducer.MainActivity
import com.ksp.screentimereducer.core.time.TimeFormatter
import com.ksp.screentimereducer.widget.WidgetSnapshotLoader.loadSnapshot

class ScreenTimeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val snapshot = loadSnapshot(context)
        provideContent {
            GlanceTheme {
                WidgetContent(snapshot)
            }
        }
    }
}

@Composable
private fun WidgetContent(snapshot: WidgetSnapshot) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF1F1147)))
            .cornerRadius(20.dp)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "Today",
            style = TextStyle(
                color = ColorProvider(Color(0xFFB6A6FF)),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = TimeFormatter.durationCompact(snapshot.totalMs),
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(modifier = GlanceModifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MiniStat(label = "Unlocks", value = "${snapshot.unlocks}")
            Spacer(modifier = GlanceModifier.width(12.dp))
            MiniStat(
                label = if (snapshot.focusActive) "Focus" else "Streak",
                value = if (snapshot.focusActive) {
                    TimeFormatter.durationCompact(snapshot.focusElapsedMs)
                } else {
                    "${snapshot.streak}d"
                },
            )
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(),
            style = TextStyle(
                color = ColorProvider(Color(0xFFB6A6FF)),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(modifier = GlanceModifier.height(2.dp))
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(Color.White),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}
