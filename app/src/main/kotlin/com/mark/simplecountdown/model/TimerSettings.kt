package com.mark.simplecountdown.model

data class TimerSettings(
    val alarmDurationSeconds: Long = 60,
    val tickSoundEnabled: Boolean = false,
    val keepScreenOn: Boolean = false,
    val darkMode: Boolean = false,
    val themeColor: AppThemeColor = AppThemeColor.CORAL,
) {
    val soundEnabled: Boolean
        get() = alarmDurationSeconds != 0L

    companion object {
        val alarmDurationOptions = listOf(0L, 10L, 30L, 60L, 300L, -1L)
    }
}

enum class AppThemeColor {
    CORAL,
    OCEAN,
    FOREST,
    VIOLET,
    AMBER,
    SKY,
}
