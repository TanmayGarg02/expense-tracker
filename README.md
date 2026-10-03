# Wallet Manager

Wallet Manager is an Android app for keeping everyday finances, recurring payments, and investments together. It is built with Kotlin and Jetpack Compose, and stores its data on the device with Room.

## What you can do

- **Track money:** Record income, expenses, and transfers; assign categories and accounts; add notes and cashback; and review recent activity.
- **See your cash flow:** View account balances, monthly income and spending, and weekly or monthly spending summaries. Hide balances when you want more privacy.
- **Organize accounts:** Keep balances for multiple accounts and inspect their transactions.
- **Plan ahead:** Set up recurring salary, bill, subscription, or SIP entries and pause or resume them.
- **Track investments:** Record holdings, invested value, and current value, and review pending IPOs and fixed deposits.
- **Review activity:** Browse transactions by date in the calendar, filter activity, and see spending statistics.
- **Personalize the app:** Set a profile name and picture, choose a base currency (INR, USD, EUR, or GBP), save notes, and create reminders.
- **Move your data:** Import and export transaction data as CSV from Settings.

The app starts with sample data when its local database is empty. You can clear app data or use **Settings → Wipe all data** to remove the records and start fresh.

## Requirements

- Android Studio with Android SDK Platform 37 installed
- JDK 17
- Android 8.0 (API 26) or newer device/emulator

The project uses Android Gradle Plugin 9.1.1, Kotlin 2.4.10, and Gradle Wrapper. Dependencies are resolved from Google Maven and Maven Central.

## Build and run

1. Clone or download this repository.
2. Open the project root in Android Studio and allow Gradle to sync, or build from a terminal:

   ```bash
   ./gradlew :app:assembleDebug
   ```

3. Install and launch the debug build on a connected device or emulator:

   ```bash
   ./gradlew :app:installDebug
   ```

To launch it from Android Studio, select the `app` run configuration and choose a device running Android 8.0 or newer.

## Project layout

```text
app/src/main/java/com/example/walletmanager/
├── data/
│   ├── local/          Room entities, DAO, and database
│   ├── model/          Finance and UI-state models
│   └── repository/     Data access and CSV import/export
├── di/                 Koin dependency-injection setup
├── notifications/      Reminder, IPO, and fixed-deposit notifications
└── ui/
    ├── component/      Shared Compose components
    ├── navigation/    Routes, navigation graph, and bottom bar
    └── screen/         Home, accounts, investments, plans, statistics, and more
```

The UI is implemented with Jetpack Compose and Material 3. A `WalletViewModel` connects the screens to the repository; Room persists accounts, transactions, plans, holdings, and related settings locally.

## Notifications

The app requests Android's notification permission when needed. Reminders and scheduled IPO or fixed-deposit alerts depend on notification permission being enabled in Android settings.

## Data and privacy

Financial records are stored in the app's local Room database. CSV export creates a file you can save or share; imported files add transaction data. Keep exported files somewhere private. Uninstalling the app or wiping its data removes locally stored records.

## License

No license file is currently included. Contact the repository owner before redistributing this project.
