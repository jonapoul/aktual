# CLAUDE.md

Guidance for Claude Code when working in this repo.

## Project

Aktual is an **unofficial** Kotlin Multiplatform client for [Actual personal budgeting software](https://github.com/actualbudget/actual), targeting Android and Desktop (JVM). Pet project, unaffiliated with upstream.

## General

- **IMPORTANT**: After any architectural/structural change, grep `**/CLAUDE.md` for references that need updating (scopes, annotations, module paths, DI patterns). Don't wait to be asked.
- Prefer `kotlinx.immutable` collections in the UI layer, not plain `List`/`Set`.
- Prefer `stateFlow.update { x }` over `stateFlow.value = x`.
- Wrap comments to the `max_line_length` in `.editorconfig` (currently 120).

## Build commands

```bash
# Compile (including test sources) for one module — default choice for module changes
./gradlew :[module]:compileAll

# Compile everything — prefer module-specific form where possible
./gradlew compileAll

# Full build — very slow, don't run without asking
./gradlew build

# Apps
./gradlew :aktual-app:android:assemble
./gradlew :aktual-app:desktop:assemble
./gradlew :aktual-app:android:installDebug
./gradlew :aktual-app:desktop:run

# Tests — always module-specific. `./gradlew allTests` pegs the machine; don't run it.
./gradlew :aktual-account:vm:test

# Formatting
./scripts/ktfmt.sh            # format files changed since main
./scripts/ktfmt.sh check      # check-only
./scripts/ktfmt.sh --force    # all files

# Dependency graph — rerun only when module deps change
./gradlew atlasGenerate

# Force-refresh git version info (bypasses config cache)
./gradlew --rerun-tasks
```

Don't run detekt locally (user handles it); `./scripts/detekt.sh` covers changed modules if asked.

## Architecture

Feature-based modular layout. Module list: `settings.gradle.kts` / `ls modules/`. Features split into `domain` / `vm` / `ui` modules. Shared infra lives under `modules/{core,api,prefs,budget,test}`.

### Layers
- **Domain** — pure Kotlin, no UI. Depends on core models, API clients, prefs.
- **VM** — Molecule's `launchMolecule` drives state; exposes `StateFlow`. Registered with `@ViewModelKey` + `@ContributesIntoMap(AppScope::class)`.
- **UI** — stateless Compose. Retrieves VMs via `metroViewModel()`. Receives navigator instances (e.g. `BackNavigator`) from its `NavEntryContributor`.

### Metro DI

Scopes: `AppScope` (app singletons), `NavScope` (child of App; nav entries), `ViewModelScope`, `BudgetScope` (per-budget).

Graphs: `AppGraph` is root and creates `ViewModelGraph`, `BudgetGraph`, `NavGraph` (a `@GraphExtension(NavScope::class)` exposing `Set<NavEntryContributor>`).

Assisted VM factories use `@ManualViewModelAssistedFactoryKey` + `@ContributesIntoMap(ViewModelScope::class)` — copy the shape from an existing one.

### Navigation

See [aktual-app:nav](aktual-app/nav/CLAUDE.md).

## Creating a new feature module

1. Add `modules/<feature>/{domain,vm,ui}` and register in `settings.gradle.kts` with `module("<feature>:...")`.
1. Apply the right module plugin per layer — `aktual.module.kotlin` (domain), `aktual.module.viewmodel` (vm), `aktual.module.compose` (ui). See [gradle/build-logic](gradle/build-logic/CLAUDE.md) for what each plugin sets up.
1. Dependencies: UI → VM (api) → Domain (api) → core models. Any module can depend on core UI / L10n / logging.
1. Create a navigator + `NavKey` in `aktual-app:nav` (see that module's CLAUDE.md).
1. Implement `NavEntryContributor` in the `:ui` module with `@ContributesIntoSet(NavScope::class)`.
1. Add the `:ui` module to `aktual-app:di/build.gradle.kts` so Metro finds contribution hints.

## Dependency helpers

Inside `kotlin { ... }`: `commonMainDependencies`, `jvmMainDependencies`, `androidMainDependencies`, `androidHostTestDependencies`, `jvmTestDependencies`. Use `api()` only for deps that leak into the public API.

## Multiplatform

Default new code to `commonMain`; reach for `androidMain` / `jvmMain` only when truly platform-specific.

## Gotchas

- **Config cache + git versions**: `gitVersionHash()` / `gitVersionCode()` are cached so builds stay sub-second — they do **not** refresh on commit. Use `./gradlew --rerun-tasks` when you need fresh values.
- **Java version**: single source of truth is `.java-version` at repo root (read by `JavaVersionValueSource` and CI's `setup-java`). Don't hardcode it anywhere.
- **SDK**: min 28, target/compile 36.
- **Desktop release** goes through Proguard — mapping at `aktual-app/desktop/build/outputs/mapping.txt`. Android goes through R8.
- **SQLDelight** is pinned to a snapshot for KMP features; see `gradle/libs.versions.toml`.

## Sub-CLAUDE.mds

- [aktual-app/nav](aktual-app/nav/CLAUDE.md) — navigation architecture
- [aktual-core/l10n](aktual-core/l10n/CLAUDE.md) — localization
- [aktual-core/icons](aktual-core/icons/CLAUDE.md) — custom + Material icons
- [aktual-prefs](aktual-prefs/CLAUDE.md) — adding a new setting
- [aktual-test/api](aktual-test/api/CLAUDE.md) — test HTTP mocks + generated fixtures
- [aktual-test/smoke](aktual-test/smoke/CLAUDE.md) — VM DI smoke tests
- [gradle/build-logic](gradle/build-logic/CLAUDE.md) — convention plugins
