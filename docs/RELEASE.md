# 發布流程

## 目前 APK 狀態

本機執行 `assemblePrerelease` 產生的 `app/build/outputs/apk/prerelease/app-prerelease.apk` 會啟用 R8 壓縮並使用 Android Debug 金鑰簽署，只供測試與功能驗收，不適合正式發布或提交商店。

## 發布前檢查

1. 更新 `app/build.gradle.kts` 的 `versionCode` 與 `versionName`。
2. 更新 `CHANGELOG.md`。
3. 執行完整驗證：

```powershell
.\gradlew.bat clean testDebugUnitTest lintPrerelease assemblePrerelease
```

4. 在 Android 13、14、15 以上實機測試通知、前景服務、鎖屏與重新開機。
5. 驗證多個倒數可同時運行、個別操作、完成鈴響及開機恢復。
6. 檢查 merged Manifest 的 Package ID 為 `com.example.simplecountdown3`，並檢查權限。
7. 使用正式 keystore 建置 release APK 或 Android App Bundle。

## 正式簽章

正式 keystore、密碼、`keystore.properties` 及任何憑證不得提交 Git。應透過本機未追蹤檔案或 CI secrets 傳入簽章設定。

正式發布後必須永久保管相同簽章；遺失簽章可能導致無法更新既有安裝。

## 建立 GitHub Release

### Prerelease 測試版

測試 APK 使用 `prerelease` Build Type、R8 壓縮及 Android Debug 簽章，GitHub Release 必須設定為 Pre-release，並在標題與說明清楚標示測試用途：

```powershell
gh release upload v1.3.0-debug app-prerelease.apk --clobber
```

Prerelease APK 不得標示為正式版本，也不適合提交應用程式商店。

### 正式版本

正式 APK 通過驗證後，可建立 tag 與 GitHub Release：

```powershell
git tag v1.0.0
git push origin v1.0.0
gh release create v1.0.0 path\to\signed-release.apk --title "簡單多重倒數 v1.0.0" --notes-file CHANGELOG.md
```

Release 附件應使用清楚名稱，例如 `simple-multi-countdown-v1.0.0.apk`，並在說明中標示最低 Android 版本、Package ID、簽章類型與重要變更。
