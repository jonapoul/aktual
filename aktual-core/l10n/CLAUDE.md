# aktual-core:l10n

XML string resources + the **Catalog** Gradle plugin for typed codegen.

Strings live in `src/commonMain/composeResources/values/strings-<feature>.xml` (one file per feature area - `core`, `account`, `settings`, `budget-*`, `about`, `metrics`).

Strings with a US/UK spelling difference live in three places, and all three need updating together:

- `values/` - US English, the fallback for non-English locales
- `values-en/strings.xml` - British spelling, used by every English locale except the US
- `values-en-rUS/strings.xml` - the US spelling again, since `values-en` would otherwise win for `en-US`

The two override files hold only the strings that differ and must have the same set of names.

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
- Don't escape apostrophes or quotes (`\'`, `\"`). These are Compose resources, not Android `res/`, so the backslash is shown literally. Only `\n`, `\t`, `\uXXXX` and `\\` are unescaped.
- Generated code lives in `build/generated/kotlin/catalogCommonMain` if you need to read it.
