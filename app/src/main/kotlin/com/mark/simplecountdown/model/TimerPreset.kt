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
            0xFF84CC16.toInt(),
            0xFF10B981.toInt(),
            0xFF06B6D4.toInt(),
            0xFF1E3A8A.toInt(),
            0xFFA855F7.toInt(),
            0xFFDB2777.toInt(),
            0xFFFB7185.toInt(),
            0xFFCA8A04.toInt(),
        )

        val colorNames = listOf(
            "紅色",
            "黃色",
            "綠色",
            "藍色",
            "紫色",
            "粉紅色",
            "青綠色",
            "棕色",
            "橘色",
            "天藍色",
            "靛藍色",
            "灰色",
            "萊姆綠",
            "翠綠色",
            "湖水藍",
            "海軍藍",
            "薰衣草紫",
            "洋紅色",
            "珊瑚粉",
            "金色",
        )
    }
}
