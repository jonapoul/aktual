# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Aktual is an **unofficial** Kotlin Multiplatform client for [Actual personal budgeting software](https://github.com/actualbudget/actual). The project supports Android and Desktop (JVM) platforms with shared Kotlin code. This is a pet project and not affiliated with the main Actual project.

## Build Commands

### Building
```bash
# Check that the project kotlin files compile. This should be the first choice before going to the following.
# This will compile all source sets, including tests.
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
# Run detekt static analysis
./gradlew detektCheck

# Format code
./scripts/ktlintFormat.sh

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
  /di                   # Application-level DI setup
  /nav                  # Navigation infrastructure

/modules
  /feature              # Feature modules follow: domain/ui/vm pattern
    /domain             # Business logic, use cases, repositories
    /ui                 # Compose UI screens and components
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
- Navigator interfaces for navigation actions
- Dependencies: VM layer (API), Core UI, L10n

### Dependency Injection (Metro)

The project uses **Metro** (by Zac Sweers), a modern KSP-based DI framework:

**Scopes:**
- `AppScope` - Application-level singletons
- `ViewModelScope` - ViewModel instances
- `BudgetScope` - Budget-specific instances (per-budget data)

**Core DI Structure:**
- `AppGraph` - Root DI graph, creates ViewModelGraph and BudgetGraph
- `ViewModelGraph` - Provides ViewModels via multibinding map
- `BudgetGraph` - Budget-scoped dependencies

**ViewModel Registration:**
```kotlin
@Inject
@ViewModelKey(YourViewModel::class)
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
@ManualViewModelAssistedFactoryKey(Factory::class)
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

Uses **Jetpack Navigation Compose** with type-safe routes defined in `/app/nav/`:

- Serializable route objects (e.g., `@Serializable data class TransactionsNavRoute(val token: LoginToken, val budgetId: BudgetId)`)
- Centralized `AktualNavHost` composable
- Navigator interfaces passed to screens for navigation actions
- Type-safe parameter passing with custom `NavType` implementations

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
- `aktual.module.multiplatform` - Base KMP module (desktop + android targets)
- `aktual.module.compose` - Multiplatform + Compose + Material3
- `aktual.module.viewmodel` - Compose + Molecule + lifecycle-viewmodel
- `aktual.module.di` - Multiplatform + Metro DI
- `aktual.module.android` - Android-only module
- `aktual.module.jvm` - JVM-only module

**Key Build Properties:**
- Java version: Sourced from `.java-version` file (currently 21)
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
- **Navigation Compose** - Type-safe navigation

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
     id("aktual.module.multiplatform")
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
   - UI: Depend on VM (API), core UI (API), L10n

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

### Code Generation

**KSP** is used for generating API requests, implemented in the `:aktual-codegen:ksp` module as a KSP processor. This generates implementations for creating KSP requests.

### Icons

Icons are stored in the `:aktual-core:icons` module at `aktual-core/icons/src/commonMain/kotlin/aktual/core/icons/`. There are two types of icons:

#### AktualIcons (Custom SVG Icons)

Custom icons for Aktual-specific graphics. Use these for custom designs not available in Material Icons.

**Creating a new Aktual icon:**

```kotlin
@file:Suppress("BooleanLiteralArgument", "UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.aktualIcon
import aktual.core.icons.internal.aktualPath
import androidx.compose.ui.graphics.vector.ImageVector

val AktualIcons.YourIcon: ImageVector by lazy {
  aktualIcon(name = "YourIcon", size = 20f) {
    aktualPath {
      // SVG path commands
      moveTo(x, y)
      lineTo(x, y)
      // ... etc
      close()
    }
  }
}
```

**Important:**
- Use `by lazy` delegate for initialization
- Use `aktualIcon()` with custom size (typically 20f or 24f, automatically prepends "Aktual." to name)
- Use `aktualPath {}` for path definitions
- Add to `AktualIconsProvider` list in `IconPreviews.kt`

#### MaterialIcons (Material Design Icons)

Material Design icons from the official Android/Compose Material library.

**Source Repository:**
- Material Icons: https://android.googlesource.com/platform//frameworks/support/+/1de65587b7e999a38df120bd8827c3594974864d/compose/material

**Creating a new Material icon:**

```kotlin
@file:Suppress("UnusedReceiverParameter")

package aktual.core.icons

import aktual.core.icons.internal.materialIcon
import aktual.core.icons.internal.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val MaterialIcons.YourIcon: ImageVector by lazy {
  materialIcon(name = "YourIcon") {
    materialPath {
      // SVG path commands converted from Material source
      moveTo(x, y)
      lineTo(x, y)
      close()
    }
  }
}
```

**For autoMirror support (RTL languages):**
```kotlin
val MaterialIcons.YourIcon: ImageVector by lazy {
  materialIcon(name = "YourIcon", autoMirror = true) {
    materialPath { /* ... */ }
  }
}
```

**Important:**
- Use `by lazy` delegate for initialization
- Use `materialIcon()` helper (24dp fixed size, automatically prepends "Material." to name)
- Use `materialPath {}` for path definitions
- Add to `MaterialIconsProvider` list in `IconPreviews.kt`

#### SVG Path Conversion Reference

- `M x y` → `moveTo(x.0f, y.0f)`
- `L x y` → `lineTo(x.0f, y.0f)`
- `l x y` → `lineToRelative(x.0f, y.0f)`
- `C x1 y1 x2 y2 x y` → `curveTo(x1.0f, y1.0f, x2.0f, y2.0f, x.0f, y.0f)`
- `c x1 y1 x2 y2 x y` → `curveToRelative(x1.0f, y1.0f, x2.0f, y2.0f, x.0f, y.0f)`
- `H x` → `horizontalLineTo(x.0f)`
- `h x` → `horizontalLineToRelative(x.0f)`
- `V y` → `verticalLineTo(y.0f)`
- `v y` → `verticalLineToRelative(y.0f)`
- `Z` → `close()`

#### Using Icons

```kotlin
import aktual.core.icons.AktualIcons
import aktual.core.icons.MaterialIcons
import aktual.core.icons.Cloud
import aktual.core.icons.Check

Icon(
  imageVector = AktualIcons.Cloud,
  contentDescription = "Cloud"
)

Icon(
  imageVector = MaterialIcons.Check,
  contentDescription = "Check"
)
```

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
