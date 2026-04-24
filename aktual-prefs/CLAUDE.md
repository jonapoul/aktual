# aktual-prefs

Preferences and settings, split into `vm` (state) and `ui` (Compose).

## Adding a new setting

1. **Declare it.** Pick the right interface in `aktual-prefs/src/.../` — `AppPreferences`, `CurrencyPreferences`, `FormatPreferences`, `SystemUiPreferences`, or `ThemePreferences` — and add `val myPref: Preference<T>`. Implement in the matching `*PreferencesImpl.kt` using `dataStore.boolean/float/int/string/translated(...).required()`.

1. **Feed a config (if needed).** If the pref contributes to a config object (e.g. `BlurConfig`, `FormatConfig`), update the corresponding use case in `aktual-app/nav/ui/.../UseCases.kt`.

1. **Add to state.** In `aktual-prefs/vm/.../SettingsScreenState.kt` add a field to the relevant `*ConfigState` using the appropriate wrapper:
   - `BooleanPreference` — toggle
   - `SliderPreference` — float slider (wrap in a factory for range; see `BlurRadiusPreference`)
   - `ListPreference<T>` — enum dropdown

1. **Collect in the VM.** In `SettingsViewModel.kt`'s relevant `*State()` composable:
   ```kotlin
   val myPref by preferences.myPref.collectAsStateFlow()
   // ... myPref = BooleanPreference(value = myPref, onChange = { preferences.myPref.launchAndSet(it) })
   ```

1. **Add the string.** In `aktual-core/l10n/src/commonMain/composeResources/values/strings-settings.xml`, then `./gradlew :aktual-core:l10n:catalog`. Access via `Strings.settingsSectionMyPref` (snake_case → camelCase).

1. **Add the UI.** In the matching group under `aktual-prefs/ui/.../root/`, use `BooleanPreferenceItem` / `SliderPreferenceItem` / `ListPreferenceItem`.

1. **Fix previews** that construct state objects directly.

## Adding a new section

1. New `*ConfigState` in `SettingsScreenState.kt`, add to `SettingsScreenState`.
1. New `@Composable` state builder in `SettingsViewModel`.
1. New `*Group.kt` under `aktual-prefs/ui/.../root/` using `PreferenceGroup`.
1. `item { NewGroup(state.newSection) }` in the `LazyColumn` of `SettingsScreen.kt`.
