# ScreenTimeReducer

A production-grade Android app that helps users build a healthier relationship with their phone — through mindful pauses before opening distracting apps, focus modes, daily limits, gamified challenges, and clear analytics.

Built with **Kotlin**, **Jetpack Compose**, **Material 3**, **MVVM + Clean Architecture**, **Hilt**, **Room**, **DataStore**, **WorkManager**, **Glance**, and **RevenueCat**.

---

## Features

1. **Dashboard** — Today's total screen time, unlock count, hourly distribution, weekly trend, top apps.
2. **App Opening Delay** — A fullscreen mindful pause with a countdown ring before opening apps you flagged as distracting. Backed by an Accessibility Service.
3. **Focus Mode** — Work / Study / Sleep / Custom. Blocks the selected apps for the chosen duration. Tracks streaks.
4. **Daily Limits** — Per-app daily caps. The user sees a warning pause when they exceed the limit.
5. **Unlock Tracker** — Counts unlocks per day, identifies peak hours, surfaces averages.
6. **Challenges** — XP, streaks, badges. Seeded with "No social for 2 hours", "No shorts/reels", "Phone-free sleep", "Weekend detox".
7. **Settings** — Theme (light/dark/system), dynamic colors, notifications, default mindful pause length, manage subscription, privacy.
8. **Onboarding** — Smooth flow that explains why each permission (Usage Access, Accessibility, Overlay) is needed.
9. **Home Widget** — Glance widget showing today's screen time, unlocks, and the active focus timer.

---

## Project structure

```
app/src/main/java/com/ksp/screentimereducer/
├── ScreenTimeApp.kt              # @HiltAndroidApp, RevenueCat init, notification channels
├── MainActivity.kt               # Splash + Compose host
├── core/                         # Cross-cutting helpers (time, permissions, notifications, workmanager)
├── data/
│   ├── local/                    # Room entities, DAOs, database
│   ├── preferences/              # DataStore + UserPreferences
│   ├── source/                   # UsageStatsManager + PackageManager wrappers
│   ├── repository/               # Repository implementations
│   └── work/                     # CoroutineWorker implementations
├── domain/
│   ├── model/                    # Pure domain models
│   ├── repository/               # Repository interfaces
│   └── usecase/                  # Use cases
├── di/                           # Hilt modules
├── service/                      # Accessibility service, focus foreground service, receivers
├── presentation/
│   ├── components/               # Reusable Composables (AppIcon, CountdownRing, GradientCard, BarChart…)
│   ├── delay/                    # DelayOverlayActivity + screen
│   ├── onboarding/               # Onboarding flow
│   ├── dashboard/ focus/ limits/ challenges/ settings/
│   ├── main/ navigation/         # MainShell + NavHost
│   └── RootNavigator.kt
├── subscription/                 # RevenueCat SubscriptionLauncher + PaywallActivity
├── widget/                       # Glance widget
└── ui/theme/                     # Color, Type, Shape, Theme, Gradients
```

---

## Building

### Requirements
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35 (compileSdk = 35, minSdk = 26, targetSdk = 35)

### Steps
1. Clone the repo.
2. Create `local.properties` at the project root (if not already created by Android Studio):
   ```properties
   sdk.dir=/path/to/your/Android/sdk
   ```
3. (Optional, recommended for paywall testing) Add your RevenueCat **Google** SDK key:
   ```properties
   revenuecat.api.key=goog_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```
   If unset, the build still compiles. `Purchases.configure` is skipped and "Manage subscription" falls back to the Play Store subscriptions page.
4. From Android Studio: **Sync project**, then **Run** the `app` configuration on a device or emulator running Android 8.0+ (API 26).

---

## Runtime permission setup

These three permissions must be granted by the user. The Onboarding flow opens the correct settings page for each.

### 1. Usage Access (required for screen time & unlock data)
- `Settings → Apps → Special app access → Usage access → ScreenTimeReducer → Allow`

### 2. Accessibility Service (required for App Opening Delay)
- `Settings → Accessibility → Installed services → ScreenTimeReducer → Enable`
- The service only listens for `TYPE_WINDOW_STATE_CHANGED` events and is declared with `isAccessibilityTool=true`.

### 3. Display over other apps (overlay)
- `Settings → Apps → Special app access → Display over other apps → ScreenTimeReducer → Allow`
- We use a **translucent Activity** for the delay screen rather than a system overlay window — this is more reliable on Android 12+ — but the permission still lets us bring the activity to front in some edge cases.

### Optional
- **Notifications** (Android 13+) — for focus session updates and reminders.

---

## RevenueCat setup

1. Create a project on [revenuecat.com](https://www.revenuecat.com) and a Google Play app under it.
2. In the RevenueCat dashboard, create at least one **Offering** with one or more **Packages** (monthly / annual / lifetime). Map each package to a Play Console subscription / IAP product.
3. Copy the **Google Play SDK key** (starts with `goog_`) into `local.properties`:
   ```properties
   revenuecat.api.key=goog_xxx
   ```
4. Re-build. `ScreenTimeApp.onCreate()` will call `Purchases.configure(...)`.
5. The "Manage subscription" link in Settings opens `PaywallActivity`, which fetches the current offering and renders packages. Selecting a package calls `Purchases.purchaseWith(...)`.
6. For development testing, add internal testers in the Play Console and use a test license account.

---

## Accessibility service — how the App Delay works

1. The user adds an app to **Daily limits** with **Mindful pause** enabled.
2. `AppMonitorAccessibilityService` listens for `TYPE_WINDOW_STATE_CHANGED` events.
3. When the foreground package changes to a flagged app, the service:
   - Checks a 30-second per-package cooldown to avoid loops.
   - Looks up the matching `AppRule` (off the main thread).
   - Launches `DelayOverlayActivity` with the package, delay seconds, and reason (`DELAY` / `FOCUS_BLOCK` / `LIMIT_REACHED`).
4. The overlay screen shows a countdown ring, the app icon, and a motivational message. The "Open anyway" button is disabled until the countdown reaches zero.
5. If the user taps "Not now", the activity finishes and `MainActivity` is brought to front instead.

The service is battery-efficient: it filters by event type, uses a per-package cooldown, and never blocks the main accessibility thread.

---

## Architecture notes

- **MVVM + Clean Architecture.** UI → ViewModel (with `StateFlow<UiState>`) → UseCase → Repository (interface in `domain`, impl in `data`) → DataSource.
- **`sealed interface` UiState.** Used in screens that have non-trivial loading/empty/ready branches (e.g. `DashboardUiState`).
- **Hilt DI.** `AppModule` provides dispatchers + context. `DatabaseModule` provides Room + DAOs. `RepositoryModule` binds repository interfaces.
- **Room 2.6.1.** All DAOs use `@Upsert` and return `Flow` for reactive queries.
- **DataStore Preferences.** Used for user settings, onboarding completion, XP, focus streak.
- **WorkManager.** `UsageAggregationWorker` (15min) syncs UsageStats into Room. `WidgetRefreshWorker` (30min) refreshes the Glance widget. Both use `HiltWorkerFactory`.
- **Foreground Service.** `FocusSessionService` (specialUse = `focus_mode_session`) keeps the focus session alive and shows a persistent notification.
- **Receivers.** `UnlockReceiver` (ACTION_USER_PRESENT) records unlock events; `BootReceiver` reschedules WorkManager jobs.
- **Glance widget.** `ScreenTimeWidget` reads a snapshot through Hilt's `EntryPointAccessors` (widget code can't be `@AndroidEntryPoint`).

---

## Troubleshooting

- **Hourly chart looks "smoothed".** The hourly distribution is approximated from daily totals because querying `UsageStatsManager` hour-by-hour for every recomposition is expensive. To make it exact, switch `UsageRepositoryImpl.observeHourly()` to call `aggregate(start, end)` for each hour bucket once per worker run and cache.
- **Accessibility service keeps getting disabled by the OS.** Some OEMs (Xiaomi, OPPO) aggressively kill services. Add the app to the "Auto-start" allowlist on those devices.
- **Delay overlay doesn't appear on Android 12+.** Make sure the user granted **Display over other apps** *and* enabled the accessibility service. The activity is launched with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP`.
- **`Purchases.sharedInstance` crashes.** It means `Purchases.configure(...)` was never called. Verify `revenuecat.api.key` is set in `local.properties` and `BuildConfig.REVENUECAT_API_KEY` is non-empty at runtime.

---

## License

This is a sample / template app. Adapt as you wish.
# screen_time_reducer_android
