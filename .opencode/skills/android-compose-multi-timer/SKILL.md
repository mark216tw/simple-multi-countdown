---
name: android-compose-multi-timer
description: Use when building or extending an Android Jetpack Compose countdown app with concurrent timers, foreground-service notifications, persisted settings and themes, adaptive icons, documentation, or a GitHub Debug Pre-release.
compatibility: Android Studio projects using Kotlin, Gradle, Jetpack Compose, and GitHub CLI.
metadata:
  platform: android
  language: kotlin
  ui: jetpack-compose
---

# Android Compose Multi-Timer

Build and release a reliable Android countdown app that can run multiple timers concurrently. Treat timer state, background execution, settings migration, visual assets, documentation, and release metadata as one end-to-end system.

## Use This Skill For

- Converting a single countdown into concurrent timers.
- Implementing pause, resume, add-time, reset, stop, restart, and alarm dismissal per timer.
- Persisting timers across process death and device reboot.
- Running timers through a foreground service with notification actions.
- Adding Compose settings, theme previews, dark mode, and system navigation-bar styling.
- Creating Android adaptive, monochrome, and legacy launcher icons.
- Updating project documentation and publishing a Debug APK as a GitHub Pre-release.

Do not use this workflow unchanged for exact alarms, calendar alarms, or safety-critical timing. Those may require `AlarmManager`, exact-alarm permissions, or a different scheduling model.

## Start With Repository Discovery

Inspect the project before editing. Locate:

- `app/build.gradle.kts` for namespace, application ID, SDK levels, and version.
- `AndroidManifest.xml` for service, receiver, notification, and wake-lock permissions.
- Timer models, repository, persistent store, foreground service, and boot receiver.
- ViewModel and top-level Compose state.
- Home, timer, and settings screens.
- Theme and launcher-icon resources.
- Unit tests, instrumentation tests, README, architecture, testing, release, privacy, and changelog documents.
- Existing Git remote, tags, releases, and signing configuration.

Search for assumptions that enforce one timer: singular fields such as `timer`, `activeTimer`, one SharedPreferences record, commands without an ID, fixed notification request codes, and routes without a timer identifier.

## Define Product Semantics First

Confirm or derive these rules before implementation:

- Whether each start creates a new timer or replaces one.
- Which operations affect one timer versus all timers.
- Alarm duration options and the representation of silent or unlimited ringing.
- Behavior when multiple timers finish together.
- Which timer a consolidated notification displays and controls.
- Whether completed timers remain available for restart.
- Reboot behavior for running, paused, completed, and ringing timers.
- Whether appearance changes preview immediately and what Cancel means.
- Whether the APK is a Debug test artifact or a formally signed release.

Encode these rules in tests and documentation, not only UI text.

## Multi-Timer Data Model

Give every runtime timer a stable unique ID. Keep preset IDs and runtime timer IDs conceptually separate because starting the same preset twice must create two independent timers.

Recommended snapshot shape:

```kotlin
data class TimerSnapshot(
    val id: String,
    val active: Boolean,
    val paused: Boolean,
    val name: String,
    val originalDurationSeconds: Long,
    val remainingSeconds: Long,
    val alarmRinging: Boolean,
    // Appearance and per-timer settings captured at start time.
)
```

Expose a list in UI state:

```kotlin
data class AppUiState(
    val timers: List<TimerSnapshot> = emptyList(),
    // Presets, settings, initialization state...
)
```

All commands must carry `timerId`:

```kotlin
pause(timerId)
resume(timerId)
addTime(timerId, seconds)
reset(timerId)
stop(timerId)
dismissAlarm(timerId)
```

Never infer the target from list position, name, preset ID, or whichever timer is currently displayed in a notification.

## Reliable Timekeeping

For each timer record, persist enough data to calculate remaining time without relying on a continuously running coroutine:

- `endAtElapsedMillis`
- `wallClockEndAtMillis`
- boot count or an equivalent boot identity
- paused remaining seconds
- original duration
- active, paused, and ringing flags
- alarm start wall-clock time and alarm duration

During the same boot, use `SystemClock.elapsedRealtime()` so manual wall-clock changes do not alter the countdown. Also persist a wall-clock end time so the timer can be reconstructed after reboot.

Round positive remaining milliseconds upward when presenting seconds:

```kotlin
((remainingMillis + 999) / 1000).coerceAtLeast(0)
```

On reboot:

1. Dismiss stale ringing state rather than replaying an old alarm unexpectedly.
2. Calculate remaining time from the persisted wall-clock end.
3. Rebase active timers onto the new elapsed-realtime clock.
4. Start the foreground service only if at least one timer is active.

## Persistent Store And Migration

SharedPreferences is appropriate when notification actions or receivers require synchronous durable writes before the process exits. Store the timer collection as a versioned JSON array or use a database when queries and schema complexity justify it.

Use synchronized read-modify-write operations. Update records by ID and preserve unrelated records.

When replacing a shipped single-timer schema:

1. Check whether the new collection key exists.
2. If not, decode the old scalar fields.
3. Convert meaningful old state into one record with a generated ID.
4. Write the new collection synchronously.
5. Leave migration code only when users can actually have old persisted data.

Do not silently normalize a removed setting into an unintended value. For example, if a removed 10-minute ringing option is read, define the fallback explicitly in the app-settings decoder rather than merely clamping it inside runtime state.

## Foreground Service

Use one foreground service to supervise all timers rather than one service instance per timer.

The service ticker should:

1. Read all records.
2. Complete every active, unpaused timer whose remaining time reached zero.
3. Dismiss every ringing timer whose configured timeout elapsed.
4. Detect second changes for tick sounds and notification refreshes.
5. Re-read after mutations before rendering service state.
6. Stop itself only when no timer is active or ringing.

A 250-500 ms handler interval is sufficient for a seconds-resolution UI. Do not persist every tick; derive remaining time from end timestamps.

### Consolidated Notification

Android requires one foreground notification. If several timers run:

- Show the total active timer count.
- Select a deterministic primary timer, usually the one nearest completion.
- Attach that primary timer's ID to every action intent.
- Use unique and stable PendingIntent request codes or `Intent` data when concurrent actions could otherwise overwrite extras.
- Rebuild the notification when the primary timer changes.

Alarm notification actions must also include the timer ID. Never let “stop” or “dismiss” remove every timer unless the UI explicitly says so.

### Audio And Power

- Capture sound, tick, screen-on, and alarm-duration settings when a timer starts if running timers should not change retroactively.
- Use alarm audio attributes for completion sound.
- Manage audio focus and release `MediaPlayer` safely.
- Keep at most one looping alarm output even when several records are ringing, while retaining each record's independent ringing state.
- Use a non-reference-counted partial wake lock with a bounded timeout and renewal.
- Release sound, audio focus, callbacks, and wake locks in every stop path and `onDestroy()`.

## Settings Semantics

Prefer one source of truth for each behavior. If “silent” is an alarm-duration choice, model it as a duration sentinel rather than retaining a separate completion-sound switch.

Example:

```kotlin
val alarmDurationOptions = listOf(0L, 10L, 30L, 60L, 300L, -1L)

val soundEnabled: Boolean
    get() = alarmDurationSeconds != 0L
```

Document sentinels:

- `0`: silent; show completion notification without ringing.
- Positive value: ring for that many seconds.
- `-1`: do not stop automatically.

When removing an old sound switch, migrate `soundEnabled = false` to the silent sentinel so an upgrade cannot unexpectedly enable sound.

Validate settings at persistence boundaries and safely decode unknown enum names or removed values.

## Compose State And Navigation

Keep timer truth in the repository/store and expose refreshed snapshots through the ViewModel. A periodic UI refresh may update derived remaining seconds, but it must not own timer completion.

Navigate to a timer by ID:

```text
timer/{timerId}
```

The timer screen looks up the current snapshot from the list and sends commands with the route ID. The home screen renders active and ringing timers in a bounded, scrollable list so many timers do not push presets off-screen.

## Immediate Theme Preview

Persist appearance settings in DataStore, but keep a temporary preview above persisted state while the settings dialog is open.

```kotlin
data class AppearancePreview(
    val themeColor: AppThemeColor,
    val darkMode: Boolean,
)

val displayedTheme = preview ?: persistedAppearance
```

Required behavior:

- Selecting a theme color immediately updates the entire app.
- Toggling dark mode immediately updates the entire app.
- The system navigation bar updates with the preview.
- Cancel clears the preview and restores persisted settings.
- Save persists the complete settings object; the preview can clear once persisted state matches it.

Do not write DataStore on every preview click unless that is explicitly the product behavior.

For fixed selectable themes, disable dynamic color or clearly define its priority; otherwise Android 12 dynamic color can make custom choices appear ineffective.

## System Navigation Bar

With edge-to-edge enabled, synchronize the navigation bar after MaterialTheme is applied:

- Use the theme surface color for the navigation-bar background.
- Use dark navigation icons in light mode and light icons in dark mode.
- Disable enforced navigation-bar contrast on Android 10+ only when the selected surface gives sufficient contrast.
- Update inside `SideEffect` so immediate theme previews are reflected.

Test gesture and three-button navigation on light and dark themes.

## Adaptive Launcher Icon

Use separate background and foreground layers:

- Full adaptive canvas: `108 x 108 dp`.
- Centered crop/reference area: `72 x 72 dp`, coordinates `18..90`.
- Core safe area: `66 x 66 dp`, coordinates `21..87`.
- If artwork should use 80% of the core safe area, render it as `52.8 x 52.8 dp`, coordinates `27.6..80.4`.

Build the foreground vector initially inside the 66 dp safe zone, then scale around `(54,54)`:

```xml
<group
    android:pivotX="54"
    android:pivotY="54"
    android:scaleX="0.8"
    android:scaleY="0.8">
    <!-- foreground paths -->
</group>
```

Provide:

- `mipmap-anydpi-v26` adaptive icon with background and foreground.
- `mipmap-anydpi-v33` adaptive icon with a monochrome layer.
- A legacy vector fallback for API 24-25.
- `android:roundIcon` in the manifest.

Keep the icon style coherent across layers. A reusable visual system can use a saturated solid background, cream primary object, dark 4 dp cartoon outline, and one or two bright accents. The monochrome asset should preserve silhouette and critical internal details without depending on color.

Test circular, rounded-square, squircle, and teardrop masks, plus Android 13 themed icons.

## Verification

Run the narrowest useful checks while editing, then the complete Debug gate:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

Inspect:

- `app/build/outputs/apk/debug/output-metadata.json`
- `app/build/reports/lint-results-debug.html`
- `app/build/reports/tests/testDebugUnitTest/index.html`

Verify application ID, version code, version name, variant, and minimum SDK in output metadata.

Calculate a release-asset digest:

```powershell
Get-FileHash -Algorithm SHA256 -LiteralPath "app\build\outputs\apk\debug\app-debug.apk"
```

Manual acceptance should cover:

- Three or more concurrent timers.
- Per-ID operations that do not affect other timers.
- Multiple timers completing close together.
- Every alarm-duration option, including silent and unlimited.
- Background, lock-screen, recent-task removal, and reboot behavior.
- Notification permission allowed and denied.
- Immediate theme preview, Cancel rollback, Save persistence, and navigation-bar sync.
- Launcher masks and themed icon.
- Portrait, landscape, narrow screens, and large font scale.

## Documentation Update

Keep these documents aligned with behavior:

- README: identity, package ID, features, latest Debug download, and Debug-signing warning.
- User guide: actual labels, option order, timer interactions, themes, background behavior.
- Architecture: timer IDs, persistence schema, migration, clock source, service, theme preview, icon dimensions.
- Testing: automated commands and manual regression matrix.
- Release guide: versioning, signing, Debug Pre-release rules, formal-release rules.
- Privacy: every locally stored field and Android permission.
- Changelog: move already released work out of Unreleased; never rewrite an old release with new work.

Search all Markdown and resources for old app names, package IDs, removed labels, removed duration options, stale download URLs, and outdated version numbers.

## Git And GitHub Debug Pre-Release

Before committing:

1. Inspect `git status`, `git diff`, and recent log.
2. Run tests, lint, and APK assembly.
3. Scan for secrets and signing files.
4. Confirm build outputs and old APKs are ignored.
5. Stage only intended source, resources, tests, and documentation.
6. Run `git diff --cached --check` and inspect the staged stat.

Version every new APK release. Increment `versionCode`; use a new semantic `versionName`. Do not attach a changed APK to an old release tag.

For a Debug artifact:

- Tag format: `vX.Y.Z-debug`.
- Release title includes `Debug`.
- Set `--prerelease`.
- State that it is a Debug version for testing, not a formal release.
- State that it uses Android Debug signing.
- Include application ID, version code, minimum Android version, and SHA-256.
- Label the asset as a Debug version.

Example:

```powershell
$notes = "Debug version for testing only. Not a formal release."
gh release create "v1.2.0-debug" `
  --repo "OWNER/REPOSITORY" `
  --target "main" `
  --prerelease `
  --title "APP_NAME v1.2.0 Debug" `
  --notes $notes

gh release upload "v1.2.0-debug" `
  "app\build\outputs\apk\debug\app-debug.apk#APP_NAME-v1.2.0-debug.apk - Debug version" `
  --repo "OWNER/REPOSITORY"
```

After upload, verify rather than assuming success:

```powershell
gh release view "v1.2.0-debug" `
  --repo "OWNER/REPOSITORY" `
  --json url,isDraft,isPrerelease,tagName,targetCommitish,name,body,assets
```

Confirm:

- `isPrerelease` is true.
- The tag points to the intended commit.
- The asset state is uploaded.
- The asset digest matches the local SHA-256.
- The working tree is clean and `main` tracks `origin/main` without being ahead.

## Common Failure Modes

- Starting a timer overwrites the existing record: replace singular storage with an ID-keyed collection.
- Notification action controls the wrong timer: include the ID and avoid colliding PendingIntents.
- Several completions stop the service while another timer runs: stop only when no record is active or ringing.
- Settings preview persists after Cancel: keep preview state separate from DataStore state.
- Custom theme does not appear on Android 12: dynamic color is overriding it.
- Silent selection still rings: use one source of truth and check the sentinel during completion.
- Removed setting reappears after upgrade: migrate legacy preferences explicitly.
- Icon looks cropped: include stroke width in bounds and test launcher masks.
- New APK reuses an old version or release tag: increment version code/name and create a new tag.
- Debug APK appears to be formal: mark title, notes, tag, asset label, and GitHub release as Pre-release.

## Completion Criteria

The task is complete only when:

- Product behavior is implemented end to end.
- Persistence and migration match shipped-data requirements.
- Background service and notification actions are ID-safe.
- UI and system chrome reflect settings correctly.
- Icons satisfy adaptive and themed-icon requirements.
- Unit tests, lint, and Debug build pass.
- Documentation matches the current UI and release.
- If publication was requested, GitHub contains the intended commit and verified Debug Pre-release asset.
