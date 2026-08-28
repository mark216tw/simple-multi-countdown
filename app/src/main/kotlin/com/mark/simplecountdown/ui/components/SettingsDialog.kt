package com.mark.simplecountdown.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mark.simplecountdown.model.AppThemeColor
import com.mark.simplecountdown.model.TimerSettings

@Composable
fun SettingsDialog(
    initialSettings: TimerSettings,
    onDismiss: () -> Unit,
    onAppearancePreview: (AppThemeColor, Boolean) -> Unit,
    onConfirm: (TimerSettings) -> Unit,
) {
    var alarmDuration by remember { mutableLongStateOf(initialSettings.alarmDurationSeconds) }
    var tickSoundEnabled by remember { mutableStateOf(initialSettings.tickSoundEnabled) }
    var keepScreenOn by remember { mutableStateOf(initialSettings.keepScreenOn) }
    var darkMode by remember { mutableStateOf(initialSettings.darkMode) }
    var themeColor by remember { mutableStateOf(initialSettings.themeColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("設定") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("倒數完成鈴響時間", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(2.dp))
                TimerSettings.alarmDurationOptions.chunked(2).forEach { rowOptions ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowOptions.forEach { seconds ->
                            val selected = alarmDuration == seconds
                            FilterChip(
                                selected = selected,
                                onClick = { alarmDuration = seconds },
                                label = {
                                    Text(
                                        alarmDurationLabel(seconds),
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    )
                                },
                                leadingIcon = selectedChipIcon(selected),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("主題色", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(2.dp))
                AppThemeColor.entries.chunked(2).forEach { rowOptions ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowOptions.forEach { option ->
                            val selected = themeColor == option
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    themeColor = option
                                    onAppearancePreview(option, darkMode)
                                },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(Color(themePreviewColor(option))),
                                        )
                                        Spacer(Modifier.size(6.dp))
                                        Text(
                                            themeColorLabel(option),
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                SettingSwitch("深色模式", darkMode) {
                    darkMode = it
                    onAppearancePreview(themeColor, it)
                }
                SettingSwitch("倒數畫面保持常亮", keepScreenOn) { keepScreenOn = it }
                SettingSwitch("倒數進行時播放答答聲", tickSoundEnabled) {
                    tickSoundEnabled = it
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        TimerSettings(
                            alarmDurationSeconds = alarmDuration,
                            tickSoundEnabled = tickSoundEnabled,
                            keepScreenOn = keepScreenOn,
                            darkMode = darkMode,
                            themeColor = themeColor,
                        ),
                    )
                },
            ) { Text("儲存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

private fun selectedChipIcon(selected: Boolean): (@Composable () -> Unit)? = if (selected) {
    {
        Icon(
            Icons.Rounded.Check,
            contentDescription = "已選擇",
            modifier = Modifier.size(18.dp),
        )
    }
} else {
    null
}

@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Switch) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun alarmDurationLabel(seconds: Long): String = when (seconds) {
    -1L -> "不自動停止"
    0L -> "無聲"
    10L, 30L -> "$seconds 秒"
    60L -> "1 分鐘"
    else -> "${seconds / 60} 分鐘"
}

private fun themeColorLabel(themeColor: AppThemeColor): String = when (themeColor) {
    AppThemeColor.CORAL -> "珊瑚紅"
    AppThemeColor.OCEAN -> "海洋藍綠"
    AppThemeColor.FOREST -> "森林綠"
    AppThemeColor.VIOLET -> "紫羅蘭"
    AppThemeColor.AMBER -> "琥珀金"
    AppThemeColor.SKY -> "天空藍"
}

private fun themePreviewColor(themeColor: AppThemeColor): Long = when (themeColor) {
    AppThemeColor.CORAL -> 0xFF9B3E32
    AppThemeColor.OCEAN -> 0xFF006A6A
    AppThemeColor.FOREST -> 0xFF3F6541
    AppThemeColor.VIOLET -> 0xFF6D4EA2
    AppThemeColor.AMBER -> 0xFFB87900
    AppThemeColor.SKY -> 0xFF315DA8
}
