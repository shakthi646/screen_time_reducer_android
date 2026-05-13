package com.ksp.screentimereducer.data.work

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ksp.screentimereducer.widget.ScreenTimeWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class WidgetRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = runCatching {
        GlanceAppWidgetManager(applicationContext).getGlanceIds(ScreenTimeWidget::class.java)
        ScreenTimeWidget().updateAll(applicationContext)
        Result.success()
    }.getOrElse { Result.success() } // widget refresh is best-effort

    companion object {
        const val NAME = "widget_refresh_worker"
    }
}
