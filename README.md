<p align="center">
  <img src="docs/img/header.png" alt="Aktual - an unofficial Kotlin Multiplatform client for Actual Budget" width="100%">
</p>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://android-arsenal.com/api?level=28"><img alt="API" src="https://img.shields.io/badge/API-28%2B-brightgreen.svg?style=flat"/></a>
  <a href="https://github.com/jonapoul/aktual"><img alt="Coverage" src="https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/jonapoul/6bf1b7a292bc61bcca966bee22e8b456/raw/coverage-badge.json"/></a>
  <a href="https://nightly.link/jonapoul/aktual/workflows/nightly/main/android.zip"><img alt="Nightly APK" src="https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/jonapoul/6bf1b7a292bc61bcca966bee22e8b456/raw/nightly-android-badge.json"/></a>
</p>

# Summary

> [!CAUTION]
> This is a pet project, not affiliated with the main Actual project. I give no guarantees of compatibility, functionality or anything else besides.

An :warning: UNOFFICIAL :warning: set of clients for the [Actual personal budgeting software](https://github.com/actualbudget/actual) - all written in Kotlin:

- [Android](aktual-app/android)
- 🚧 [Desktop](aktual-app/desktop) 🚧
- 🚧 iOS - TBC? 🚧

> [!IMPORTANT]
> This is nowhere near complete - do not bank on it being ready to use any time soon!

# Nightly builds

Latest builds of `main`, via [nightly.link](https://nightly.link):

- [Android](https://nightly.link/jonapoul/aktual/workflows/nightly/main/android.zip)
- [Linux (.deb)](https://nightly.link/jonapoul/aktual/workflows/nightly/main/desktop-Linux.zip)
- [macOS (.dmg)](https://nightly.link/jonapoul/aktual/workflows/nightly/main/desktop-macOS.zip)
- [Windows (.msi/.exe)](https://nightly.link/jonapoul/aktual/workflows/nightly/main/desktop-Windows.zip)

## Obtainium

[Obtainium](https://github.com/ImranR98/Obtainium) installs Android apps straight from GitHub releases and keeps them up to date. Aktual publishes its nightly Android build as a GitHub pre-release, so Obtainium can track it for you.

1. Install Obtainium from its [releases page](https://github.com/ImranR98/Obtainium/releases) or [F-Droid](https://f-droid.org/packages/dev.imranr.obtainium.fdroid/).
1. On your Android device, tap the badge below. It opens Obtainium with Aktual already set up.
1. Tap "Add", then "Install".

<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://app/%7B%22id%22%3A%22dev.jonpoulton.aktual.app%22%2C%22url%22%3A%22https%3A%2F%2FGitHub.com%2Fjonapoul%2Faktual%22%2C%22author%22%3A%22jonapoul%22%2C%22name%22%3A%22Aktual%20Budget%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Atrue%2C%5C%22includeDrafts%5C%22%3Afalse%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertReleaseTitlesFilter%5C%22%3Afalse%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertReleaseNotesFilter%5C%22%3Afalse%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Atrue%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchGroupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Atrue%2C%5C%22releaseDateAsVersion%5C%22%3Afalse%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22includeZips%5C%22%3Atrue%2C%5C%22zippedApkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22includeTarballs%5C%22%3Afalse%2C%5C%22tarballedApkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22minimumUpdateAgeDays%5C%22%3A%5C%22%5C%22%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%2C%5C%22detectionDateAsReleaseDate%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22installerMode%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22allowedSigningCertHashes%5C%22%3A%5C%22%5C%22%2C%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthStaleRelease%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthStaleVersion%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthFailedChecks%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthArchived%5C%22%3A%5C%22%5C%22%2C%5C%22updateHealthFilteredReleases%5C%22%3A%5C%22%5C%22%7D%22%2C%22overrideSource%22%3Anull%7D"><img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="60"></a>

Obtainium then checks for new nightlies in the background and lets you know when one is ready.

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
