package com.mark.simplecountdown.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mark.simplecountdown.model.AppUiState
import com.mark.simplecountdown.model.AppThemeColor
import com.mark.simplecountdown.model.TimerPreset
import com.mark.simplecountdown.model.TimerSettings
import com.mark.simplecountdown.ui.components.PresetEditorDialog
import com.mark.simplecountdown.ui.components.SettingsDialog
import com.mark.simplecountdown.util.formatCompactDuration
import com.mark.simplecountdown.util.formatDuration

private data class EditorRequest(val preset: TimerPreset?, val customTimer: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: AppUiState,
    snackbarHostState: SnackbarHostState,
    onAddPreset: (TimerPreset) -> Unit,
    onUpdatePreset: (TimerPreset) -> Unit,
    onDuplicatePreset: (TimerPreset) -> Unit,
    onDeletePreset: (String) -> Unit,
    onSavePresetOrder: (List<TimerPreset>) -> Unit,
    onSaveCustomTimer: (TimerPreset) -> Unit,
    onSaveSettings: (TimerSettings) -> Unit,
    onPreviewAppearance: (AppThemeColor, Boolean) -> Unit,
    onCancelAppearancePreview: () -> Unit,
    onStartTimer: (TimerPreset) -> Unit,
    onOpenTimer: (String) -> Unit,
    onDismissAlarm: (String) -> Unit,
) {
    var editorRequest by remember { mutableStateOf<EditorRequest?>(null) }
    var showSettings by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<TimerPreset?>(null) }
    var displayedPresets by remember { mutableStateOf(uiState.presets) }
    var dragging by remember { mutableStateOf(false) }
    var draggedPresetId by remember { mutableStateOf<String?>(null) }
    var draggedOffsetY by remember { mutableFloatStateOf(0f) }
    val itemHeights = remember { mutableStateMapOf<String, Int>() }

    LaunchedEffect(uiState.presets) {
        if (!dragging) displayedPresets = uiState.presets
    }

    fun reorderedPresets(from: Int, to: Int): List<TimerPreset> {
        if (from !in displayedPresets.indices || to !in displayedPresets.indices) {
            return displayedPresets
        }
        return displayedPresets.toMutableList().apply { add(to, removeAt(from)) }
    }

    fun movePresetAndSave(from: Int, to: Int) {
        val updated = reorderedPresets(from, to)
        displayedPresets = updated
        onSavePresetOrder(updated)
    }

    fun startDragging(presetId: String) {
        dragging = true
        draggedPresetId = presetId
        draggedOffsetY = 0f
    }

    fun dragPreset(presetId: String, deltaY: Float) {
        val itemHeight = itemHeights[presetId]?.toFloat()?.takeIf { it > 0f } ?: return
        val updated = displayedPresets.toMutableList()
        var currentIndex = updated.indexOfFirst { it.id == presetId }
        if (currentIndex < 0) return
        var newOffset = draggedOffsetY + deltaY
        while (newOffset > itemHeight / 2 && currentIndex < updated.lastIndex) {
            updated.add(currentIndex + 1, updated.removeAt(currentIndex))
            currentIndex++
            newOffset -= itemHeight
        }
        while (newOffset < -itemHeight / 2 && currentIndex > 0) {
            updated.add(currentIndex - 1, updated.removeAt(currentIndex))
            currentIndex--
            newOffset += itemHeight
        }
        displayedPresets = updated
        draggedOffsetY = newOffset
    }

    fun finishDragging() {
        if (!dragging) return
        val updated = displayedPresets
        dragging = false
        draggedPresetId = null
        draggedOffsetY = 0f
        onSavePresetOrder(updated)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("簡單多倒數")
                        Text(
                            "您可以同時啟動多個倒數計時",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { editorRequest = EditorRequest(null, false) }) {
                        Icon(Icons.Outlined.Add, contentDescription = "新增預設")
                    }
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "設定")
                    }
                    Spacer(Modifier.width(8.dp))
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editorRequest = EditorRequest(uiState.lastCustomTimer, true)
                },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Timer, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("自訂倒數")
                }
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            val visibleTimers = uiState.timers.filter { it.alarmRinging || it.active }
            if (visibleTimers.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 260.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(visibleTimers, key = { it.id }) { timer ->
                        if (timer.alarmRinging) {
                            RingingAlarmCard(
                                name = timer.name,
                                onDismissAlarm = { onDismissAlarm(timer.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        } else {
                            ActiveTimerCard(
                                name = timer.name,
                                remainingSeconds = timer.remainingSeconds,
                                paused = timer.paused,
                                onClick = { onOpenTimer(timer.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    "我的預設",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text("長按拖曳排序", style = MaterialTheme.typography.bodySmall)
            }
            if (displayedPresets.isEmpty()) {
                EmptyPresets(
                    onAdd = { editorRequest = EditorRequest(null, false) },
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 112.dp),
                ) {
                    itemsIndexed(displayedPresets, key = { _, item -> item.id }) { index, preset ->
                        val isDragged = draggedPresetId == preset.id
                        PresetCard(
                            preset = preset,
                            index = index,
                            itemCount = displayedPresets.size,
                            modifier = (if (isDragged) Modifier else Modifier.animateItem())
                                .zIndex(if (isDragged) 1f else 0f)
                                .graphicsLayer {
                                    translationY = if (isDragged) {
                                        draggedOffsetY
                                    } else {
                                        0f
                                    }
                                    shadowElevation = if (isDragged) 12.dp.toPx() else 0f
                                }
                                .onSizeChanged { itemHeights[preset.id] = it.height },
                            isDragging = isDragged,
                            onPlay = { onStartTimer(preset) },
                            onEdit = { editorRequest = EditorRequest(preset, false) },
                            onDuplicate = { onDuplicatePreset(preset) },
                            onDelete = { deleteTarget = preset },
                            onMoveAndSave = ::movePresetAndSave,
                            onDragStart = { startDragging(preset.id) },
                            onDrag = { dragPreset(preset.id, it) },
                            onDragFinished = ::finishDragging,
                        )
                    }
                }
            }
        }
    }

    editorRequest?.let { request ->
        PresetEditorDialog(
            preset = request.preset,
            customTimer = request.customTimer,
            onDismiss = { editorRequest = null },
            onConfirm = { preset ->
                editorRequest = null
                if (request.customTimer) {
                    onSaveCustomTimer(preset)
                    onStartTimer(preset)
                } else if (request.preset == null) {
                    onAddPreset(preset)
                } else {
                    onUpdatePreset(preset)
                }
            },
        )
    }
    if (showSettings) {
        SettingsDialog(
            initialSettings = uiState.settings,
            onDismiss = {
                showSettings = false
                onCancelAppearancePreview()
            },
            onAppearancePreview = onPreviewAppearance,
            onConfirm = {
                showSettings = false
                onSaveSettings(it)
            },
        )
    }
    deleteTarget?.let { preset ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("刪除預設？") },
            text = { Text("「${preset.name}」將從預設庫移除。") },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        deleteTarget = null
                        onDeletePreset(preset.id)
                    },
                ) { Text("刪除") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun PresetCard(
    preset: TimerPreset,
    index: Int,
    itemCount: Int,
    modifier: Modifier = Modifier,
    isDragging: Boolean,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onMoveAndSave: (Int, Int) -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragFinished: () -> Unit,
) {
    val color = Color(preset.colorValue)
    val foreground = if (color.luminance() < 0.5f) Color.White else Color.Black
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = onEdit,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) 0.28f else 0.16f),
        ),
        elevation = CardDefaults.cardElevation(if (isDragging) 8.dp else 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, top = 12.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .width(8.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(color),
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    preset.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(3.dp))
                Text(formatCompactDuration(preset.durationSeconds))
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Outlined.MoreVert, contentDescription = "更多操作")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("建立副本") },
                        onClick = {
                            menuExpanded = false
                            onDuplicate()
                        },
                    )
                    if (index > 0) {
                        DropdownMenuItem(
                            text = { Text("向上移") },
                            onClick = {
                                menuExpanded = false
                                onMoveAndSave(index, index - 1)
                            },
                        )
                    }
                    if (index < itemCount - 1) {
                        DropdownMenuItem(
                            text = { Text("向下移") },
                            onClick = {
                                menuExpanded = false
                                onMoveAndSave(index, index + 1)
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("刪除") },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
            DragHandle(
                presetId = preset.id,
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragFinished = onDragFinished,
            )
            IconButton(
                onClick = onPlay,
                modifier = Modifier.semantics { contentDescription = "開始 ${preset.name}" },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = color,
                    contentColor = foreground,
                ),
            ) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = "開始倒數")
            }
        }
    }
}

@Composable
private fun DragHandle(
    presetId: String,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragFinished: () -> Unit,
) {
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragFinished by rememberUpdatedState(onDragFinished)
    Icon(
        Icons.Outlined.DragIndicator,
        contentDescription = "長按拖曳排序",
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .pointerInput(presetId) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { currentOnDragStart() },
                    onDragCancel = currentOnDragFinished,
                    onDragEnd = currentOnDragFinished,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentOnDrag(dragAmount.y)
                    },
                )
            },
    )
}

@Composable
private fun ActiveTimerCard(
    name: String,
    remainingSeconds: Long,
    paused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        ListItem(
            leadingContent = { Icon(Icons.Outlined.HourglassTop, contentDescription = null) },
            headlineContent = { Text(name) },
            supportingContent = { Text(if (paused) "已暫停" else "倒數進行中") },
            trailingContent = {
                Text(
                    formatDuration(remainingSeconds),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            colors = androidx.compose.material3.ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun RingingAlarmCard(
    name: String,
    onDismissAlarm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        ListItem(
            leadingContent = { Icon(Icons.Outlined.NotificationsActive, contentDescription = null) },
            headlineContent = { Text("時間到") },
            supportingContent = { Text(name) },
            trailingContent = {
                FilledTonalButton(onClick = onDismissAlarm) {
                    Icon(Icons.Outlined.NotificationsOff, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("停止鬧鈴")
                }
            },
            colors = androidx.compose.material3.ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun EmptyPresets(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Outlined.Timer,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline,
        )
        Spacer(Modifier.height(16.dp))
        Text("還沒有計時預設")
        Spacer(Modifier.height(12.dp))
        FilledTonalButton(onClick = onAdd) { Text("新增第一個預設") }
    }
}
