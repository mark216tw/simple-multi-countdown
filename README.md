# 簡單多重倒數

以 Kotlin、Jetpack Compose 和 Material 3 實作，可同時運行多個倒數的 Android 計時器。

- App 名稱：簡單多重倒數
- Package ID：`com.example.simplecountdown3`
- 支援版本：Android 7.0（API 24）以上

## 功能

- 建立、編輯、複製、刪除與拖曳排序倒數預設
- 20 種預設識別色與自訂倒數
- 同時啟動多個倒數，首頁可逐筆查看及進入控制畫面
- 暫停、繼續、加 1/5 分鐘、重設與停止
- 前景服務通知及針對個別倒數的通知快速操作
- 完成鬧鈴、答答聲，以及無聲、10 秒、30 秒、1/5 分鐘或不自動停止的鈴響設定
- 重新開機後恢復所有進行中的倒數
- 6 組可即時預覽的主題色、深色模式、橫向排版與保持螢幕常亮
- 系統底部導覽列會隨 App 主題切換淺色或深色外觀

## 安裝 APK

最新測試發行版可從 [GitHub Releases](https://github.com/mark216tw/simple-multi-countdown/releases/tag/v1.3.0-debug) 下載。此版本標示為 Pre-release，不是正式發布版本。

GitHub 提供的 APK 使用 `prerelease` Build Type、啟用 R8 壓縮，並以 Android Debug 金鑰簽署，只適合測試，不應當作正式商店發布版本。

## 技術

- Kotlin 2.2.20、Java 17
- Jetpack Compose、Material 3、Navigation Compose
- ViewModel、StateFlow、Preferences DataStore
- Android foreground service、SharedPreferences timer state
- minSdk 24、targetSdk 35、compileSdk 36

## 建置

需要 Java 17 與 Android SDK 36。

```powershell
.\gradlew.bat assemblePrerelease
```

本機建置產生的 APK 位於 `app/build/outputs/apk/prerelease/app-prerelease.apk`。

## 驗證

```powershell
.\gradlew.bat testDebugUnitTest lintPrerelease assemblePrerelease
```

通知、鬧鈴、鎖屏與重新開機行為仍應在 Android 13 以上實機進行最終驗收。

## 文件

- [使用指南](docs/USER_GUIDE.md)
- [架構說明](docs/ARCHITECTURE.md)
- [測試指南](docs/TESTING.md)
- [發布流程](docs/RELEASE.md)
- [隱私權說明](PRIVACY.md)
- [版本紀錄](CHANGELOG.md)
- [OpenCode Android 多重倒數 Skill](.opencode/skills/android-compose-multi-timer/SKILL.md)

## 隱私與授權

App 不包含網路、帳號、分析或廣告功能，資料只保存在使用者裝置內。詳細內容請參閱 [PRIVACY.md](PRIVACY.md)。

本專案採用 [MIT License](LICENSE)。
