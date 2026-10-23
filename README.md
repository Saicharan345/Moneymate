# MoneyMate — Personal Finance Manager

A native Android app for tracking income and expenses, setting a monthly budget and understanding where your money goes. Built with Kotlin, Jetpack Compose, Room and MVVM as a portfolio project.

> **Disclaimer:** This is an independent portfolio project. It is **not** an official Moneyview product and is not affiliated with or endorsed by Moneyview. It uses no Moneyview branding.

## 1. What the app does

- **Dashboard** – total balance, this month's income and expenses, remaining budget, spending by category and recent transactions. Every figure is computed from the database.
- **Transactions** – add, edit and delete (with confirmation); search by title; filter by type and category; newest first; empty states.
- **Budget** – set a monthly budget, see usage and remaining amount, get a warning at 80 % and an "exceeded" warning above 100 %.
- **Analytics** – month switcher, income/expense/net totals, donut chart + category bars, 6-month income-vs-expense bar chart (custom Compose `Canvas`, no chart library).
- **Settings** – budget, currency display (INR default), clear sample data, delete all transactions (both confirmed), privacy note, app info.
- **Sample data** – a small, clearly badged ("SAMPLE") data set is inserted **once** on first launch. It is never re-inserted.

All data stays on the device. The app declares **no INTERNET permission**, has no login, no fake bank links and no payments.

## 2. Technologies

Kotlin 1.9.24 · Jetpack Compose (BOM 2024.06.00) · Material 3 · Navigation Compose 2.7.7 · AndroidX ViewModel + StateFlow · Kotlin Coroutines/Flow · Room 2.6.1 (KSP) · DataStore Preferences · Gradle Kotlin DSL (AGP 8.5.2, Gradle 8.7) · JUnit 4 · Compose UI Test.

`minSdk 26` · `targetSdk / compileSdk 34` · **JDK 17**.

## 3. Architecture

```
app/src/main/java/com/saicharan/moneymate/
├── MainActivity.kt, MoneyMateApp.kt, AppContainer.kt   # entry point + manual DI
├── data/
│   ├── local/        # Room entity, DAO, database, mappers
│   └── repository/   # TransactionRepository (Room), SettingsRepository (DataStore)
├── domain/
│   ├── model/        # Transaction, Category, CurrencyOption, summaries
│   └── usecase/      # FinanceCalculator, BudgetCalculator, validators, filter, sample-data seeding
├── ui/
│   ├── dashboard · transactions · analytics · budget · settings   # screen + ViewModel per feature
│   ├── components/   # shared composables
│   ├── navigation/   # routes + bottom-nav destinations
│   └── theme/
└── util/             # money & date formatting
```

- **MVVM + unidirectional data flow.** Repositories expose `Flow`; ViewModels combine them into `StateFlow<UiState>` (Loading / Error / Success); Composables only render state and send events.
- **Money is never a `Double`.** Amounts are `BigDecimal` in the domain and stored as `Long` minor units (paise) in Room.
- **Business logic is pure Kotlin** (`domain/usecase`) so it is unit-testable without Android.
- **No DI framework** – a tiny `AppContainer` + `ViewModelProvider.Factory` keeps the build simple.

## 4. Main features (checklist)

| Area | Included |
|---|---|
| CRUD | Add / edit / delete with validation + confirmation |
| Validation | title required (≤ 60), amount > 0 with ≤ 2 decimals and an upper bound, category must match type, no future dates, notes ≤ 200 |
| Search & filter | title search, type filter, category filter, clear-filters |
| Budget | persisted locally, progress bar, 80 % warning, exceeded warning |
| Analytics | month navigation, donut, category bars, 6-month trend |
| Currency | INR (₹, lakh/crore grouping), USD, EUR, GBP – **display only, no conversion** |
| UX | edge-to-edge, light fintech theme, bottom navigation, snackbars, keyboard-aware form, content descriptions |

## 5. Prerequisites

- **Android Studio** (current stable release) – it bundles JDK 17 and the SDK manager.
- Android SDK Platform **34** (Android Studio offers to install it on first sync).
- An Android emulator (API 26+) **or** a physical phone with USB debugging enabled.
- Internet access for the first Gradle sync (dependencies are downloaded).

## 6. Open and run in Android Studio

1. `File ▸ Open…` and select the `MoneyMate` folder (the one containing `settings.gradle.kts`).
2. Wait for **Gradle sync** to finish. If prompted, install SDK Platform 34.
3. Make sure `File ▸ Settings ▸ Build, Execution, Deployment ▸ Build Tools ▸ Gradle ▸ Gradle JDK` is **JDK 17** (the bundled *jbr-17* is fine).
4. Choose a device in the toolbar and press **Run ▶**.

## 7. Run on an emulator or a physical device

**Emulator:** `Tools ▸ Device Manager ▸ Create Device` → pick a Pixel phone → system image API 34 → Finish → Run.

**Phone:** enable *Developer options* and *USB debugging*, connect via USB, accept the RSA prompt, select the phone in Android Studio and Run. Or from PowerShell:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb devices                      # your phone should be listed as "device"
.\gradlew.bat installDebug          # builds and installs the debug APK
& $adb shell am start -n com.saicharan.moneymate/.MainActivity
```

## 8. Run the tests

From a PowerShell prompt in the project folder:

```powershell
# one-time per shell: point Gradle at Android Studio's bundled JDK 17
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

.\gradlew.bat testDebugUnitTest          # JVM unit tests (no device needed)
.\gradlew.bat connectedDebugAndroidTest  # Compose UI tests (needs emulator/device)
.\gradlew.bat assembleDebug              # build the APK -> app\build\outputs\apk\debug\
```

The HTML unit-test report is written to `app\build\reports\tests\testDebugUnitTest\index.html`.

Unit tests cover: balance, monthly income/expense totals, category breakdown, 6-month totals, budget usage %, remaining budget, 80 % / 100 % thresholds, transaction & amount validation, filter/search/sort, and INR formatting.

## 9. Screenshots

Screenshots have **not** been captured yet. Add images to `docs/screenshots/` and replace the placeholders:

| Home | Transactions | Add / Edit |
|---|---|---|
| `[PLACEHOLDER: docs/screenshots/home.png]` | `[PLACEHOLDER: docs/screenshots/transactions.png]` | `[PLACEHOLDER: docs/screenshots/form.png]` |

| Analytics | Settings |
|---|---|
| `[PLACEHOLDER: docs/screenshots/analytics.png]` | `[PLACEHOLDER: docs/screenshots/settings.png]` |

## 10. Limitations and future improvements

- Currency choice changes the **symbol only**; there is no exchange-rate conversion.
- One budget for the whole month (no per-category budgets, no rollover).
- No recurring transactions, attachments, CSV export/import or cloud backup (backup is deliberately disabled for privacy).
- Light theme only; no dark theme.
- Filtering/aggregation happens in memory – fine for personal data volumes, but a very large history would benefit from SQL aggregation and paging.
- Room schema export is off and there are no migrations yet (database version 1).
- Possible next steps: biometric app lock, per-category budgets, recurring entries, export, widgets, Hilt, screenshot tests.

## Verification notes

- The pure-Kotlin domain and util layers and all unit-test files were compiled and executed outside Android (Kotlin 1.9.24 compiler + a minimal JUnit shim): 40 tests, 0 failures.
- The Android/Compose layers (Room, DataStore, ViewModels, screens, navigation) and the instrumented UI test were written carefully but **not built in that environment** (no Android SDK). Run `.\gradlew.bat assembleDebug` and `.\gradlew.bat testDebugUnitTest` to confirm on your machine, and open an issue if anything fails.

## License

MIT — see [LICENSE](LICENSE).
