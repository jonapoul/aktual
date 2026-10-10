# CLAUDE.md

Guidance for Claude Code when working in this repo.

## Project

Aktual is an **unofficial** Kotlin Multiplatform client for [Actual personal budgeting software](https://github.com/actualbudget/actual), targeting Android and Desktop (JVM). Pet project, unaffiliated with upstream.


## General

- **IMPORTANT**: After any architectural/structural change, grep `**/CLAUDE.md` for references that need updating (scopes, annotations, module paths, DI patterns). Don't wait to be asked.
- Limit visibility where possible: make a type `internal` unless another module uses it. A public VM that injects internal types needs an `internal constructor`.
- Prefer `kotlinx.immutable` collections in the UI layer, not plain `List`/`Set`.
- Prefer `stateFlow.update { x }` over `stateFlow.value = x`.
- The project compiles with `-Xcontext-sensitive-resolution` (CSR), so enum entries, sealed subtypes and companion properties of the expected type (`Role.Button`, `Alignment.Center`) can be referenced unqualified when the expected type is known. Always do this instead of qualifying (`PossibleRole.Admin`) or importing the entry. The expected type is known in:
  - function/constructor arguments, named or positional: `SideNavRail(selectedTab = Transactions)`
  - `when` branches and `==`/`!=` comparisons: `when (method) { Header -> ... }`, `if (tab == Accounts)`
  - assertions: `assertThat(role).isEqualTo(Admin)`
  - typed property/variable assignments, default parameter values and return values of declared-type functions

  The `RedundantQualifier` detekt rule flags qualifiers that can be dropped.

  Qualify only where the name is ambiguous with a type or function in scope, as `LoginMethod.Password` is with the `Password` class and `Role.Checkbox` is with the `Checkbox` composable, or where there is no declared type, as in `override val role = Role.Admin`. Constructor calls of sealed subtypes don't resolve through CSR either, so write `ScheduleDate.Once(date)`, not `Once(date)`.
- `Strings.xyz` (user-facing text) is generated from XML in `aktual-core:l10n` - add the string there and regenerate, don't hardcode. See [aktual-core/l10n](aktual-core/l10n/CLAUDE.md).
- In tests, observe `Flow`/`StateFlow` emissions with Turbine (`flow.test { awaitItem() }`), not by reading `.value` or manual collectors.
- Wrap comments at 100 columns. That is where `ktfmt --google-style` wraps them, not the `max_line_length` of 120 in `.editorconfig`.

## Build commands

```bash
# Compile (including test sources) for one module - default choice for module changes
./gradlew :[module]:compileAll

# Compile everything - prefer module-specific form where possible
./gradlew compileAll

# Full build - very slow, don't run without asking
./gradlew build

# Build and launch apps
./gradlew :aktual-app:desktop:run
./gradlew :aktual-app:android:installDebug && adb shell am start -n dev.jonpoulton.aktual.app.dev/aktual.app.android.AktualActivity

# Tests - always module-specific. `./gradlew allTests` pegs the machine; don't run it.
# Never pass a bare `:module:test` - it's ambiguous for KMP modules. Use `testAll` (both
# targets), `testAndroidHostTest`, or `desktopTest`; run the `run-module-tests` skill to pick.
./gradlew :aktual-account:vm:testAll

# Formatting
./scripts/ktfmt.sh            # format files changed since main
./scripts/ktfmt.sh check      # check-only
./scripts/ktfmt.sh --force    # all files

# Run a Gradle task on only the modules with changes since main (append --dry-run to preview, --force on
# detekt.sh/lint.sh/compile.sh to run on all modules).
# A change to the root build file, .github/actions/ or build-logic/ runs all modules. A libs.versions.toml change only
# runs the modules using the changed entries, unless build-logic uses them. Detekt uses the narrower
# scripts/lib/.global-triggers-detekt.
./scripts/detekt.sh           # detektCheck on changed modules
./scripts/lint.sh             # lint on changed modules (quite heavy, don't run unless needed)
./scripts/compile.sh          # compileAll on changed modules
./scripts/test.sh             # testAll on changed modules

# Dependency graph - rerun only when module deps change, then run the `redundant-module-links` skill
./gradlew atlasGenerate

# Force-refresh git version info (bypasses config cache)
./gradlew --rerun-tasks
```

Run `./scripts/detekt.sh` before opening a PR and fix what it reports. `lint.sh`, `test.sh` and `compile.sh` share the same change-detection logic (extracted into `scripts/lib/`).

## Architecture

Feature-based modular layout. Module list: `settings.gradle.kts` / `ls modules/`. Features split into `domain` / `vm` / `ui` modules. Shared infra lives under `modules/{core,api,prefs,budget,test}`.

### Layers
- **Domain** - pure Kotlin, no UI. Depends on core models, API clients, prefs.
- **VM** - Molecule's `launchMolecule` drives state; exposes `StateFlow`. Registered with `@ViewModelKey` + `@ContributesIntoMap(AppScope::class)`.
- **UI** - stateless Compose. Retrieves VMs via `metroViewModel()`. Receives navigator instances (e.g. `BackNavigator`) from its `NavEntryContributor`.

### Metro DI

Scopes: `AppScope` (app singletons), `ServerChosenScope` (after server URL is set), `LoggedInScope` (after authentication), `BudgetScope` (per-budget). All defined in `aktual-di:scopes`.

Graphs: All graphs implement `AktualGraph`. Hierarchy: `AppGraph` (root) → `ServerChosenGraph` → `LoggedInGraph`, plus `AppGraph` → `BudgetGraph`. `BudgetGraph` hangs off the app graph so budgets can open without a server (the demo budget), so it can't see server-chosen/logged-in bindings - server details come through the `BudgetServer` it's created with (`Remote(url, token)` or `None`). Open run levels are still ordered app → server-chosen → logged-in → budget, and the VM factory merges every open level's VMs. Graph types live in `aktual-di:graphs`.

Graph lifecycle hooks (`Closeable` / `Initializable` set contributions) must be qualified with the contributing scope, e.g. `binding<@ForScope(BudgetScope::class) Closeable>()`. Unqualified, child graphs inherit parent contributions, so closing a child would close its parents' resources too.

Each scope has its own `CoroutineScope`, cancelled when its graph closes. Inject `AppCoroutineScope` / `ServerChosenCoroutineScope` / `LoggedInCoroutineScope` / `BudgetCoroutineScope`, matching the lifetime the work should have. A plain `CoroutineScope` isn't bound, and the `InjectedRawCoroutineScope` detekt rule flags injecting one.

VMs are registered with `@ViewModelKey` + `@ContributesIntoMap(<Scope>::class)` where the scope is the narrowest graph that provides all the VM's dependencies (`AppScope` → `ServerChosenScope` → `LoggedInScope`, or `BudgetScope` for anything needing the open budget). Assisted VM factories use `@ManualViewModelAssistedFactoryKey` + `@ContributesIntoMap(<Scope>::class)` - copy the shape from an existing one.

### Navigation

See [aktual-app:nav](aktual-app/nav/CLAUDE.md).

## Creating a new feature module

1. Add `modules/<feature>/{domain,vm,ui}` and register in `settings.gradle.kts` with `module("<feature>:...")`.
1. Apply the right module plugin per layer - `aktual.module.kotlin` (domain), `aktual.module.viewmodel` (vm), `aktual.module.compose` (ui). See [build-logic](gradle/build-logic/CLAUDE.md) for what each plugin sets up.
1. Dependencies: UI → VM (api) → Domain (api) → core models. Any module can depend on core UI / L10n / logging.
1. Create a navigator + `NavKey` in `aktual-core:nav` (see `aktual-app/nav/CLAUDE.md`).
1. Implement `NavEntryContributor` in the `:ui` module with `@ContributesIntoSet(AppScope::class)` (or `BudgetNavEntryContributor` with `@ContributesIntoSet(BudgetScope::class)` for budget screens).
1. Add the `:ui` module to `aktual-app:ui-app/build.gradle.kts` (or `aktual-app:ui-budget/build.gradle.kts` for budget screens) so Metro finds contribution hints.

## Dependency helpers

Inside `kotlin { ... }`: `commonMainDependencies`, `desktopMainDependencies`, `androidMainDependencies`, `androidHostTestDependencies`, `desktopTestDependencies`. Use `api()` only for deps that leak into the public API.

## Multiplatform

Default new code to `commonMain`; reach for `androidMain` / `desktopMain` only when truly platform-specific.

## Gotchas

- **Config cache + git versions**: `gitVersionHash()` / `gitVersionCode()` are cached so builds stay sub-second - they do **not** refresh on commit. Use `./gradlew --rerun-tasks` when you need fresh values.
- **Java version**: single source of truth is `.java-version` at repo root (read by `JavaVersionValueSource` and CI's `setup-java`). Don't hardcode it anywhere.
- **SDK**: min 28, target/compile 37.
- **Desktop release** goes through Proguard - mapping at `aktual-app/desktop/build/outputs/mapping.txt`. Android goes through R8.
- **Cloud sessions**: `.claude/hooks/session-start.sh` (Claude Code on the web only) points Gradle at Google's Maven Central mirror via `~/.gradle/init.d`, since Maven Central rate-limits those sessions with 429s, and installs an Android SDK at `~/android-sdk`. The environment must allow `dl.google.com`.
- **Android manifest lock**: `aktual-app/android` locks its merged manifest (`AndroidManifest.lock.yaml`) via the manifest-lock plugin with `failOnLockChange = true`, so adding/changing an Android dependency that touches the manifest (permissions, components) fails the build until the lock is regenerated. Run `./gradlew :aktual-app:android:androidManifestLock` to update it, then commit the change.

## Sub-CLAUDE.mds

- [aktual-app/nav](aktual-app/nav/CLAUDE.md) - navigation architecture
- [aktual-core/l10n](aktual-core/l10n/CLAUDE.md) - localization
- [aktual-core/icons](aktual-core/icons/CLAUDE.md) - custom + Material icons
- [aktual-prefs](aktual-prefs/CLAUDE.md) - adding a new setting
- [aktual-test/api](aktual-test/api/CLAUDE.md) - test HTTP mocks + generated fixtures
- [aktual-test/smoke](aktual-test/smoke/CLAUDE.md) - VM DI smoke tests
- [aktual-budget/data/db](aktual-budget/data/db/CLAUDE.md) - database schema, migrations, adapters
- [gradle/build-logic](build-logic/CLAUDE.md) - convention plugins
- [detekt-rules](detekt-rules/CLAUDE.md) - custom detekt rules
