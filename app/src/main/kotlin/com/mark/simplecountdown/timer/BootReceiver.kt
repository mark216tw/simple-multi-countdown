package com.mark.simplecountdown.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        TimerStateStore.dismissAllAlarms(context)
        if (TimerStateStore.rebaseAfterBootIfNeeded(context).none { it.active }) return

        val serviceIntent = Intent(context, TimerForegroundService::class.java).apply {
            action = TimerForegroundService.ACTION_SYNC
        }
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
