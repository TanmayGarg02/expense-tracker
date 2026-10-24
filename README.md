# PocketWise

PocketWise is an Android app for keeping everyday finances, recurring payments, and investments together. It is built with Kotlin and Jetpack Compose, and stores its data on the device with Room.

## What you can do

- **Track money:** Record income, expenses, and transfers; assign categories and accounts; add notes and cashback; and review recent activity. Editable starter categories include Food, Transport, Entertainment, Travel, Education, Subscriptions, Health, and Bills. Transfer fees are recorded as separate expenses.
- **See your cash flow:** View account balances, monthly income and spending, and weekly or monthly spending summaries. Hide balances when you want more privacy.
- **Organize accounts:** Track bank, cash, credit card, and brokerage accounts. Brokerage accounts show free funds and linked investments.
- **Plan ahead:** Set up recurring payments and monthly mutual fund or index fund SIPs. A SIP can create a new holding or add contributions to an existing fund; plans can be paused or resumed.
- **Track investments:** Link holdings to a brokerage or mark them as Other, review pending IPOs and fixed deposits, and record sale proceeds when exiting a holding.
- **Review activity:** Browse transactions by date in the calendar, filter activity, and see spending statistics.
- **Personalize the app:** Set a profile name and picture, choose a base currency (INR, USD, EUR, or GBP), save notes, and create reminders.
- **Move your data:** Import and export accounts, transactions, recurring plans, and holdings as CSV from Settings.

The app creates its starter categories when its database is first opened. To explore a populated example, choose **Settings → Add sample data**. Demo records are labeled in the app. Use **Settings → Wipe all data** to clear your records and return to an empty setup.

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

For personal use, the debug build is sufficient. Android Studio and Gradle sign it with the local debug key, so you do not need to create a release keystore. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`; `./gradlew :app:installDebug` installs or updates it directly on the connected device.

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

The UI is implemented with Jetpack Compose and Material 3. A `WalletViewModel` connects the screens to the repository; Room persists accounts, transactions, plans, holdings, and related settings locally. Room migrations add new fields while retaining existing records during app updates.

## Notifications

The app requests Android's notification permission when needed. Reminders and scheduled IPO or fixed-deposit alerts depend on notification permission being enabled in Android settings.

## Data and privacy

Financial records are stored in the app's local Room database; the app does not sync them to a cloud service. CSV export creates a file you can save or share, and CSV import can add accounts, transactions, recurring plans, and holdings. Keep exported files somewhere private. Uninstalling the app or wiping its data removes locally stored records.

## License

No license file is currently included. Contact the repository owner before redistributing this project.
