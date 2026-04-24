# gradle/build-logic

Gradle convention plugins that centralize build config. Included build — `settings.gradle.kts` applies `../repositories.gradle.kts` and registers the root version catalog.

## Plugin layers

**Module plugins** are the entry points that consumer `build.gradle.kts` files apply. They compose **convention plugins** (which should not be applied directly).

### Module plugins

| ID | What it sets up |
|----|-----------------|
| `aktual.module.kotlin` | KMP (desktop + android) with default hierarchy, Android library target, coroutines, test deps |
| `aktual.module.compose` | `kotlin` + Compose compiler/resources + Material3 + compose test deps |
| `aktual.module.viewmodel` | `kotlin` + Compose compiler + Molecule + lifecycle-viewmodel + Metro DI |
| `aktual.module.di` | `kotlin` + Metro DI (no Compose) |
| `aktual.module.jvm` | JVM-only (no KMP, no Android) + style + test deps |

### Convention plugins (composed by module plugins)

`ConventionKotlinBase` (warnings-as-errors, free compiler args like `-Xcontext-parameters`/`-Xexpect-actual-classes`, `compileAll` task, Metro, buildconfig/lint workaround), `ConventionKotlinJvm` (toolchain via `.java-version`), `ConventionCompose` (compiler plugin, metrics, stability), `ConventionDi` (Metro with full binding-graph validation + hints + shrink unused), `ConventionStyle` (detekt + licensee), `ConventionDetekt`, `ConventionLicensee` (Apache-2.0/MIT/BSD/EPL-1.0), `ConventionTest` (Burst, `testAll`, Robolectric/MockK JVM args), `ConventionTestDependencies`, `ConventionIdea`.

Helpers: `Internal.kt` (kotlin{} DSL extension + `testLibraries`/`androidTestLibraries`), `Utils.kt` (`optIn()`), `AndroidCommon.kt` (`Lint.commonConfigure()`, `buildNamespace()` from Gradle path), `Dependencies.kt` (`androidHostTestDependencies`).

## Conventions

- Consumer `build.gradle.kts` files apply **module plugins**, never convention plugins directly.
- Use lazy Gradle APIs (`configureEach`, `named`, `withType`) — eager ones (`all`, `forEach`) break the configuration cache.
- Java version comes from `.java-version` at repo root. Don't hardcode it.
- Centralize new free compiler args and opt-ins in `ConventionKotlinBase`, not per-module.
- `compileAll` (registered by `ConventionKotlinBase`) is the preferred compile check.
