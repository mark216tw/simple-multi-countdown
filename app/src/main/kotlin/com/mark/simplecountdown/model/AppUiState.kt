package com.mark.simplecountdown.model

data class AppUiState(
    val presets: List<TimerPreset> = emptyList(),
    val lastCustomTimer: TimerPreset? = null,
    val settings: TimerSettings = TimerSettings(),
    val timers: List<TimerSnapshot> = emptyList(),
    val initialized: Boolean = false,
)
