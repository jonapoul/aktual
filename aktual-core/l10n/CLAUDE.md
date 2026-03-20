### Localization (L10n)

Strings are managed in the `:aktual-core:l10n` module using XML resource files and the **Catalog** Gradle plugin for code generation.

**String definitions:** `src/commonMain/composeResources/values/`

Files are organized by feature:
- `strings-core.xml` - Core app strings (nav, theme names, months, etc.)
- `strings-account.xml` - Account/login strings
- `strings-settings.xml` - Settings screen strings
- `strings-budget-list.xml`, `strings-budget-transactions.xml`, `strings-budget-reports.xml`, `strings-budget-sync.xml` - Budget feature strings
- `strings-about.xml` - About screen strings
- `strings-metrics.xml` - Metrics strings

**Adding a new string:**

1. Add to the appropriate XML file:
   ```xml
   <string name="feature_my_string">My string</string>
   <string name="feature_my_parameterized_string">Hello %1$s, you have %2$d items</string>
   ```

2. Regenerate the `Strings` object:
   ```bash
   ./gradlew :aktual-core:l10n:catalog
   ```

3. Use in code:
   ```kotlin
   Strings.featureMyString
   Strings.featureMyParameterizedString(string1 = "World", int2 = 5)
   ```

For reference, the generated classes (`Strings`, etc.) are output in `build/generated/kotlin/catalogCommonMain`, relative to this file.

**Naming conventions:**
- XML: `snake_case` prefixed by feature (e.g., `settings_theme_dark`)
- Generated Kotlin: `camelCase` (e.g., `Strings.settingsThemeDark`)
- All generated properties are `@Composable`
