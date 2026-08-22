package com.mark.simplecountdown.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mark.simplecountdown.model.TimerPreset
import java.util.UUID

@Composable
fun PresetEditorDialog(
    preset: TimerPreset?,
    customTimer: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (TimerPreset) -> Unit,
) {
    val duration = preset?.durationSeconds ?: 0
    var name by remember(preset?.id, customTimer) {
        mutableStateOf(preset?.name ?: if (customTimer) "自訂倒數" else "")
    }
    var hours by remember(preset?.id) { mutableStateOf(twoDigits(duration / 3600)) }
    var minutes by remember(preset?.id) { mutableStateOf(twoDigits((duration % 3600) / 60)) }
    var seconds by remember(preset?.id) { mutableStateOf(twoDigits(duration % 60)) }
    var selectedColor by remember(preset?.id) {
        mutableIntStateOf(preset?.colorValue ?: TimerPreset.colors.first())
    }
    var error by remember { mutableStateOf<String?>(null) }

    fun submit() {
        val trimmedName = name.trim()
        val totalSeconds = (hours.toLongOrNull() ?: 0) * 3600 +
            (minutes.toLongOrNull() ?: 0) * 60 +
            (seconds.toLongOrNull() ?: 0)
        error = when {
            trimmedName.isEmpty() -> "請輸入計時器名稱"
            totalSeconds !in 1..359999 -> "時間需介於 1 秒到 99:59:59"
            else -> null
        }
        if (error != null) return
        onConfirm(
            TimerPreset(
                id = preset?.id ?: UUID.randomUUID().toString(),
                name = trimmedName,
                durationSeconds = totalSeconds,
                colorValue = selectedColor,
            ),
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                when {
                    customTimer -> "自訂倒數"
                    preset == null -> "新增預設"
                    else -> "編輯預設"
                },
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 24) name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("名稱") },
                    singleLine = true,
                    supportingText = { Text("${name.length}/24") },
                )
                Spacer(Modifier.height(12.dp))
                Text("倒數時間", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimeField(hours, "時", 99) { hours = it }
                    Text(":", modifier = Modifier.padding(horizontal = 5.dp))
                    TimeField(minutes, "分", 59) { minutes = it }
                    Text(":", modifier = Modifier.padding(horizontal = 5.dp))
                    TimeField(seconds, "秒", 59) { seconds = it }
                }
                Spacer(Modifier.height(20.dp))
                Text("識別色", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(10.dp))
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
                )
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TimerPreset.colors.chunked(4).forEachIndexed { rowIndex, rowColors ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowColors.forEachIndexed { columnIndex, colorValue ->
                                val selected = colorValue == selectedColor
                                val color = Color(colorValue)
                                val colorName = colorNames[rowIndex * 4 + columnIndex]
                                Surface(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .semantics {
                                            contentDescription = colorName
                                            stateDescription = if (selected) "已選擇" else "未選擇"
                                        }
                                        .clickable(role = Role.RadioButton) {
                                            selectedColor = colorValue
                                        },
                                    shape = CircleShape,
                                    color = color,
                                    border = if (selected) {
                                        BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                                    } else {
                                        null
                                    },
                                ) {
                                    if (selected) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = "已選擇顏色",
                                            modifier = Modifier.padding(10.dp),
                                            tint = if (color.luminance() < 0.5f) Color.White else Color.Black,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                error?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = ::submit) { Text(if (customTimer) "開始" else "儲存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TimeField(
    value: String,
    label: String,
    max: Int,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { candidate ->
            if (candidate.length <= 2 && candidate.all(Char::isDigit)) {
                val numericValue = candidate.toIntOrNull()
                if (numericValue == null || numericValue <= max) onValueChange(candidate)
            }
        },
        modifier = Modifier.weight(1f),
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

private fun twoDigits(value: Long) = value.toString().padStart(2, '0')
