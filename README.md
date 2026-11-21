<p align="center">
  <img src="docs/img/header.png" alt="Aktual - an unofficial Kotlin Multiplatform client for Actual Budget" width="100%">
</p>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://developer.android.com/tools/releases/platforms#9.0"><img alt="API" src="https://img.shields.io/badge/API-28%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/jonapoul/aktual"><img alt="Coverage" src="https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/jonapoul/6bf1b7a292bc61bcca966bee22e8b456/raw/coverage-badge.json"/></a>
  <a href="https://nightly.link/jonapoul/aktual/workflows/nightly/main/android.zip"><img alt="Nightly APK" src="https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/jonapoul/6bf1b7a292bc61bcca966bee22e8b456/raw/nightly-android-badge.json"/></a>
</p>

# About

[Actual Budget](https://github.com/actualbudget/actual) is a free, open-source personal finance app built around envelope budgeting: you give every dollar a job, and move money between categories as plans change. It's local-first, meaning each device keeps its own copy of your budget and works without a connection. You run (or rent) an Actual server, which syncs changes between your devices and can also handle bank sync and end-to-end encryption. Actual's official apps are a web app and an Electron desktop app.

Aktual is a separate, unofficial app for Android and desktop that works with the same server and the same budgets. It's written from scratch in Kotlin Multiplatform with a native UI, rather than wrapping Actual's web app. It downloads your budget from your Actual server, works on it locally and syncs changes back, so you can switch between Aktual and Actual's own apps on the same budget.

> [!CAUTION]
> Aktual is an unofficial pet project. It isn't affiliated with or supported by the Actual team, so please don't report Aktual bugs upstream.

| Platform | Status |
|---|---|
| [Android](aktual-app/android) | Covers most of Actual's features |
| [Desktop](aktual-app/desktop) | Mostly works, but a little janky |
| iOS | 🚧 TBC? 🚧 |
| Other platforms | 🚧 TBC? 🚧 |

# Install

All builds come from the latest commit on `main`. There are no stable releases yet. Android needs Android 9 or newer.

## Android

### F-Droid

🚧 TBC 🚧

### Obtainium

[Obtainium](https://github.com/ImranR98/Obtainium) installs Android apps straight from GitHub releases and keeps them up to date. Aktual publishes its nightly Android build as a GitHub pre-release, so Obtainium can track it for you.

1. Install Obtainium from its [releases page](https://github.com/ImranR98/Obtainium/releases) or [F-Droid](https://f-droid.org/packages/dev.imranr.obtainium.fdroid/).
1. On your Android device, tap the badge below. It opens Obtainium with Aktual already set up.
1. Tap "Add", then "Install".

<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://app/%7B%22id%22%3A%22dev.jonpoulton.aktual.app%22%2C%22url%22%3A%22https%3A%2F%2FGitHub.com%2Fjonapoul%2Faktual%22%2C%22author%22%3A%22jonapoul%22%2C%22name%22%3A%22Aktual%20Budget%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Atrue%2C%5C%22includeDrafts%5C%22%3Afalse%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertReleaseTitlesFilter%5C%22%3Afalse%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertReleaseNotesFilter%5C%22%3Afalse%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Atrue%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchGroupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Atrue%2C%5C%22releaseDateAsVersion%5C%22%3Afalse%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22includeZips%5C%22%3Atrue%2C%5C%22zippedApkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22includeTarballs%5C%22%3Afalse%2C%5C%22tarballedApkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22minimumUpdateAgeDays%5C%22%3A%5C%22%5C%22%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%2C%5C%22detectionDateAsReleaseDate%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22installerMode%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22allowedSigningCertHashes%5C%22%3A%5C%22%5C%22%2C%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthStaleRelease%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthStaleVersion%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthFailedChecks%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthArchived%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthFilteredReleases%5C%22%3A%5C%22%5C%22%7D%22%2C%22overrideSource%22%3Anull%7D"><img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="60"></a>

Obtainium then checks for new nightlies in the background and lets you know when one is ready.

### Direct download

[Download the latest APK](https://nightly.link/jonapoul/aktual/workflows/nightly/main/android.zip) via [nightly.link](https://nightly.link).

## Desktop

Desktop builds are still rough around the edges.

- [Linux (.deb)](https://nightly.link/jonapoul/aktual/workflows/nightly/main/desktop-Linux.zip)
- [macOS (.dmg)](https://nightly.link/jonapoul/aktual/workflows/nightly/main/desktop-macOS.zip)
- [Windows (.msi/.exe)](https://nightly.link/jonapoul/aktual/workflows/nightly/main/desktop-Windows.zip)

# Features

### Getting started
- Connect to your Actual server with a password or OpenID
- Open, download and sync budgets from your server
- End-to-end encrypted budgets
- Try the demo budget without a server

<table>
  <tr>
    <td width="33%"><img src="docs/img/feature_server_url.png" alt="Server URL screen"></td>
    <td width="33%"><img src="docs/img/feature_login.png" alt="Login screen, showing the password and OpenID options"></td>
    <td width="33%"><img src="docs/img/feature_budget_list.png" alt="Budget list, with a synced budget and an encrypted one"></td>
  </tr>
</table>

### Budgeting
- Envelope and tracking budgets
- Set, copy and transfer budget amounts, and hold money for next month
- Create, rename and delete categories and category groups
- Notes on categories and months

<table>
  <tr>
    <td width="33%"><img src="docs/img/feature_budget.png" alt="Monthly budget table with category groups expanded"></td>
    <td width="33%"><img src="docs/img/feature_budget_sheet.png" alt="Category budget sheet: set, copy or transfer an amount"></td>
    <td width="33%"><img src="docs/img/feature_budget_create.png" alt="Creating a category"></td>
  </tr>
</table>

### Accounts and transactions
- On-budget, off-budget and closed accounts
- Add, edit and delete transactions, including splits and transfers
- Search, filter and save filters
- Reconcile accounts
- Undo deletes and edits

<table>
  <tr>
    <td width="33%"><img src="docs/img/feature_transactions.png" alt="Transaction list with filter chips applied"></td>
    <td width="33%"><img src="docs/img/feature_edit_transaction.png" alt="Editing a split transaction"></td>
    <td width="33%"><img src="docs/img/feature_accounts.png" alt="Account list with on-budget, off-budget and closed accounts"></td>
  </tr>
</table>

### Schedules, rules, payees and tags
- Create and edit schedules, and post upcoming ones early
- Create and edit rules, with date and amount range conditions
- Manage and merge payees
- Manage tags

<table>
  <tr>
    <td width="33%"><img src="docs/img/feature_schedules.png" alt="Schedule list screen"></td>
    <td width="33%"><img src="docs/img/feature_rules.png" alt="Rule list screen with a few conditions and actions"></td>
    <td width="33%"><img src="docs/img/feature_payees.png" alt="Payee list"></td>
  </tr>
</table>

### Bank sync

> [!WARNING]
> GoCardless is the only provider I've been able to test. The others should work, but are untested.

- GoCardless
- SimpleFIN
- Pluggy.ai
- Enable Banking
- Akahu

<table>
  <tr>
    <td width="33%"><img src="docs/img/feature_banksync_providers.png" alt="Bank sync provider list (SimpleFIN, GoCardless, etc.)"></td>
    <td width="33%"><img src="docs/img/feature_banksync_link.png" alt="Linking a bank account to an Actual account"></td>
    <td width="33%"><img src="docs/img/feature_banksync_settings.png" alt="Per-account bank sync settings or field mappings"></td>
  </tr>
</table>

### Reports
- Dashboard with custom widgets
- Net worth, cash flow, spending, age of money, summary, calendar and custom reports
- Crossover report
- Formula reports (experimental)
- Sankey, balance forecast, budget analysis and Monte Carlo reports (experimental)

<table>
  <tr>
    <td width="33%"><img src="docs/img/feature_reports_dashboard.png" alt="Reports dashboard with several widgets"></td>
    <td width="33%"><img src="docs/img/feature_reports_custom.png" alt="A custom report, showing a bar chart by payee"></td>
    <td width="33%"><img src="docs/img/feature_reports_sankey.png" alt="Sankey report"></td>
  </tr>
</table>

### Settings and more
- Light and dark themes, plus a [catalog of community themes](https://github.com/actualbudget/actual/blob/master/packages/desktop-client/src/data/customThemeCatalog.json)
- Choose the start page for each budget
- Number, date and currency formats
- Advanced and experimental settings, matching Actual's
- API keys
- Crash reports and logs which you can view/export

<table>
  <tr>
    <td width="33%"><img src="docs/img/feature_settings.png" alt="Main settings screen"></td>
    <td width="33%"><img src="docs/img/feature_settings_theme.png" alt="Theme settings, with the light and dark pickers"></td>
    <td width="33%"><img src="docs/img/feature_settings_amounts.png" alt="Currency/number formatting settings"></td>
  </tr>
</table>

# Not yet supported

- Creating a new budget (this opens Actual in your browser for now)
- Importing from files (CSV, OFX, QIF) or other apps (YNAB, nYNAB)
- Exporting a budget
- Switching a budget between envelope and tracking
- Goal templates
- Rule action templating
- Server user management
- Stable releases. Only nightly builds are available

# Compatibility

Aktual is tested against the latest Actual server release. Its local database follows Actual's migrations, and a daily check opens a PR when upstream adds a new one, so budgets stay readable by both apps.

I do make efforts to stay compatible mostly with the latest state of Actual's main branch, so it's possible that you might see features in-app (if you use nightly builds) which aren't supported by your server yet.

# Building from source

You need the JDK version in [`.java-version`](.java-version).

```bash
git clone https://github.com/jonapoul/aktual.git
cd aktual

# Android debug APK, written to aktual-app/android/build/outputs/apk/debug/
./gradlew :aktual-app:android:assembleDebug

# Desktop installer for your OS, written to aktual-app/desktop/build/compose/binaries/main/
./gradlew :aktual-app:desktop:packageDistributionForCurrentOS
```

To test against a local server, [`actual-server/docker-compose.yml`](actual-server/docker-compose.yml) runs one on port 5006.

# Contributing

- **Bugs**: open an issue with steps to reproduce and your Android version or desktop OS. If the app crashed, attach the crash report from the in-app crash screen.
- **Feature requests**: check [Not yet supported](#not-yet-supported) first, since those gaps are already known.
- **Pull requests**: Before opening, run `./scripts/ktfmt.sh`, `./scripts/detekt.sh` and `./scripts/test.sh`.
- **Translations**: welcome! Strings live in [`aktual-core/l10n`](aktual-core/l10n/src/commonMain/composeResources). Add a `values-<language>/` folder with copies of `values/strings.xml` and `values/plurals.xml`, and translate what you can.
- **Architecture**: each module's README has its dependency chart, and [`CLAUDE.md`](CLAUDE.md) describes the project's conventions.

# License

```
Copyright 2024-2026 Jon Poulton

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

<!--
# Star History

[![Star History Chart](https://api.star-history.com/chart?repos=jonapoul/aktual&type=date&legend=top-left)](https://www.star-history.com/?repos=jonapoul%2Faktual&type=date&legend=top-left)
-->
