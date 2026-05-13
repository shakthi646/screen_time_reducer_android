package com.ksp.screentimereducer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ksp.screentimereducer.core.work.WorkScheduler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var workScheduler: WorkScheduler

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                workScheduler.scheduleRecurringJobs()
            }
        }
    }
}
