package com.mark.simplecountdown.timer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.mark.simplecountdown.model.TimerPreset
import com.mark.simplecountdown.model.TimerSettings
import com.mark.simplecountdown.model.TimerSnapshot
import java.util.UUID

class TimerRepository(context: Context) {
    private val appContext = context.applicationContext

    fun states(): List<TimerSnapshot> = TimerStateStore.readAll(appContext).map(TimerRecord::toSnapshot)

    fun start(preset: TimerPreset, settings: TimerSettings, timerId: String = UUID.randomUUID().toString()): String {
        TimerStateStore.start(
            context = appContext,
            id = timerId,
            name = preset.name,
            durationSeconds = preset.durationSeconds,
            colorValue = preset.colorValue,
            soundEnabled = settings.soundEnabled,
            tickSoundEnabled = settings.tickSoundEnabled,
            keepScreenOn = settings.keepScreenOn,
            alarmDurationSeconds = settings.alarmDurationSeconds,
        )
        try {
            ContextCompat.startForegroundService(
                appContext,
                serviceIntent(TimerForegroundService.ACTION_SYNC),
            )
        } catch (error: RuntimeException) {
            TimerStateStore.remove(appContext, timerId)
            throw error
        }
        return timerId
    }

    fun pause(id: String) {
        TimerStateStore.pause(appContext, id)
        sendCommand(TimerForegroundService.ACTION_SYNC)
    }

    fun resume(id: String) {
        TimerStateStore.resume(appContext, id)
        sendCommand(TimerForegroundService.ACTION_SYNC)
    }

    fun addTime(id: String, seconds: Long) {
        TimerStateStore.addTime(appContext, id, seconds)
        sendCommand(TimerForegroundService.ACTION_SYNC)
    }

    fun reset(id: String) {
        TimerStateStore.reset(appContext, id)
        sendCommand(TimerForegroundService.ACTION_SYNC)
    }

    fun stop(id: String) {
        TimerStateStore.remove(appContext, id)
        sendCommand(TimerForegroundService.ACTION_SYNC)
    }

    fun dismissAlarm(id: String) {
        TimerStateStore.dismissAlarm(appContext, id)
        sendCommand(TimerForegroundService.ACTION_SYNC)
    }

    fun updateSettings(settings: TimerSettings) {
        val hadRunningTimers = TimerStateStore.readAll(appContext).any { it.active || it.alarmRinging }
        TimerStateStore.updateSettings(
            context = appContext,
            alarmDurationSeconds = settings.alarmDurationSeconds,
            soundEnabled = settings.soundEnabled,
            tickSoundEnabled = settings.tickSoundEnabled,
            keepScreenOn = settings.keepScreenOn,
        )
        if (hadRunningTimers) {
            sendCommand(TimerForegroundService.ACTION_SYNC)
        }
    }

    fun ensureService() {
        if (TimerStateStore.readAll(appContext).none { it.active || it.alarmRinging }) return
        ContextCompat.startForegroundService(
            appContext,
            serviceIntent(TimerForegroundService.ACTION_SYNC),
        )
    }

    private fun sendCommand(action: String, configure: Intent.() -> Unit = {}) {
        appContext.startService(serviceIntent(action).apply(configure))
    }

    private fun serviceIntent(action: String) =
        Intent(appContext, TimerForegroundService::class.java).apply { this.action = action }
}
