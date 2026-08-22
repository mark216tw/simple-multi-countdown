# 測試指南

## 自動測試

執行 JVM 單元測試：

```powershell
.\gradlew.bat testDebugUnitTest
```

執行 Android lint：

```powershell
.\gradlew.bat lintDebug
```

編譯 App 與 AndroidTest APK：

```powershell
.\gradlew.bat assembleDebug assembleDebugAndroidTest
```

連接實機或啟動模擬器後執行 instrumented tests：

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

報告位置：

- 單元測試：`app/build/reports/tests/testDebugUnitTest/index.html`
- Lint：`app/build/reports/lint-results-debug.html`
- AndroidTest：`app/build/reports/androidTests/connected/debug/index.html`

## 手動功能檢查

### 預設與設定

- 新增、編輯、複製與刪除預設。
- 清空預設清單後重新啟動 App，確認不會自動補回預設。
- 拖曳卡片時確認卡片與手指位置同步，放開後重新啟動 App，確認順序保存。
- 驗證 12 種顏色、名稱長度與時間範圍。
- 修改每項設定後重新啟動 App，確認設定保存。
- 確認 App 顯示名稱為「簡單多倒數」，安裝 Package ID 為 `com.example.simplecountdown3`。
- 確認鈴響時間只有 10 秒、30 秒、1 分鐘、5 分鐘、10 分鐘及永不自動停止。

### 倒數

- 測試執行、暫停、繼續、加 1/5 分鐘、重設與停止。
- 測試 `00:00:01`、跨小時與 `99:59:59`。
- 連續啟動至少三個不同時間的倒數，確認全部同時遞減且不互相取代。
- 分別暫停、繼續、加時、重設及停止其中一筆，確認其他倒數不受影響。
- 啟動超過首頁活動區塊高度的倒數數量，確認清單可捲動且預設庫仍可操作。
- 同時完成兩筆倒數，確認鈴響可停止，其他活動倒數仍繼續。
- 測試直向、橫向、窄螢幕與大字級。

### 背景與通知

- Android 13 以上允許及拒絕通知權限。
- 多筆倒數運行時，確認通知顯示總數及最接近完成的一筆。
- 從通知暫停、繼續、加時、停止及停止鬧鈴，確認只影響通知所顯示的倒數。
- 鎖屏、返回桌面、從最近使用的 App 移除後確認所有倒數仍繼續。
- 強制停止 App 後確認系統行為符合 Android 限制。
- 重新開機後確認所有活動倒數恢復，重新開機前的鈴響不會自動重播。

### 聲音與電源

- 測試完成提示音開啟與關閉。
- 分別測試 10 秒、30 秒、1 分鐘、5 分鐘、10 分鐘及「永不自動停止」。
- 測試答答聲、媒體音量、鬧鐘音量、藍牙及勿擾模式。
- 測試保持常亮只在倒數畫面生效。

## 建議裝置矩陣

- Android 7 或 8：最低版本與背景服務基本行為。
- Android 12：動態色彩。
- Android 13：通知執行期權限與 themed icon。
- Android 14、15：前景服務政策與背景限制。
- 至少一台具有積極省電策略的實體裝置。
