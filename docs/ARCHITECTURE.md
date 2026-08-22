# 架構說明

## 技術組成

- 單一 Android `app` module。
- Kotlin、Jetpack Compose、Material 3。
- ViewModel、StateFlow 與 Navigation Compose。
- Preferences DataStore 保存 UI 資料。
- SharedPreferences 保存需要同步寫入的背景計時狀態。
- Foreground Service 維持倒數、通知、聲音與 wake lock。

## 主要目錄

```text
app/src/main/kotlin/com/mark/simplecountdown/
├─ MainActivity.kt
├─ AppViewModel.kt
├─ data/                 預設及設定持久化
├─ model/                UI 與計時資料模型
├─ timer/                計時狀態、Service、通知與開機 Receiver
├─ ui/                   Compose 畫面與元件
└─ util/                 時間格式化
```

應用程式的安裝識別碼由 Gradle `applicationId` 設為 `com.example.simplecountdown3`；Kotlin source namespace 維持 `com.mark.simplecountdown`。兩者用途不同，不影響安裝識別碼。

## UI 狀態流

`AppDataRepository` 將 DataStore 內容轉換為 `Flow<StoredAppData>`。`AppViewModel` 合併持久化資料與目前的 `TimerSnapshot` 清單，對 Compose 暴露單一 `StateFlow<AppUiState>`。

Compose 使用 `collectAsStateWithLifecycle()` 收集狀態。所有預設 CRUD、設定與計時命令都經由 `AppViewModel`，畫面不保存另一套計時真相。

## 持久化

### DataStore

檔名：`app_data_v1`

保存內容：

- 排序後的預設清單。
- 上一次自訂倒數。
- 鬧鈴時間、聲音、常亮與深色模式設定。

### 原生計時狀態

SharedPreferences 檔名：`native_timer_state_v1`，其中 `timer_records_v2` 以 JSON 清單保存多筆計時器。每筆記錄具有獨立 UUID，所有暫停、繼續、加時、重設、停止及停止鬧鈴命令都以此 ID 定位。

計時狀態使用同步 `commit()`。通知 action、Service 或 BroadcastReceiver 執行後可能立即結束程序，因此狀態必須在返回前完成持久化。

若偵測到舊版單一計時欄位且尚未建立 `timer_records_v2`，首次讀取會將舊狀態轉換成一筆帶 ID 的記錄。

## 計時來源

同一次開機期間以 `SystemClock.elapsedRealtime()` 計算剩餘時間，避免使用者調整系統時間影響倒數。同時保存 wall-clock 結束時間與 boot count，以便重新開機後重建 monotonic clock 基準。

每筆計時器各自遵循以下狀態，彼此可同時倒數：

```text
Inactive -> Running <-> Paused -> Ringing -> Completed
```

若關閉完成提示音，`Running` 會直接進入 `Completed`。

## 前景服務

`TimerForegroundService` 負責：

- 更新所有活動倒數，通知顯示目前最接近完成的一筆與總數。
- 每 250 毫秒檢查所有倒數，逐筆完成計時及處理鈴響逾時。
- 處理帶有計時器 ID 的通知暫停、繼續、加時、停止及停止鬧鈴 action。
- 播放答答聲及完成鬧鐘聲。
- 管理 audio focus。
- 使用具有逾時及定時續期的 partial wake lock。
- 在倒數完成後更新完成通知。

`BootReceiver` 接收 `BOOT_COMPLETED`，重新計算剩餘時間並恢復活動中的服務。

## 產品規格

- App 顯示名稱：簡單多倒數。
- Package ID：`com.example.simplecountdown3`。
- 多個倒數可同時執行，並可逐筆操作。
- 鈴響時間選項：10 秒、30 秒、1 分鐘、5 分鐘、10 分鐘及永不自動停止。
- 倒數時間範圍：1 秒至 `99:59:59`。
- 支援 Android 7.0（API 24）以上。

## Android 權限

- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_SPECIAL_USE`
- `POST_NOTIFICATIONS`
- `WAKE_LOCK`
- `RECEIVE_BOOT_COMPLETED`

專案沒有宣告網路、位置、儲存空間、麥克風或聯絡人權限。
