# aktual-core:l10n

XML string resources + the **Catalog** Gradle plugin for typed codegen.

Strings live in `src/commonMain/composeResources/values/strings-<feature>.xml` (one file per feature area — `core`, `account`, `settings`, `budget-*`, `about`, `metrics`).

## Adding a string

1. Add to the appropriate XML file:
   ```xml
   <string name="feature_my_string">My string</string>
   <string name="feature_my_parameterized_string">Hello %1$s, you have %2$d items</string>
   ```
1. Regenerate: `./gradlew :aktual-core:l10n:catalog`.
1. Use: `Strings.featureMyString` / `Strings.featureMyParameterizedString(string1 = "...", int2 = 5)`.

## Conventions

- XML names are `snake_case` prefixed by feature; generated Kotlin is `camelCase`.
- All generated `Strings.*` properties are `@Composable`.
- Generated code lives in `build/generated/kotlin/catalogCommonMain` if you need to read it.
