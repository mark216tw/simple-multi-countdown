package com.mark.simplecountdown.model

data class TimerSettings(
    val alarmDurationSeconds: Long = 60,
    val soundEnabled: Boolean = true,
    val tickSoundEnabled: Boolean = false,
    val keepScreenOn: Boolean = false,
    val darkMode: Boolean = false,
) {
    companion object {
        val alarmDurationOptions = listOf(10L, 30L, 60L, 300L, 600L, -1L)
    }
}
