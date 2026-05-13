package com.ksp.screentimereducer.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ksp.screentimereducer.domain.repository.UnlockRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Registered programmatically against ACTION_USER_PRESENT (sent every time
 * the user unlocks the device). Receiving system broadcasts dynamically
 * avoids the manifest-receiver background limit on Android 8+.
 */
@AndroidEntryPoint
class UnlockReceiver : BroadcastReceiver() {

    @Inject lateinit var unlockRepository: UnlockRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    unlockRepository.recordUnlock()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
