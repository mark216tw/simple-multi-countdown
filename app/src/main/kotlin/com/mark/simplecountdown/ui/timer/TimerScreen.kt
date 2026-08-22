package com.mark.simplecountdown.ui.timer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mark.simplecountdown.model.TimerSnapshot
import com.mark.simplecountdown.util.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    timer: TimerSnapshot,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onAddTime: (Long) -> Unit,
    onReset: () -> Unit,
    onStop: () -> Unit,
    onDismissAlarm: () -> Unit,
    onRestart: () -> Unit,
) {
    var confirmReset by remember { mutableStateOf(false) }
    var confirmStop by remember { mutableStateOf(false) }
    val view = LocalView.current

    DisposableEffect(timer.active, timer.keepScreenOn, view) {
        val previous = view.keepScreenOn
        view.keepScreenOn = timer.active && timer.keepScreenOn
        onDispose { view.keepScreenOn = previous }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(timer.name.ifBlank { "倒數計時" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (timer.active) {
                        IconButton(onClick = { confirmStop = true }) {
                            Icon(Icons.Outlined.StopCircle, contentDescription = "停止倒數")
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { contentPadding ->
        if (timer.active) {
            ActiveTimerBody(
                timer = timer,
                onPause = onPause,
                onResume = onResume,
                onAddTime = onAddTime,
                onReset = { confirmReset = true },
                modifier = Modifier.padding(contentPadding),
            )
        } else {
            FinishedTimerBody(
                timer = timer,
                onDismissAlarm = onDismissAlarm,
                onRestart = onRestart,
                onBack = onBack,
                modifier = Modifier.padding(contentPadding),
            )
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("重新開始倒數？") },
            text = { Text("目前進度將被重設。") },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        confirmReset = false
                        onReset()
                    },
                ) { Text("重新開始") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("取消") }
            },
        )
    }
    if (confirmStop) {
        AlertDialog(
            onDismissRequest = { confirmStop = false },
            title = { Text("停止倒數？") },
            text = { Text("停止後將無法恢復目前進度。") },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        confirmStop = false
                        onStop()
                        onBack()
                    },
                ) { Text("停止") }
            },
            dismissButton = {
                TextButton(onClick = { confirmStop = false }) { Text("繼續倒數") }
            },
        )
    }
}

@Composable
private fun ActiveTimerBody(
    timer: TimerSnapshot,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onAddTime: (Long) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 20.dp),
    ) {
        val landscape = maxWidth > maxHeight
        val ring: @Composable (Modifier) -> Unit = { ringModifier ->
            Box(ringModifier, contentAlignment = Alignment.Center) {
                CountdownRing(timer = timer, maxSize = if (landscape) 340.dp else 390.dp)
            }
        }
        val controls: @Composable (Modifier) -> Unit = { controlsModifier ->
            TimerControls(
                paused = timer.paused,
                onPause = onPause,
                onResume = onResume,
                onAddTime = onAddTime,
                onReset = onReset,
                modifier = controlsModifier,
            )
        }

        if (landscape) {
            Row(Modifier.fillMaxSize()) {
                ring(Modifier.weight(3f).fillMaxSize())
                controls(Modifier.weight(2f).fillMaxSize())
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                ring(Modifier.weight(5f).fillMaxSize())
                controls(Modifier.weight(3f).fillMaxSize())
            }
        }
    }
}

@Composable
private fun CountdownRing(timer: TimerSnapshot, maxSize: androidx.compose.ui.unit.Dp) {
    val progress = if (timer.originalDurationSeconds == 0L) {
        0f
    } else {
        (timer.remainingSeconds.toFloat() / timer.originalDurationSeconds).coerceIn(0f, 1f)
    }
    val timerColor = Color(timer.colorValue)
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest

    BoxWithConstraints(contentAlignment = Alignment.Center) {
        val size = minOf(maxWidth, maxHeight, maxSize)
        val timeFontSize = when {
            size < 250.dp -> 46.sp
            size < 310.dp -> 60.sp
            timer.remainingSeconds >= 3600 -> 72.sp
            else -> 90.sp
        }
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val strokeWidth = 18.dp.toPx()
                drawCircle(
                    color = trackColor,
                    style = Stroke(width = strokeWidth),
                    radius = (this.size.minDimension - strokeWidth) / 2,
                )
                drawArc(
                    color = timerColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (timer.paused) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(99.dp),
                    ) {
                        Text("已暫停", modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
                    }
                    Spacer(Modifier.size(10.dp))
                }
                Text(
                    formatDuration(timer.remainingSeconds),
                    fontSize = timeFontSize,
                    lineHeight = 96.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp,
                    maxLines = 1,
                )
                Text(
                    "剩餘時間",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TimerControls(
    paused: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onAddTime: (Long) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilledTonalButton(onClick = { onAddTime(60) }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("1 分鐘")
            }
            FilledTonalButton(onClick = { onAddTime(5 * 60) }) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("5 分鐘")
            }
        }
        Spacer(Modifier.size(22.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedIconButton(onClick = onReset, modifier = Modifier.size(64.dp)) {
                Icon(Icons.Outlined.Replay, contentDescription = "重新開始", modifier = Modifier.size(30.dp))
            }
            Spacer(Modifier.width(20.dp))
            LargeFloatingActionButton(onClick = if (paused) onResume else onPause) {
                Icon(
                    if (paused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                    contentDescription = if (paused) "繼續" else "暫停",
                    modifier = Modifier.size(42.dp),
                )
            }
        }
    }
}

@Composable
private fun FinishedTimerBody(
    timer: TimerSnapshot,
    onDismissAlarm: () -> Unit,
    onRestart: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Outlined.NotificationsActive,
            contentDescription = null,
            modifier = Modifier.size(88.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.size(24.dp))
        Text(
            "時間到",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (timer.name.isNotBlank()) {
            Spacer(Modifier.size(8.dp))
            Text(timer.name)
        }
        Spacer(Modifier.size(28.dp))
        if (timer.alarmRinging) {
            FilledTonalButton(onClick = onDismissAlarm) {
                Icon(Icons.Outlined.NotificationsOff, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("停止鬧鈴")
            }
            Spacer(Modifier.size(10.dp))
        }
        if (timer.originalDurationSeconds > 0) {
            FilledTonalButton(onClick = onRestart) {
                Icon(Icons.Outlined.Replay, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("再來一次")
            }
        }
        Spacer(Modifier.size(8.dp))
        TextButton(onClick = onBack) { Text("返回預設庫") }
    }
}
