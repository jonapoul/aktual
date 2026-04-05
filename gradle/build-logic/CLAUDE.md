# gradle/build-logic

Gradle convention plugins that centralize build configuration for all modules in the project. This is an included build (`settings.gradle.kts` applies `../repositories.gradle.kts` and registers the version catalog from `../libs.versions.toml`).

## Plugin hierarchy

Plugins are split into **convention** plugins (cross-cutting concerns) and **module** plugins (entry points that compose conventions).

### Module plugins (applied by consumer `build.gradle.kts` files)

| Plugin ID | Class | What it sets up |
|-----------|-------|-----------------|
| `aktual.module.kotlin` | `ModuleKotlin` | KMP (desktop + android) with default hierarchy, Android library target, coroutines, test deps |
| `aktual.module.compose` | `ModuleCompose` | `ModuleKotlin` + Compose compiler/resources + Material3 + compose test deps |
| `aktual.module.viewmodel` | `ModuleViewModel` | `ModuleKotlin` + Compose compiler + Molecule + lifecycle-viewmodel + Metro DI |
| `aktual.module.di` | `ModuleDi` | `ModuleKotlin` + Metro DI (no Compose) |
| `aktual.module.jvm` | `ModuleJvm` | JVM-only (no KMP, no Android) + style + test deps |

### Convention plugins (composed by module plugins, not applied directly)

| Class | Concern |
|-------|---------|
| `ConventionKotlinBase` | `allWarningsAsErrors`, free compiler args (`-Xcontext-parameters`, `-Xexpect-actual-classes`, etc.), `compileAll` task, Metro DI, buildconfig/lint workaround |
| `ConventionKotlinJvm` | JVM toolchain (java version from `.java-version`), source/target compatibility |
| `ConventionCompose` | Compose compiler plugin, compose metrics, stability config, lint checks |
| `ConventionDi` | Metro plugin with full binding-graph validation, contribution hints, shrink unused bindings |
| `ConventionStyle` | Applies `ConventionDetekt` + `ConventionLicensee` |
| `ConventionDetekt` | Detekt with project config, baseline, SARIF reporting, excludes generated sources |
| `ConventionLicensee` | Licensee with allowed licenses (Apache-2.0, MIT, BSD, EPL-1.0) |
| `ConventionTest` | Burst parameterized tests, buildconfig for test resources dir, `testAll` task, Robolectric/MockK JVM args, test logging |
| `ConventionTestDependencies` | Adds common test libraries (assertk, turbine, mockk, etc.) and android test libraries (robolectric, etc.) |
| `ConventionIdea` | IntelliJ IDEA integration |

## Key files

| File | Purpose |
|------|---------|
| `Internal.kt` | `kotlin {}` extension helper, `testLibraries` / `androidTestLibraries` lists |
| `Utils.kt` | `optIn()` helpers for adding `@OptIn` compiler args |
| `AndroidCommon.kt` | `Lint.commonConfigure()`, `buildNamespace()` (converts Gradle path to Android namespace) |
| `Dependencies.kt` | `androidHostTestDependencies` source set helper |

## Conventions to follow

- Module plugins compose convention plugins — don't apply convention plugins directly from consumer `build.gradle.kts` files.
- Use lazy Gradle APIs (`configureEach`, `named`, `withType`) instead of eager ones (`all`, `forEach`) to preserve configuration cache compatibility.
- The Java version comes from the root `.java-version` file — never hardcode it.
- Free compiler args and opt-ins are centralized in `ConventionKotlinBase` — add new ones there, not in individual modules.
- The `compileAll` task (registered by `ConventionKotlinBase`) depends on all `KotlinCompile` tasks and is the preferred way to check compilation.
