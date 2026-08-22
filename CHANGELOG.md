# 版本紀錄

本文件依照 [Keep a Changelog](https://keepachangelog.com/zh-TW/1.1.0/) 格式維護，版本號遵循 [Semantic Versioning](https://semver.org/lang/zh-TW/)。

## [Unreleased]

### Changed

- App 名稱改為「簡單多倒數」，Package ID 改為 `com.example.simplecountdown3`。
- 鈴響時間移除 15、20、25 分鐘，新增 10 秒與 30 秒。
- 改善預設卡片拖曳時的手指同步與換位動畫。
- 放大倒數畫面的時間字型。
- 將設定對話框標題簡化為「設定」。
- 強化鈴響時間已選選項的視覺對比。

### Added

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

[Unreleased]: https://github.com/mark216tw/simple-multi-countdown/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/mark216tw/simple-multi-countdown/releases/tag/v1.0.0
