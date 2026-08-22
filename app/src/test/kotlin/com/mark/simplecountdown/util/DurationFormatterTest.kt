package com.mark.simplecountdown.util

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationFormatterTest {
    @Test
    fun `formats durations below one hour as minutes and seconds`() {
        assertEquals("00:00", formatDuration(0))
        assertEquals("03:07", formatDuration(187))
        assertEquals("59:59", formatDuration(3599))
    }

    @Test
    fun `formats durations of one hour or more with hours`() {
        assertEquals("01:00:00", formatDuration(3600))
        assertEquals("99:59:59", formatDuration(359999))
    }

    @Test
    fun `clamps negative durations to zero`() {
        assertEquals("00:00", formatDuration(-10))
    }

    @Test
    fun `formats compact duration without empty units`() {
        assertEquals("25 分鐘", formatCompactDuration(1500))
        assertEquals("1 小時 1 分鐘 1 秒", formatCompactDuration(3661))
        assertEquals("5 秒", formatCompactDuration(5))
    }
}
