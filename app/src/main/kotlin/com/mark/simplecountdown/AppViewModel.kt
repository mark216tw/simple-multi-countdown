package com.mark.simplecountdown

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mark.simplecountdown.data.AppDataRepository
import com.mark.simplecountdown.model.AppUiState
import com.mark.simplecountdown.model.TimerPreset
import com.mark.simplecountdown.model.TimerSettings
import com.mark.simplecountdown.timer.TimerRepository
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val dataRepository = AppDataRepository(application)
    private val timerRepository = TimerRepository(application)
    private val timers = MutableStateFlow(timerRepository.states())
    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages = _messages.asSharedFlow()

    val uiState = combine(dataRepository.data, timers) { stored, timerSnapshots ->
        AppUiState(
            presets = stored.presets,
            lastCustomTimer = stored.lastCustomTimer,
            settings = stored.settings,
            timers = timerSnapshots,
            initialized = true,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppUiState(timers = timers.value),
    )

    init {
        timerRepository.ensureService()
        viewModelScope.launch {
            while (isActive) {
                refreshTimer()
                delay(500)
            }
        }
    }

    fun addPreset(preset: TimerPreset) = updatePresets("無法儲存預設。") { it + preset }

    fun updatePreset(preset: TimerPreset) = updatePresets("無法儲存預設。") { presets ->
        presets.map { if (it.id == preset.id) preset else it }
    }

    fun duplicatePreset(preset: TimerPreset) = updatePresets("無法建立副本。") { presets ->
        val index = presets.indexOfFirst { it.id == preset.id }
        if (index < 0) return@updatePresets presets
        presets.toMutableList().apply {
            add(
                index + 1,
                preset.copy(id = UUID.randomUUID().toString(), name = "${preset.name} 副本"),
            )
        }
    }

    fun deletePreset(id: String) = updatePresets("無法刪除預設。") { presets ->
        presets.filterNot { it.id == id }
    }

    fun savePresetOrder(presets: List<TimerPreset>) {
        viewModelScope.launch {
            runCatching { dataRepository.savePresets(presets) }
                .onFailure { _messages.emit("無法儲存排序。") }
        }
    }

    fun reorderPresets(fromIndex: Int, toIndex: Int) = updatePresets("無法儲存排序。") { presets ->
        if (fromIndex !in presets.indices || toIndex !in presets.indices) return@updatePresets presets
        presets.toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
    }

    fun saveLastCustomTimer(preset: TimerPreset) {
        viewModelScope.launch {
            runCatching { dataRepository.saveLastCustomTimer(preset) }
                .onFailure { _messages.emit("無法記住自訂倒數設定。") }
        }
    }

    fun saveSettings(settings: TimerSettings) {
        viewModelScope.launch {
            runCatching {
                dataRepository.saveSettings(settings)
                timerRepository.updateSettings(settings)
                refreshTimer()
            }.onFailure { _messages.emit("無法儲存設定。") }
        }
    }

    fun startTimer(preset: TimerPreset): String? = runCatching {
        timerRepository.start(preset, uiState.value.settings)
    }.onSuccess {
        refreshTimer()
    }.onFailure {
        _messages.tryEmit("無法啟動背景計時服務。")
    }.getOrNull()

    fun restartTimer(id: String) {
        val snapshot = timers.value.firstOrNull { it.id == id } ?: return
        if (snapshot.originalDurationSeconds <= 0) return
        timerAction {
            timerRepository.start(
                preset = TimerPreset(
                    id = "restart-${UUID.randomUUID()}",
                    name = snapshot.name.ifBlank { "自訂倒數" },
                    durationSeconds = snapshot.originalDurationSeconds,
                    colorValue = snapshot.colorValue,
                ),
                settings = uiState.value.settings,
                timerId = id,
            )
        }
    }

    fun pauseTimer(id: String) = timerAction { timerRepository.pause(id) }

    fun resumeTimer(id: String) = timerAction { timerRepository.resume(id) }

    fun addTime(id: String, seconds: Long) = timerAction { timerRepository.addTime(id, seconds) }

    fun resetTimer(id: String) = timerAction { timerRepository.reset(id) }

    fun stopTimer(id: String) = timerAction { timerRepository.stop(id) }

    fun dismissAlarm(id: String) = timerAction { timerRepository.dismissAlarm(id) }

    fun onAppResumed() {
        refreshTimer()
        timerRepository.ensureService()
    }

    fun showMessage(message: String) {
        _messages.tryEmit(message)
    }

    private fun updatePresets(
        errorMessage: String,
        transform: (List<TimerPreset>) -> List<TimerPreset>,
    ) {
        viewModelScope.launch {
            runCatching { dataRepository.savePresets(transform(uiState.value.presets)) }
                .onFailure { _messages.emit(errorMessage) }
        }
    }

    private fun timerAction(
        errorMessage: String = "操作失敗，請稍後再試。",
        action: () -> Unit,
    ) {
        runCatching(action)
            .onSuccess { refreshTimer() }
            .onFailure { _messages.tryEmit(errorMessage) }
    }

    private fun refreshTimer() {
        timers.value = timerRepository.states()
    }
}
