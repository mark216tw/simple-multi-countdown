package com.mark.simplecountdown.model

data class TimerPreset(
    val id: String,
    val name: String,
    val durationSeconds: Long,
    val colorValue: Int,
) {
    companion object {
        val defaults = listOf(
            TimerPreset("pomodoro", "專注工作", 25 * 60, 0xFFE45C4F.toInt()),
            TimerPreset("short-break", "短暫休息", 5 * 60, 0xFF5C8F73.toInt()),
            TimerPreset("noodles", "泡麵", 3 * 60, 0xFFF2B84B.toInt()),
            TimerPreset("exercise", "運動休息", 60, 0xFF3F7CAC.toInt()),
        )

        val colors = listOf(
            0xFFE45C4F.toInt(),
            0xFFF2B84B.toInt(),
            0xFF5C8F73.toInt(),
            0xFF3F7CAC.toInt(),
            0xFF7B61A8.toInt(),
            0xFFB85C86.toInt(),
            0xFF4D8F91.toInt(),
            0xFF7B6D5C.toInt(),
            0xFFF97316.toInt(),
            0xFF00A6FB.toInt(),
            0xFF4F46E5.toInt(),
            0xFF475569.toInt(),
        )
    }
}
