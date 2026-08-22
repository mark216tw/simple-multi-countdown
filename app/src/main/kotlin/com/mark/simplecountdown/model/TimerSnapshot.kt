package com.mark.simplecountdown.model

data class TimerSnapshot(
    val id: String = "",
    val active: Boolean = false,
    val paused: Boolean = false,
    val name: String = "",
    val originalDurationSeconds: Long = 0,
    val remainingSeconds: Long = 0,
    val colorValue: Int = 0xFFE45C4F.toInt(),
    val soundEnabled: Boolean = true,
    val tickSoundEnabled: Boolean = false,
    val keepScreenOn: Boolean = false,
    val alarmRinging: Boolean = false,
    val alarmDurationSeconds: Long = 60,
)

enum class TimerPhase {
    INACTIVE,
    RUNNING,
    PAUSED,
    RINGING,
    COMPLETED,
}

val TimerSnapshot.phase: TimerPhase
    get() = when {
        alarmRinging -> TimerPhase.RINGING
        active && paused -> TimerPhase.PAUSED
        active -> TimerPhase.RUNNING
        originalDurationSeconds > 0 -> TimerPhase.COMPLETED
        else -> TimerPhase.INACTIVE
    }
