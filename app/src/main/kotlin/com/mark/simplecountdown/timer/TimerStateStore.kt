package com.mark.simplecountdown.timer

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import android.provider.Settings
import com.mark.simplecountdown.model.TimerSnapshot
import java.util.UUID
import kotlin.math.abs
import org.json.JSONArray
import org.json.JSONObject

data class TimerRecord(
    val id: String,
    val active: Boolean,
    val paused: Boolean,
    val name: String,
    val originalDurationSeconds: Long,
    val endAtElapsedMillis: Long,
    val wallClockEndAtMillis: Long,
    val bootEpochMillis: Long,
    val bootCount: Int,
    val sameBoot: Boolean,
    val remainingWhenPaused: Long,
    val colorValue: Int,
    val soundEnabled: Boolean,
    val tickSoundEnabled: Boolean,
    val keepScreenOn: Boolean,
    val alarmRinging: Boolean,
    val alarmDurationSeconds: Long,
    val alarmStartedAtWallClockMillis: Long,
) {
    fun remainingSeconds(): Long {
        if (!active) return 0
        if (paused) return remainingWhenPaused.coerceAtLeast(0)
        val remainingMillis = if (sameBoot) {
            endAtElapsedMillis - SystemClock.elapsedRealtime()
        } else {
            wallClockEndAtMillis - System.currentTimeMillis()
        }
        return ((remainingMillis + 999) / 1000).coerceAtLeast(0)
    }

    fun toSnapshot() = TimerSnapshot(
        id = id,
        active = active,
        paused = paused,
        name = name,
        originalDurationSeconds = originalDurationSeconds,
        remainingSeconds = remainingSeconds(),
        colorValue = colorValue,
        soundEnabled = soundEnabled,
        tickSoundEnabled = tickSoundEnabled,
        keepScreenOn = keepScreenOn,
        alarmRinging = alarmRinging,
        alarmDurationSeconds = alarmDurationSeconds,
    )
}

object TimerStateStore {
    private const val PREFS = "native_timer_state_v1"
    private const val RECORDS = "timer_records_v2"

    @Synchronized
    fun readAll(context: Context): List<TimerRecord> {
        val preferences = preferences(context)
        val encoded = preferences.getString(RECORDS, null)
        if (encoded != null) return decodeRecords(context, encoded)

        val legacy = decodeLegacyRecord(context, preferences)
        val records = if (legacy != null) listOf(legacy) else emptyList()
        writeAll(preferences, records)
        return records
    }

    @Synchronized
    fun read(context: Context, id: String): TimerRecord? = readAll(context).firstOrNull { it.id == id }

    @Synchronized
    fun start(
        context: Context,
        id: String = UUID.randomUUID().toString(),
        name: String,
        durationSeconds: Long,
        colorValue: Int,
        soundEnabled: Boolean,
        tickSoundEnabled: Boolean,
        keepScreenOn: Boolean,
        alarmDurationSeconds: Long,
    ): TimerRecord {
        val duration = durationSeconds.coerceIn(1, 359999)
        val nowElapsed = SystemClock.elapsedRealtime()
        val nowWallClock = System.currentTimeMillis()
        val record = TimerRecord(
            id = id,
            active = true,
            paused = false,
            name = name,
            originalDurationSeconds = duration,
            endAtElapsedMillis = nowElapsed + duration * 1000,
            wallClockEndAtMillis = nowWallClock + duration * 1000,
            bootEpochMillis = nowWallClock - nowElapsed,
            bootCount = currentBootCount(context),
            sameBoot = true,
            remainingWhenPaused = duration,
            colorValue = colorValue,
            soundEnabled = soundEnabled,
            tickSoundEnabled = tickSoundEnabled,
            keepScreenOn = keepScreenOn,
            alarmRinging = false,
            alarmDurationSeconds = normalizeAlarmDuration(alarmDurationSeconds),
            alarmStartedAtWallClockMillis = 0,
        )
        replace(context, record)
        return record
    }

    @Synchronized
    fun pause(context: Context, id: String): TimerRecord? = update(context, id) { current ->
        val remaining = current.remainingSeconds()
        if (!current.active || current.paused || remaining <= 0) current
        else current.copy(paused = true, remainingWhenPaused = remaining)
    }

    @Synchronized
    fun resume(context: Context, id: String): TimerRecord? = update(context, id) { current ->
        if (!current.active || !current.paused) return@update current
        val nowElapsed = SystemClock.elapsedRealtime()
        val nowWallClock = System.currentTimeMillis()
        current.copy(
            paused = false,
            endAtElapsedMillis = nowElapsed + current.remainingWhenPaused * 1000,
            wallClockEndAtMillis = nowWallClock + current.remainingWhenPaused * 1000,
            bootEpochMillis = nowWallClock - nowElapsed,
            bootCount = currentBootCount(context),
            sameBoot = true,
        )
    }

    @Synchronized
    fun addTime(context: Context, id: String, seconds: Long): TimerRecord? = update(context, id) { current ->
        if (!current.active || seconds <= 0) return@update current
        if (current.paused) {
            current.copy(remainingWhenPaused = (current.remainingWhenPaused + seconds).coerceAtMost(359999))
        } else {
            val addition = seconds.coerceAtMost((359999 - current.remainingSeconds()).coerceAtLeast(0)) * 1000
            current.copy(
                endAtElapsedMillis = current.endAtElapsedMillis + addition,
                wallClockEndAtMillis = current.wallClockEndAtMillis + addition,
            )
        }
    }

    @Synchronized
    fun reset(context: Context, id: String): TimerRecord? = update(context, id) { current ->
        if (!current.active) return@update current
        val nowElapsed = SystemClock.elapsedRealtime()
        val nowWallClock = System.currentTimeMillis()
        current.copy(
            paused = false,
            endAtElapsedMillis = nowElapsed + current.originalDurationSeconds * 1000,
            wallClockEndAtMillis = nowWallClock + current.originalDurationSeconds * 1000,
            bootEpochMillis = nowWallClock - nowElapsed,
            bootCount = currentBootCount(context),
            sameBoot = true,
            remainingWhenPaused = current.originalDurationSeconds,
        )
    }

    @Synchronized
    fun rebaseAfterBootIfNeeded(context: Context): List<TimerRecord> {
        val nowElapsed = SystemClock.elapsedRealtime()
        val nowWallClock = System.currentTimeMillis()
        val updated = readAll(context).map { current ->
            if (!current.active || current.sameBoot) current else current.copy(
                endAtElapsedMillis = nowElapsed + current.remainingSeconds() * 1000,
                wallClockEndAtMillis = nowWallClock + current.remainingSeconds() * 1000,
                bootEpochMillis = nowWallClock - nowElapsed,
                bootCount = currentBootCount(context),
                sameBoot = true,
            )
        }
        writeAll(preferences(context), updated)
        return updated
    }

    @Synchronized
    fun remove(context: Context, id: String) {
        writeAll(preferences(context), readAll(context).filterNot { it.id == id })
    }

    @Synchronized
    fun complete(context: Context, id: String): TimerRecord? = update(context, id) { current ->
        val shouldRing = current.soundEnabled
        current.copy(
            active = false,
            paused = false,
            endAtElapsedMillis = 0,
            wallClockEndAtMillis = 0,
            remainingWhenPaused = 0,
            alarmRinging = shouldRing,
            alarmStartedAtWallClockMillis = if (shouldRing) System.currentTimeMillis() else 0,
        )
    }

    @Synchronized
    fun dismissAlarm(context: Context, id: String): TimerRecord? = update(context, id) { current ->
        current.copy(alarmRinging = false, alarmStartedAtWallClockMillis = 0)
    }

    @Synchronized
    fun dismissAllAlarms(context: Context) {
        val updated = readAll(context).map { current ->
            current.copy(alarmRinging = false, alarmStartedAtWallClockMillis = 0)
        }
        writeAll(preferences(context), updated)
    }

    @Synchronized
    fun updateSettings(
        context: Context,
        alarmDurationSeconds: Long,
        soundEnabled: Boolean,
        tickSoundEnabled: Boolean,
        keepScreenOn: Boolean,
    ) {
        val updated = readAll(context).map { current ->
            val keepRinging = current.alarmRinging && soundEnabled
            current.copy(
                alarmDurationSeconds = normalizeAlarmDuration(alarmDurationSeconds),
                soundEnabled = soundEnabled,
                tickSoundEnabled = tickSoundEnabled,
                keepScreenOn = keepScreenOn,
                alarmRinging = keepRinging,
                alarmStartedAtWallClockMillis = if (keepRinging) current.alarmStartedAtWallClockMillis else 0,
            )
        }
        writeAll(preferences(context), updated)
    }

    private fun update(context: Context, id: String, transform: (TimerRecord) -> TimerRecord): TimerRecord? {
        val records = readAll(context)
        val current = records.firstOrNull { it.id == id } ?: return null
        val updated = transform(current)
        writeAll(preferences(context), records.map { if (it.id == id) updated else it })
        return updated
    }

    private fun replace(context: Context, record: TimerRecord) {
        // Finished timers only need to live long enough for the result screen's restart action.
        val records = readAll(context).filter { it.active || it.alarmRinging || it.id == record.id }
        val updated = if (records.any { it.id == record.id }) {
            records.map { if (it.id == record.id) record else it }
        } else {
            records + record
        }
        writeAll(preferences(context), updated)
    }

    private fun decodeRecords(context: Context, encoded: String): List<TimerRecord> = runCatching {
        val array = JSONArray(encoded)
        List(array.length()) { index -> decodeRecord(context, array.getJSONObject(index)) }
    }.getOrDefault(emptyList())

    private fun decodeRecord(context: Context, value: JSONObject): TimerRecord {
        val bootCount = value.optInt("bootCount", -1)
        val bootEpoch = value.optLong("bootEpochMillis", 0)
        return TimerRecord(
            id = value.getString("id"),
            active = value.optBoolean("active"),
            paused = value.optBoolean("paused"),
            name = value.optString("name"),
            originalDurationSeconds = value.optLong("originalDurationSeconds"),
            endAtElapsedMillis = value.optLong("endAtElapsedMillis"),
            wallClockEndAtMillis = value.optLong("wallClockEndAtMillis"),
            bootEpochMillis = bootEpoch,
            bootCount = bootCount,
            sameBoot = isSameBoot(context, bootCount, bootEpoch),
            remainingWhenPaused = value.optLong("remainingWhenPaused"),
            colorValue = value.optLong("colorValue", 0xFFE45C4FL).toInt(),
            soundEnabled = value.optBoolean("soundEnabled", true),
            tickSoundEnabled = value.optBoolean("tickSoundEnabled"),
            keepScreenOn = value.optBoolean("keepScreenOn"),
            alarmRinging = value.optBoolean("alarmRinging"),
            alarmDurationSeconds = normalizeAlarmDuration(value.optLong("alarmDurationSeconds", 60)),
            alarmStartedAtWallClockMillis = value.optLong("alarmStartedAtWallClockMillis"),
        )
    }

    private fun decodeLegacyRecord(context: Context, preferences: SharedPreferences): TimerRecord? {
        val active = preferences.getBoolean("active", false)
        val ringing = preferences.getBoolean("alarm_ringing", false)
        val duration = preferences.getLong("original_duration", 0)
        if (!active && !ringing && duration <= 0) return null
        val bootCount = preferences.getInt("boot_count", -1)
        val bootEpoch = preferences.getLong("boot_epoch", 0)
        return TimerRecord(
            id = "legacy-${UUID.randomUUID()}",
            active = active,
            paused = preferences.getBoolean("paused", false),
            name = preferences.getString("name", "") ?: "",
            originalDurationSeconds = duration,
            endAtElapsedMillis = preferences.getLong("end_at_elapsed", 0),
            wallClockEndAtMillis = preferences.getLong("wall_clock_end_at", 0),
            bootEpochMillis = bootEpoch,
            bootCount = bootCount,
            sameBoot = isSameBoot(context, bootCount, bootEpoch),
            remainingWhenPaused = preferences.getLong("paused_remaining", 0),
            colorValue = preferences.getInt("color", 0xFFE45C4F.toInt()),
            soundEnabled = preferences.getBoolean("sound", true),
            tickSoundEnabled = preferences.getBoolean("tick_sound", false),
            keepScreenOn = preferences.getBoolean("keep_screen_on", false),
            alarmRinging = ringing,
            alarmDurationSeconds = normalizeAlarmDuration(preferences.getLong("alarm_duration", 60)),
            alarmStartedAtWallClockMillis = preferences.getLong("alarm_started_at", 0),
        )
    }

    private fun encodeRecord(record: TimerRecord) = JSONObject()
        .put("id", record.id)
        .put("active", record.active)
        .put("paused", record.paused)
        .put("name", record.name)
        .put("originalDurationSeconds", record.originalDurationSeconds)
        .put("endAtElapsedMillis", record.endAtElapsedMillis)
        .put("wallClockEndAtMillis", record.wallClockEndAtMillis)
        .put("bootEpochMillis", record.bootEpochMillis)
        .put("bootCount", record.bootCount)
        .put("remainingWhenPaused", record.remainingWhenPaused)
        .put("colorValue", record.colorValue.toLong() and 0xffffffffL)
        .put("soundEnabled", record.soundEnabled)
        .put("tickSoundEnabled", record.tickSoundEnabled)
        .put("keepScreenOn", record.keepScreenOn)
        .put("alarmRinging", record.alarmRinging)
        .put("alarmDurationSeconds", record.alarmDurationSeconds)
        .put("alarmStartedAtWallClockMillis", record.alarmStartedAtWallClockMillis)

    @SuppressLint("ApplySharedPref", "UseKtx")
    private fun writeAll(preferences: SharedPreferences, records: List<TimerRecord>) {
        val encoded = JSONArray().apply { records.forEach { put(encodeRecord(it)) } }.toString()
        preferences.edit().putString(RECORDS, encoded).commit()
    }

    private fun preferences(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun isSameBoot(context: Context, bootCount: Int, bootEpoch: Long): Boolean {
        val currentBootCount = currentBootCount(context)
        return if (currentBootCount >= 0 && bootCount >= 0) {
            currentBootCount == bootCount
        } else {
            abs((System.currentTimeMillis() - SystemClock.elapsedRealtime()) - bootEpoch) < 60_000
        }
    }

    private fun currentBootCount(context: Context): Int =
        Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)

    private fun normalizeAlarmDuration(seconds: Long): Long =
        if (seconds == -1L) -1L else seconds.coerceIn(10L, 600L)
}
