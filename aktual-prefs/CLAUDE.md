# aktual-prefs

Preferences and settings, split into `vm` (state) and `ui` (Compose). `ui:core` holds the preference item composables shared with other features: `BasicPreferenceItem`, `PreferenceGroup` and the value-based `BooleanPreferenceItem`.

## Adding a new setting

1. **Declare it.** Pick the right interface in `aktual-prefs/src/.../` - `AppPreferences`, `CurrencyPreferences`, `FormatPreferences`, `SystemUiPreferences`, or `ThemePreferences` - and add `val myPref: Preference<T>`. Implement in the matching `*PreferencesImpl.kt` using `dataStore.boolean/float/int/string/translated(...).required()`.

1. **Feed a config (if needed).** If the pref contributes to a config object (e.g. `BlurConfig`, `FormatConfig`), update the corresponding use case in `aktual-app/nav/src/commonMain/.../UseCases.kt`.

1. **Add to state.** Each settings screen has its own `*SettingsState` under `aktual-prefs/vm/.../<screen>/` (`systemui`, `format`, `currency`, `schedules`, `transactions`). Add a field using the appropriate wrapper:
   - `BooleanPreference` - toggle
   - `SliderPreference` - float slider (wrap in a factory for range; see `HazeRadiusPreference`)
   - `ListPreference<T>` - enum dropdown

1. **Collect in the VM.** In the matching `*SettingsViewModel.kt`, using the helpers in `PreferenceCollection.kt`:
   ```kotlin
   val myPref by collectAsState(preferences.myPref)
   // ... myPref = BooleanPreference(value = myPref, onChange = { launchAndSet(preferences.myPref, it) })
   ```

1. **Add the string.** In `aktual-core/l10n/src/commonMain/composeResources/values/strings-settings.xml`, then `./gradlew :aktual-core:l10n:catalog`. Access via `Strings.settingsSectionMyPref` (snake_case → camelCase).

1. **Add the UI.** In the matching `*SettingsScreen.kt` under `aktual-prefs/ui/.../<screen>/`, add an `item { }` using `BooleanPreferenceItem` / `SliderPreferenceItem` / `ListPreferenceItem`.

1. **Fix previews** that construct state objects directly.

## Adding a new settings screen

The root `SettingsScreen` only lists links to the other settings screens.

1. New `*SettingsState` + `*SettingsViewModel` in their own package under `aktual-prefs/vm/`, plus a smoke test in `aktual-test:smoke`.
1. New navigator + route in `aktual-core:nav`'s `SettingsNavigators.kt` / `SettingsRoutes.kt`.
1. New `*SettingsScreen.kt` under `aktual-prefs/ui/`, built on `SettingsListScaffold`, and an `entry` in `SettingsNavEntryContributor`.
1. A `SubSettingsItem` in `SettingsScreen.kt` with its `SettingsAction`, and update `aktual-app/nav/navgraph.mmd`.
