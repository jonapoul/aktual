# build-logic

Gradle convention plugins that centralize build config. Included build - `settings.gradle.kts` applies `../repositories.gradle.kts` and registers the root version catalog.

## Plugin layers

**Module plugins** are the entry points that consumer `build.gradle.kts` files apply. They compose **convention plugins** (which should not be applied directly).

### Module plugins

| ID | What it sets up |
|----|-----------------|
| `aktual.module.kotlin` | KMP with only the `desktop` JVM target, default hierarchy plus a `jvmShared` group over the JVM targets, coroutines, test deps. |
| `aktual.module.kmp-android` | `ModuleKmpAndroid`: adds the Android library target, lint, Robolectric setup and android test deps (including `:aktual-test:android`) on top of `kotlin` (or `di`) |
| `aktual.module.android` | `ModuleAndroidLibrary`: plain (non-KMP) Android library with lint, Robolectric unit tests and test deps. For android-only code like platform bindings |
| `aktual.module.compose` | `kotlin` + `android` + Compose compiler/resources + Material3 + compose test deps |
| `aktual.module.viewmodel` | `kotlin` + `android` + Compose compiler + Molecule + lifecycle-viewmodel + Metro DI, plus `aktual-test` for tests |
| `aktual.module.di` | `kotlin` + Metro DI (no Compose, JVM-only unless `android` is added) |
| `aktual.module.jvm` | JVM-only (no KMP, no Android) + style + test deps |

### Convention plugins (composed by module plugins)

- `ConventionKotlinBase` - warnings-as-errors, free compiler args like `-Xexpect-actual-classes`, `compileAll` task, `lintAll` task (runs `lint` if the module has it, used by `scripts/lint.sh`), Metro, buildconfig/lint workaround, sort-dependencies check
- `ConventionKotlinJvm` - toolchain via `.java-version`
- `ConventionCompose` - compiler plugin, metrics, stability
- `ConventionDi` - Metro with full binding-graph validation + hints + shrink unused
- `ConventionStyle` - detekt + licensee
- `ConventionDetekt` - only the type-resolution task per compilation runs: the per-source-set tasks are disabled, and the android ones only run when the module has `androidMain` / `androidHostTest` sources. The detekt tasks are silent and never fail. `detektCheck` (`DetektReportTask`) merges their checkstyle reports, writes the sorted, deduplicated issues to the module's `build/reports/detekt/issues.txt` and fails if there are any. `DetektSummaryService` (a shared build service) collects the issues of every `detektCheck` in the invocation, prints them once at the end of the build as clickable `file://` links and writes them to the root `build/reports/detekt/all-issues.txt`. It needs `--continue` to cover modules after the first failure
- `ConventionLicensee` - Apache-2.0/MIT/BSD/EPL-1.0
- `ConventionSortDependencies` - checks dependency declarations are in canonical order via square/gradle-dependencies-sorter; run `./gradlew sortDependencies` to fix
- `ConventionTest` - Burst, `testAll`, Robolectric/MockK JVM args, `maxParallelForks` of a quarter of the CPU cores (1 to 4)
- `ConventionTestDependencies`
- `ConventionIdea`
- `ConventionAtlas` - in modules, puts the chart at `build/atlas/chart.svg`. At the root, `atlasGenerate` also writes `build/atlas/index.html` (`WriteAtlasIndex`), linking to every module's chart, and prints its `file://` URL

Helpers live in `aktual.gradle.dsl`:

- `TestLibraries.kt` - `testLibraries`/`androidTestLibraries`
- `OptIn.kt` - `optIn()`
- `AndroidCommon.kt` - `Lint.commonConfigure()`, `buildNamespace()` from Gradle path
- `Dependencies.kt` - `androidHostTestDependencies`, `desktopMainDependencies`/`desktopTestDependencies` (the JVM target is named `desktop`, so use these instead of blueprint's `jvmMain`/`jvmTest` helpers)

KClass-friendly wrappers:

- `ExtensionAware.kt` - `kotlin{}`
- `ExtensionContainer.kt` - `configure()`
- `PluginManager.kt` - `apply()`
- `TaskCollection.kt` - `withType()`

## Conventions

- Consumer `build.gradle.kts` files apply **module plugins**, never convention plugins directly.
- Use lazy Gradle APIs (`configureEach`, `named`, `withType`) - eager ones (`all`, `forEach`) break the configuration cache.
- Java version comes from `.java-version` at repo root. Don't hardcode it.
- Centralize new free compiler args and opt-ins in `ConventionKotlinBase`, not per-module.
- `compileAll` (registered by `ConventionKotlinBase`) is the preferred compile check.
