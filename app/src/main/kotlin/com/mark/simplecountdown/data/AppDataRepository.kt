package com.mark.simplecountdown.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mark.simplecountdown.model.TimerPreset
import com.mark.simplecountdown.model.TimerSettings
import com.mark.simplecountdown.model.AppThemeColor
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.appDataStore by preferencesDataStore(name = "app_data_v1")

data class StoredAppData(
    val presets: List<TimerPreset>,
    val lastCustomTimer: TimerPreset?,
    val settings: TimerSettings,
)

class AppDataRepository(private val context: Context) {
    private object Keys {
        val presets = stringPreferencesKey("timer_presets_v1")
        val lastCustomTimer = stringPreferencesKey("last_custom_timer_v1")
        val alarmDuration = longPreferencesKey("alarm_duration_seconds")
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val tickSoundEnabled = booleanPreferencesKey("tick_sound_enabled")
        val keepScreenOn = booleanPreferencesKey("keep_screen_on")
        val darkMode = booleanPreferencesKey("dark_mode")
        val themeColor = stringPreferencesKey("theme_color")
    }

    val data: Flow<StoredAppData> = context.appDataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(::decode)

    suspend fun savePresets(presets: List<TimerPreset>) {
        context.appDataStore.edit { preferences ->
            preferences[Keys.presets] = encodePresets(presets)
        }
    }

    suspend fun saveLastCustomTimer(preset: TimerPreset) {
        context.appDataStore.edit { preferences ->
            preferences[Keys.lastCustomTimer] = encodePreset(preset).toString()
        }
    }

    suspend fun saveSettings(settings: TimerSettings) {
        require(settings.alarmDurationSeconds in TimerSettings.alarmDurationOptions)
        context.appDataStore.edit { preferences ->
            preferences[Keys.alarmDuration] = settings.alarmDurationSeconds
            preferences[Keys.soundEnabled] = settings.soundEnabled
            preferences[Keys.tickSoundEnabled] = settings.tickSoundEnabled
            preferences[Keys.keepScreenOn] = settings.keepScreenOn
            preferences[Keys.darkMode] = settings.darkMode
            preferences[Keys.themeColor] = settings.themeColor.name
        }
    }

    private fun decode(preferences: Preferences): StoredAppData {
        val presets = preferences[Keys.presets]?.let(::decodePresets) ?: TimerPreset.defaults
        val lastCustom = preferences[Keys.lastCustomTimer]?.let(::decodePreset)
        val storedAlarmDuration = preferences[Keys.alarmDuration]
            ?.takeIf { it in TimerSettings.alarmDurationOptions }
            ?: 60L
        val alarmDuration = if (preferences[Keys.soundEnabled] == false) 0L else storedAlarmDuration
        return StoredAppData(
            presets = presets,
            lastCustomTimer = lastCustom,
            settings = TimerSettings(
                alarmDurationSeconds = alarmDuration,
                tickSoundEnabled = preferences[Keys.tickSoundEnabled] ?: false,
                keepScreenOn = preferences[Keys.keepScreenOn] ?: false,
                darkMode = preferences[Keys.darkMode] ?: false,
                themeColor = preferences[Keys.themeColor]
                    ?.let { stored -> AppThemeColor.entries.firstOrNull { it.name == stored } }
                    ?: AppThemeColor.CORAL,
            ),
        )
    }

    private fun encodePresets(presets: List<TimerPreset>): String = JSONArray().apply {
        presets.forEach { put(encodePreset(it)) }
    }.toString()

    private fun decodePresets(encoded: String): List<TimerPreset> = runCatching {
        val array = JSONArray(encoded)
        List(array.length()) { index -> decodePreset(array.getJSONObject(index)) }
    }.getOrElse { TimerPreset.defaults }

    private fun decodePreset(encoded: String): TimerPreset? = runCatching {
        decodePreset(JSONObject(encoded))
    }.getOrNull()

    private fun encodePreset(preset: TimerPreset) = JSONObject()
        .put("id", preset.id)
        .put("name", preset.name)
        .put("durationSeconds", preset.durationSeconds)
        .put("colorValue", preset.colorValue.toLong() and 0xffffffffL)

    private fun decodePreset(value: JSONObject) = TimerPreset(
        id = value.getString("id"),
        name = value.getString("name"),
        durationSeconds = value.getLong("durationSeconds").coerceIn(1, 359999),
        colorValue = value.getLong("colorValue").toInt(),
    )
}
