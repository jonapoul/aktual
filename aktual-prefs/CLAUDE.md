# aktual-prefs

Preferences and settings, split into `vm` (state) and `ui` (Compose). `impl` stores this device's settings in DataStore (`AppScope`). `budget` implements `BudgetFormatPreferences`, the formatting synced with the open budget (`BudgetScope`). `components` holds the preference item composables shared with other features: `BasicPreferenceItem`, `PreferenceGroup` and the value-based `BooleanPreferenceItem`. Items inside a `PreferenceGroup` drop their own card background and use the group's.

## Device vs budget settings

Settings that upstream syncs with the budget (its `SyncedPrefs`: currency, number format, hide fraction, date format, first day of week) live in the budget's `preferences` table, not DataStore. `BudgetFormatPreferences` reads them through `PreferencesDao` and writes with `BudgetSync`, using upstream's stored formats, so they sync with the web app and other devices. Their VMs (`AmountSettingsViewModel`, `DateSettingsViewModel`) are in `BudgetScope`, and the root settings screen groups their pages under the budget's name, greyed out while no budget is open. "Switch budget" closes the open budget and forgets it as the last opened one, so this holds from the budget list and the next launch starts there. Settings search shows results from `BudgetSettingsPages` disabled while no budget is open. Everything else is a device setting in DataStore, under the root screen's "This device" group.

Upstream's experimental feature flags (`SyncedPrefKey.FeatureFlag`, stored as `flags.<name>`) are synced the same way, through `BudgetFeatureFlags`. `ExperimentalSettingsViewModel` lists the flags Aktual supports in `Supported`; the rest show their synced value but can't be toggled. Add a flag there once Aktual implements it, and gate the feature on it like upstream does. Report flags are mapped by `WidgetType.featureFlag` (`aktual-budget:reports:vm`): `ChartDataLoader.load` shows a disabled card for those reports while their flag is off, the add report screen hides them, and `FormulaReportViewModel` / `MonteCarloViewModel` check their flag themselves. `EnableBanking` is dropped from the providers `BankSyncProvidersViewModel` and `LinkBankAccountViewModel` offer, but already linked accounts keep syncing. `CurrencySupport` hides the currency group in `AmountSettingsViewModel`, but the stored currency still applies everywhere. Tests can use `FakeBudgetFeatureFlags` from `aktual-test`.

The experimental page is in settings search as `SettingsPage.Experimental`, a budget page. `SettingsIndex` lists every flag by the same title the screen shows (`FeatureFlag.title` there must match `ExperimentalSettingsScreen`), so a new flag needs a title in both places. An index entry with a `flag` only shows in search, with everything in it, while the open budget has that flag on, read through `OpenBudgetFlags`. The currency group uses it for `CurrencySupport`, and `GoalTemplatesUi` for `GoalTemplates`.

`FormatConfigUseCase` (`aktual-app:nav`) reads the open budget's formats through `BudgetFormatPreferencesGraph`, falling back to upstream's defaults with no budget open.

## Adding a new setting

1. **Declare it.** Pick the right `*Preferences` interface in `aktual-prefs/src/.../` (e.g. `AppPreferences`, `FormatPreferences`, `TransactionPreferences`, or `BudgetFormatPreferences` for one synced with the budget) and add `val myPref: Preference<T>`. Implement in the matching `*PreferencesImpl.kt` using `dataStore.boolean/float/int/string/translated(...).required()`.

1. **Feed a config (if needed).** If the pref contributes to a config object (e.g. `BlurConfig`, `FormatConfig`), update the corresponding use case in `aktual-app/nav/src/commonMain/.../UseCases.kt`.

1. **Add to state.** Each settings screen has its own `*SettingsState` under `aktual-prefs/vm/.../<screen>/` (`systemui`, `bars`, `dialogs`, `security`, `format`, `amounts`, `dates`, `transactions`, `theme`, `updates`). Add a field using the appropriate wrapper:
   - `BooleanPreference` - toggle
   - `SliderPreference` - float slider with a `range` (see the factories in `SharedModels.kt`)
   - `ListPreference<T>` - enum dropdown

1. **Collect in the VM.** In the matching `*SettingsViewModel.kt`, using the helpers in `PreferenceCollection.kt`:
   ```kotlin
   val myPref by collectAsState(preferences.myPref)
   // ... myPref = BooleanPreference(value = myPref, onChange = { launchAndSet(preferences.myPref, it) })
   ```

1. **Add the string.** In `aktual-core/l10n/src/commonMain/composeResources/values/strings.xml` (the `settings` section), then `./gradlew :aktual-core:l10n:catalog`. Access via `Strings.settingsSectionMyPref` (snake_case → camelCase).

1. **Add the UI.** In the matching `*SettingsScreen.kt` under `aktual-prefs/ui/.../<screen>/`, add an `item { }` using `BooleanPreferenceItem` / `SliderPreferenceItem` / `ListPreferenceItem`.

1. **Add it to search.** Add an entry to `SettingsIndex` in `aktual-prefs/vm/.../search/`, under the same page and group as on screen and with the same title string, so the settings search finds it. Tapping a result scrolls to and flashes the item by matching that title (and its `PreferenceGroup` title), so a mismatch only lands on the page.

1. **Fix previews** that construct state objects directly.

## Changing a stored key or format

Don't just swap the key, or existing users lose their value. Add a `DataMigration<Preferences>` under `aktual-prefs/impl/.../migration/` and append it to `preferenceMigrations()`, which `DataStoreContainer` runs whenever the DataStore opens. To drop keys that aren't used any more, add them to a `RemoveKeys`. Freeze any lookup data in the migration itself rather than reading the current enum, since that can change later.

## Adding a new settings screen

The root `SettingsScreen` only lists links to the other settings screens.

1. New `*SettingsState` + `*SettingsViewModel` in their own package under `aktual-prefs/vm/`, plus a smoke test in `aktual-test:smoke`.
1. New navigator + route in `aktual-core:nav`'s `SettingsNavigators.kt` / `SettingsRoutes.kt`.
1. New `*SettingsScreen.kt` under `aktual-prefs/ui/`, built on `SettingsListScaffold`, and an `entry` in `SettingsNavEntryContributor`. For settings whose output is worth comparing, use `PreviewSettingsScaffold` instead, which pins a live preview above the list (beside it on wide windows), and `PreviewOptionGroup` for radio options that each show their own output.
1. A `SubSettingsItem` in `SettingsScreen.kt` with its `SettingsAction`, and update `aktual-app/nav/navgraph.mmd`.

## Security settings

The app lock toggle needs a successful unlock before it's saved. The VM can't show the system prompt, so it calls `AppLockAuthenticator` (bound per platform in `aktual-app:android` / `aktual-app:desktop`). On Android, `AndroidAppLockAuthenticator` passes the request to `AktualActivity`, which shows the prompt and reports the result back.

## App icon

`AppIcon` lists the launcher icon variants, and `AppIconController` applies one per platform. `AndroidAppIconController` (`aktual-core:android`) enables the matching `Launcher<Name>` activity-alias in the app manifest and disables the rest. `DesktopAppIconController` (`aktual-core:jvm`) stores the choice, and the window icon follows it.

Each variant is drawn twice, so keep the two in step: in Compose by `AppIconPainter.kt` (`components`, used by the picker and the desktop window), and as vector drawables in `aktual-app/android/src/main/res/drawable` with an adaptive icon per variant in `mipmap-anydpi`. Adding a variant needs the enum entry, its manifest alias, the drawables, and a label string. The debug and ci build types override `ic_launcher_banner` / `ic_launcher_banner_monochrome` to stamp DEV / CI across every variant.
