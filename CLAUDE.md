# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Aktual is an **unofficial** Kotlin Multiplatform client for [Actual personal budgeting software](https://github.com/actualbudget/actual). The project supports Android and Desktop (JVM) platforms with shared Kotlin code. This is a pet project and not affiliated with the main Actual project.

## General

- **IMPORTANT**: After completing any architectural or structural change, proactively check ALL CLAUDE.md files in the repo for references that need updating. Don't wait to be asked — scan for mentions of changed scopes, annotations, module structures, DI patterns, file locations, etc. Use `grep` across `**/CLAUDE.md` to find stale references.
- Prefer `kotlinx.immutable` collections on the UI layer, not regular List/Set/etc.
- When setting a `MutableStateFlow`, prefer `stateFlow.update { x }` over `stateFlow.value = x`
- When writing comments, stretch them out horizontally to match the `max_line_length` value in .editorconfig, currently 120.

## Build Commands

### Building
```bash
# Runs all kotlin compilation tasks on this module, including test source sets. This should be the first choice when making a change to a module
./gradlew :[path-to-module]:compileAll

# Check that the project kotlin files compile. You should prefer module-specific invocations of this, where possible.
./gradlew compileAll

# Build everything. This will take a long time, don't do it without asking
./gradlew build

# Build Android app
./gradlew :aktual-app:android:assemble

# Build Desktop app
./gradlew :aktual-app:desktop:assemble
```

### Testing
```bash
# Run all tests across all targets
./gradlew allTests

# Run tests for a specific module
./gradlew :aktual-account:vm:test

# Run all checks (includes tests, detekt, and other verifications)
./gradlew check
```

### Code Quality
```bash
# Run detekt static analysis on modules changed on this branch
./scripts/detekt.sh

# Run detekt on all modules. Avoid this unless explicitly asked to do it
./gradlew detektCheck

# Format changed files with ktfmt (since diverging from main)
./scripts/ktfmt.sh

# Check formatting of changed files without modifying
./scripts/ktfmt.sh check

# Format all files (ignore git check)
./scripts/ktfmt.sh --force

# Generate code coverage report
./gradlew koverHtmlReport
```

### Development
```bash
# Run Android app
./gradlew :aktual-app:android:installDebug

# Run Desktop app
./gradlew :aktual-app:desktop:run

# Generate dependency graph visualization. Only needs to be run when changing dependencies between gradle modules
./gradlew atlasGenerate

# Clean build artifacts
./gradlew clean

# Invalidate configuration cache (e.g., to refresh git versions)
./gradlew --rerun-tasks
```

### Build Performance

**Configuration Cache:** The project uses Gradle's configuration cache for optimal build performance:

- **Typical configuration time:** ~800ms (with cache)
- **Cold start:** ~5-7 seconds (first build or cache invalidation)
- **After code changes:** Sub-second (cache reused)
- **After commits:** Sub-second (cache reused)

**Important:** Git version information (`gitVersionHash()`, `gitVersionCode()`) is cached for performance. The cache does NOT invalidate when you make commits, keeping builds fast. Git versions are refreshed when:
- Configuration cache is invalidated (cold start, `--rerun-tasks`, gradle.properties changes)
- Building for CI/release (typically has cold cache)

To manually refresh git versions during development:
```bash
./gradlew --rerun-tasks build
```

## Architecture

### Module Organization

The project follows a strict **feature-based modular architecture** with clear layer separation:

```
/app                    # Application entry points
  /android              # Android app
  /desktop              # Desktop app (JVM)
  /di                   # Application-level DI setup + UI module aggregation for Metro
  /nav                  # Navigation API (routes, extensions, NavEntryContributor)
    /ui                 # Navigation UI (AktualNavHost, AktualAppContent, RootViewModel)

/modules
  /feature              # Feature modules follow: domain/ui/vm pattern
    /domain             # Business logic, use cases, repositories
    /ui                 # Compose UI screens, components, and NavEntryContributor
    /vm                 # ViewModels with presentation logic

  /core                 # Shared infrastructure
    /connection         # Network connectivity utilities
    /di                 # Core DI abstractions (AppGraph, ViewModelGraph, BudgetGraph)
    /model              # Core domain models (LoginToken, ServerUrl, etc.)
    /ui                 # Shared UI components, themes, Material3 setup

  /api                  # API clients
    /actual             # Actual server API client
    /builder            # API builder utilities
    /github             # GitHub API client

  /budget               # Budget feature modules
    /data               # SQLDelight database layer
    /encryption         # Budget encryption
    /model              # Budget domain models
    /list               # Budget list feature (ui + vm)
    /reports            # Reports feature (ui + vm)
    /sync               # Sync feature (ui + vm)
    /transactions       # Transactions feature (ui + vm)

  /prefs                # Preferences/settings feature
    /di                 # Preferences DI bindings
    /impl               # Preferences implementation (DataStore)
    /ui                 # Settings screens (ui + vm)
    /vm                 # Settings ViewModels

  /test                 # Test utilities
    /kotlin             # Common test utilities (AssertK, Turbine, MockK)
    /android            # Android test utilities (Robolectric)
    /compose            # Compose test utilities
    /di                 # Test DI utilities
    /api                # API test utilities
```

### Layer Responsibilities

**Domain Layer** (`domain` modules):
- Contains business logic, use cases, and repository interfaces
- No UI dependencies, pure Kotlin
- Dependencies: Core models, API clients, preferences
- Example: `LoginRequester` handles authentication logic

**ViewModel Layer** (`vm` modules):
- Presentation logic and state management
- Uses **Molecule** for reactive composition (`launchMolecule`)
- Exposes `StateFlow` for UI state
- Registered in Metro DI with `@ViewModelKey` and `@ContributesIntoMap`
- Dependencies: Domain layer, Core DI, Lifecycle ViewModel

**UI Layer** (`ui` modules):
- Jetpack Compose screens and components (Material3)
- Stateless composables that collect state from ViewModels
- Retrieves ViewModels via `metroViewModel()` composable
- Receives navigator classes from `aktual-app:nav` (e.g., `BackNavigator`, `SettingsNavigator`) as composable parameters
- Each UI module contributes a `NavEntryContributor` via `@ContributesIntoSet(NavScope::class)` to register its nav entries
- Dependencies: VM layer (API), Core UI, L10n, Nav API (for navigators/routes/extensions)

### Dependency Injection (Metro)

The project uses **Metro** (by Zac Sweers), a modern Kotlin-compiler-based DI framework:

**Scopes:**
- `AppScope` - Application-level singletons
- `NavScope` - Navigation entry contributors (child of `AppScope`)
- `ViewModelScope` - ViewModel instances
- `BudgetScope` - Budget-specific instances (per-budget data)

**Core DI Structure:**
- `AppGraph` - Root DI graph, creates ViewModelGraph, BudgetGraph, and NavGraph
- `ViewModelGraph` - Provides ViewModels via multibinding map
- `NavGraph` - `@GraphExtension(NavScope::class)`, provides `Set<NavEntryContributor>`
- `BudgetGraph` - Budget-scoped dependencies

**ViewModel Registration:**
```kotlin
@ViewModelKey
@ContributesIntoMap(ViewModelScope::class)
class YourViewModel(...) : ViewModel()
```

**Assisted Injection for runtime parameters:**
```kotlin
@AssistedInject
class YourViewModel(
  @Assisted private val runtimeParam: Type,
  // ... injected dependencies
) : ViewModel()

@AssistedFactory
@ManualViewModelAssistedFactoryKey
@ContributesIntoMap(ViewModelScope::class)
fun interface Factory : ManualViewModelAssistedFactory {
  fun create(runtimeParam: Type): YourViewModel
}
```

**Retrieving ViewModels in Compose:**
```kotlin
@Composable
fun YourScreen(viewModel: YourViewModel = metroViewModel()) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  // UI composition
}
```

### Navigation

See the [aktual-app:nav](aktual-app/nav/CLAUDE.md) module for details.

### Dependency Helper Functions
```kotlin
kotlin {
  commonMainDependencies {
    implementation(libs["some.common.lib"])
  }

  jvmMainDependencies {
    implementation(compose.desktop.currentOs)
  }

  androidMainDependencies {
    implementation(libs["androidx.core.ktx"])
  }

  androidHostTestDependencies {
    implementation(libs["robolectric"])
  }

  jvmTestDependencies {
    implementation(libs["some.test.lib"])
  }
}
```

### Build System

**Convention Plugins** (`/build-logic`):
The project uses Gradle convention plugins to centralize build configuration:

**Module Plugins** (use these when creating new modules):
- `aktual.module.kotlin` - Base KMP module (desktop + android targets)
- `aktual.module.compose` - Multiplatform + Compose + Material3
- `aktual.module.viewmodel` - Compose + Molecule + lifecycle-viewmodel
- `aktual.module.di` - Multiplatform + Metro DI
- `aktual.module.jvm` - JVM-only module

**Key Build Properties:**
- Java version: Sourced from `.java-version` file
- Min SDK: 28
- Target/Compile SDK: 36
- KSP2 enabled
- Configuration cache: enabled and optimized

**Java Version Management:**
The Java version is managed in a single source of truth: the `.java-version` file at the repository root.

- **Gradle Build**: Uses a custom `JavaVersionValueSource` that reads from `.java-version` via Project extension functions `javaVersion()` and `jvmTarget()`
- **GitHub Actions**: Workflows use `java-version-file: .java-version` in the `setup-java` action
- **Configuration Cache**: The ValueSource is properly annotated with `@InputFile` and `@PathSensitive(NONE)` for optimal caching behavior
- **Validation**: The ValueSource validates that the file exists, is not empty, and contains a valid integer

To change the Java version, simply update the `.java-version` file. Both the Gradle build and CI will automatically pick up the change.

### Key Technologies

**Core Stack:**
- Kotlin Multiplatform (2.2.21)
- Jetpack Compose (1.9.4) with Material3
- Kotlin Coroutines & Flow (1.10.2)
- Lifecycle ViewModel

**Specialized Libraries:**
- **Molecule** - Reactive composition in ViewModels (alternative to MVI frameworks)
- **Metro** - KSP-based dependency injection
- **SQLDelight** (2.2.0-SNAPSHOT) - Type-safe SQL database
- **Ktor** (3.3.1) - HTTP client
- **Kotlinx Serialization** - JSON serialization
- **Navigation 3** - Type-safe navigation with user-owned back stack

**Testing:**
- **AssertK** - Fluent assertions
- **Turbine** - Flow testing
- **MockK** - Mocking framework
- **Burst** - Parameterized tests
- **Robolectric** - Android unit tests without emulator

## Development Patterns

### Creating a New Feature Module

1. **Create module directories:**
   ```
   /modules/yourfeature/domain
   /modules/yourfeature/vm
   /modules/yourfeature/ui
   ```

2. **Add to `settings.gradle.kts`:**
   ```kotlin
   module("yourfeature:domain")
   module("yourfeature:ui")
   module("yourfeature:vm")
   ```

3. **Create `build.gradle.kts` for each module:**
   ```kotlin
   // domain/build.gradle.kts
   plugins {
     id("aktual.module.kotlin")
   }

   // vm/build.gradle.kts
   plugins {
     id("aktual.module.viewmodel")
   }

   // ui/build.gradle.kts
   plugins {
     id("aktual.module.compose")
   }
   ```

4. **Set up dependencies:**
   - Domain: Depend on core models, API clients
   - VM: Depend on domain (API), core DI (API)
   - UI: Depend on VM (API), core UI (API), L10n, `:aktual-app:nav` (for routes/extensions)

5. **Create a navigator class + route** in `aktual-app:nav`:
   ```kotlin
   // YourNavigator.kt
   @Immutable
   class YourNavigator(private val stack: AktualNavStack) {
     operator fun invoke(param: Param) = stack.push(YourNavRoute(param))
   }

   @Immutable @Serializable data class YourNavRoute(val param: Param) : NavKey
   ```

6. **Create a `NavEntryContributor`** in the UI module to register screens:
   ```kotlin
   @ContributesIntoSet(NavScope::class)
   class YourFeatureNavEntryContributor : NavEntryContributor {
     override fun contribute(scope: EntryProviderScope<NavKey>, stack: AktualNavStack) {
       scope.entry<YourNavRoute> { route -> YourScreen(BackNavigator(stack), route.param) }
     }
   }
   ```

7. **Add the UI module** to `aktual-app:di/build.gradle.kts` so Metro discovers the contribution hints.

### Testing Patterns

**ViewModel tests** use Turbine for Flow testing:
```kotlin
@Test
fun `test state changes`() = runTest {
  viewModel.stateFlow.test {
    viewModel.onAction()
    assertEquals(expectedState, awaitItem())
  }
}
```

**Robolectric** is used for Android unit tests - ViewModels and other Android-dependent code can be tested without an emulator.

### Localization (L10n)

See the [aktual-core:l10n](aktual-core/l10n/CLAUDE.md) module for details.

### Icons

See the [aktual-core:icons](aktual-core/icons/CLAUDE.md) module for details.

### Module Dependencies

**Visibility:**
- Use `api()` for dependencies that leak into the module's public API
- Use `implementation()` for internal dependencies
- Test modules are auto-included by convention plugins

**Typical dependency patterns:**
- UI → VM (api) → Domain (api) → Core Models (api)
- All modules can depend on: Core UI, L10n, Logging

To see a full picture of dependencies between modules, see the `chart.dot` files in each module's root directory.

## Important Notes

- **Configuration Cache Optimization**: The build is optimized for Gradle's configuration cache to achieve sub-second build times. Git version tracking intentionally does NOT invalidate the cache on commits to maintain fast iteration. Use `./gradlew --rerun-tasks` when you need fresh git version values.

- **Gradle Best Practices**: Convention plugins use lazy APIs (`configureEach`, `matching`) instead of eager APIs (`all`, `forEach`) to avoid unnecessary configuration overhead and maintain configuration cache compatibility.

- **Molecule Usage**: ViewModels use Molecule's `launchMolecule` for reactive composition. This is an alternative to traditional MVI patterns - state is composed reactively using Compose-style code inside ViewModels.

- **Metro vs Dagger/Hilt**: This project uses Metro instead of Dagger/Hilt for better KMP support and faster build times with KSP.

- **SQLDelight Snapshots**: The project uses SQLDelight snapshot versions for latest KMP features. Check `/gradle/libs.versions.toml` for current version.

- **Type Safety**: Navigation routes, DI keys, SQL queries, and JSON serialization are all type-safe via code generation.

- **Multiplatform First**: Follow this hierarchy when choosing where to place code:
  1. `commonMain` - Default choice for pure Kotlin code
  2. `androidMain` / `jvmMain` - Only when truly platform-specific

## Debugging

### Proguard Desktop

`:aktual-app:desktop` is built with Proguard in release builds. If we need to investigate, the mapping file is output to `aktual-app/desktop/build/outputs/mapping.txt`.

### R8 Android

`:aktual-app:android` is built with R8.
