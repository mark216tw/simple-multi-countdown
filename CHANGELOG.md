# 版本紀錄

本文件依照 [Keep a Changelog](https://keepachangelog.com/zh-TW/1.1.0/) 格式維護，版本號遵循 [Semantic Versioning](https://semver.org/lang/zh-TW/)。

## [Unreleased]

## [1.3.0] - 2026-08-28

### Changed

- 首頁上方的活動倒數卡片改用各計時器識別色，並自動使用高對比文字與圖示。
- 鈴響時間的已選項使用實心主色、高對比粗體文字及勾選圖示。
- 倒數識別色色盤由 12 色增加至 20 色，並改善黑／白前景的對比判斷。
- GitHub 測試發行 APK 改用 `prerelease` Build Type，啟用 R8 壓縮並使用 Android Debug 金鑰簽署。

### Added

- 新增可重用的 OpenCode Android Compose 多重倒數開發與 Debug Pre-release skill。

## [1.2.0] - 2026-08-22

### Changed

- App 名稱改為「簡單多重倒數」。
- App 圖示改為暖橘背景的粗線條沙漏，並提供 adaptive、monochrome 與 legacy 三種資源。
- 沙漏圖案使用 `66 × 66 dp` 安全區的 80%，實際尺寸為 `52.8 × 52.8 dp`。

## [1.1.0] - 2026-08-22

### Changed

- 系統底部導覽列會跟隨 App 主題顯示對應背景及明暗圖示。
- 首頁標題下方新增多倒數功能說明。
- 「倒數完成鈴響時間」新增無聲選項，並移除獨立的完成提示音開關。
- 設定開關調整為深色模式、倒數畫面保持常亮、倒數進行時播放答答聲的順序。
- 主題色及深色模式改為點選後立即預覽，取消時恢復原設定。
- Package ID 改為 `com.example.simplecountdown3`。
- 鈴響時間移除 10、15、20、25 分鐘，新增 10 秒與 30 秒，並將無限鈴響文案改為「不自動停止」。
- 改善預設卡片拖曳時的手指同步與換位動畫。
- 放大倒數畫面的時間字型。
- 將設定對話框標題簡化為「設定」。
- 強化鈴響時間已選選項的視覺對比。

### Added

- 新增珊瑚紅、海洋藍綠、森林綠、紫羅蘭、琥珀金及天空藍 6 組淺色／深色主題。
- 支援多個倒數同時運行、個別控制、背景持續計時與開機恢復。
- 首頁新增可捲動的活動倒數清單。
- 新增 4 種預設識別色，色盤增加至 12 色。

## [1.0.0] - 2026-08-21

### Added

- 使用 Kotlin、Jetpack Compose 與 Material 3 建立原生 Android App。
- 預設倒數的新增、編輯、複製、刪除與排序。
- 自訂倒數、圓形進度、暫停、繼續、加時、重設與停止。
- 前景服務、通知快速操作、完成鬧鈴及開機恢復。
- 完成提示音、答答聲、常亮、深色模式與動態色彩設定。
- DataStore 持久化、單元測試、Compose 啟動測試與 Android lint。

[Unreleased]: https://github.com/mark216tw/simple-multi-countdown/compare/v1.3.0-debug...HEAD
[1.3.0]: https://github.com/mark216tw/simple-multi-countdown/releases/tag/v1.3.0-debug
[1.2.0]: https://github.com/mark216tw/simple-multi-countdown/releases/tag/v1.2.0-debug
[1.1.0]: https://github.com/mark216tw/simple-multi-countdown/releases/tag/v1.1.0-debug
[1.0.0]: https://github.com/mark216tw/simple-multi-countdown/releases/tag/v1.0.0-debug
