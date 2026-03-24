# aktual-settings

Settings screen, split into `vm` (ViewModel/state) and `ui` (Compose UI).

## Adding a New Setting

### 1. Add the preference

Choose the appropriate interface in `aktual-core/prefs/` based on the preference's domain:
- `AppPreferences` — core app prefs (token, serverUrl, privacy)
- `CurrencyPreferences` — currency display (currency, symbol position, spacing)
- `FormatPreferences` — data formatting (number format, date format, first day of week, hide fraction)
- `SystemUiPreferences` — UI chrome (bottom bar visibility, blur settings)

**Interface:** `aktual-core/prefs/.../[Chosen]Preferences.kt`
```kotlin
val myNewPref: Preference<Boolean>
```

**Implementation:** `aktual-core/prefs/impl/.../[Chosen]PreferencesImpl.kt`
```kotlin
override val myNewPref: Preference<Boolean> =
  dataStore.boolean(key = booleanPreferencesKey("myNewPref"), default = true).required()
```

Supported types: `boolean`, `float`, `int`, `string`, or `translated` (for enums).

### 2. Wire into the use case (if needed)

If the preference feeds a config object (e.g., `BlurConfig`, `FormatConfig`), update the corresponding use case in `aktual-app/nav/.../UseCases.kt` to collect and include the new field.

### 3. Add to screen state

**State class:** `aktual-settings/vm/.../SettingsScreenState.kt` — add a field to the appropriate `*ConfigState` data class (e.g., `SystemUiConfigState`).

Use the appropriate wrapper type:
- `BooleanPreference` — toggle switch
- `SliderPreference` — float slider (wrap with a factory function for range, see `BlurRadiusPreference`)
- `ListPreference<T>` — dropdown/list picker for enums

### 4. Collect in ViewModel

**ViewModel:** `aktual-settings/vm/.../SettingsViewModel.kt` — in the relevant `*State()` composable method:
```kotlin
val myNewPref by preferences.myNewPref.collectAsStateFlow()
// ...add to returned state object:
myNewPref = BooleanPreference(
  value = myNewPref,
  onChange = { preferences.myNewPref.launchAndSet(it) },
),
```

### 5. Add localization string

**XML:** `aktual-core/l10n/src/commonMain/composeResources/values/strings-settings.xml`
```xml
<string name="settings_section_my_new_pref">My new preference</string>
```

**Regenerate:** `./gradlew :aktual-core:l10n:catalog`

Access via `Strings.settingsSectionMyNewPref` (XML `snake_case` → Kotlin `camelCase`).

### 6. Add UI item

**Group composable:** `aktual-settings/ui/.../root/` — add to the appropriate group file (e.g., `SystemUiGroup.kt`):
```kotlin
BooleanPreferenceItem(
  preference = state.myNewPref,
  title = Strings.settingsSectionMyNewPref,
  subtitle = null,
  icon = MaterialIcons.SomeIcon,
  includeBackground = false,
)
```

Available UI components:
- `BooleanPreferenceItem` — switch toggle
- `SliderPreferenceItem` — slider with value label
- `ListPreferenceItem` — dropdown list

### 7. Fix previews

Update any preview composables (e.g., in `SettingsScreen.kt`) that construct state objects directly — add the new field with a default value.

## Adding a New Section

1. Create a new `*ConfigState` data class in `SettingsScreenState.kt`
2. Add it to `SettingsScreenState`
3. Add a `@Composable` state builder method in `SettingsViewModel`
4. Create a new `*Group.kt` composable in `aktual-settings/ui/.../root/` using `PreferenceGroup`
5. Add `item { NewGroup(state.newSection) }` to the `LazyColumn` in `SettingsScreen.kt`
