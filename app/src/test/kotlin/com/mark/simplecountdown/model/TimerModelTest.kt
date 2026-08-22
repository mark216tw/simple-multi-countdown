package com.mark.simplecountdown.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class TimerModelTest {
    @Test
    fun `default presets match the product specification`() {
        assertEquals(4, TimerPreset.defaults.size)
        assertEquals("專注工作", TimerPreset.defaults.first().name)
        assertEquals(25 * 60L, TimerPreset.defaults.first().durationSeconds)
    }

    @Test
    fun `default preset lists can be copied independently`() {
        val first = TimerPreset.defaults.toMutableList()
        val second = TimerPreset.defaults.toMutableList()
        first.removeAt(0)

        assertNotSame(first, second)
        assertEquals(3, first.size)
        assertEquals(4, second.size)
    }

    @Test
    fun `timer phase follows ringing active paused and completed priority`() {
        assertEquals(TimerPhase.INACTIVE, TimerSnapshot().phase)
        assertEquals(TimerPhase.RUNNING, TimerSnapshot(active = true).phase)
        assertEquals(TimerPhase.PAUSED, TimerSnapshot(active = true, paused = true).phase)
        assertEquals(TimerPhase.RINGING, TimerSnapshot(active = false, alarmRinging = true).phase)
        assertEquals(
            TimerPhase.COMPLETED,
            TimerSnapshot(active = false, originalDurationSeconds = 60).phase,
        )
    }

    @Test
    fun `alarm duration options include supported values`() {
        assertEquals(listOf(10L, 30L, 60L, 300L, 600L, -1L), TimerSettings.alarmDurationOptions)
    }

    @Test
    fun `preset editor offers twelve colors`() {
        assertEquals(12, TimerPreset.colors.size)
    }
}
