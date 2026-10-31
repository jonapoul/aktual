---
name: redundant-module-links
description: Find `api` module dependencies that should be `implementation`, and remove dependencies already exposed through another dependency's `api` chain. Run as part of post-change validation whenever a module's project dependencies have changed.
argument-hint: "[--report-only]"
---

Two checks on `project(...)` dependencies in `build.gradle.kts` files:

- **Unexposed `api`**: an `api` dependency whose types don't appear in the module's public API, so it can be `implementation`.
- **Redundant**: a dependency that another direct dependency already exposes through `api` links.

Run the unexposed check first, since flipping `api` links changes which chains exist.

## When to run

After any change that adds, removes or switches (`api` <-> `implementation`) a `project(...)` dependency, or changes a public signature, alongside `compile.sh` and `ktfmt.sh`.

## Process

### 1. Refresh the inputs

Both scripts read `**/build/atlas/project-links.json`, which only reflect the build files as of the last atlas run. The unexposed check also reads compiled main classes. Regenerate both first, through the `gradle-runner` agent:

```bash
./gradlew compileAll --continue
./gradlew atlasGenerate
```

### 2. Report unexposed `api` links

```bash
python3 .claude/skills/redundant-module-links/unexposed_api.py
```

It runs `Exposed.java` (plain `java` with ASM, kotlin-metadata-jvm and kotlin-stdlib from the Gradle cache) to read Kotlin metadata from every module's compiled classes, and lists which project modules each module's public API uses. An `api` link is reported when nothing from the dependency, or from anything it re-exports through `api`, appears in the public API. Modules are processed bottom-up, so a flip lower down is taken into account above it.

### 3. Report redundant links

```bash
python3 .claude/skills/redundant-module-links/redundant_links.py
```

Each line shows the build file line and the `api` chain that already exposes the dependency. If `$ARGUMENTS` contains `--report-only`, stop here and summarise both reports.

### 4. Apply

Flip the unexposed links first, then fix what breaks before removing redundant links:

```bash
python3 .claude/skills/redundant-module-links/unexposed_api.py --apply
./gradlew sortDependencies
./gradlew compileAll --continue
```

A flipped link stops reaching modules that used it through the old `api` chain. For each "Unresolved reference" in another module, add `implementation(project(...))` for the module that owns the type to that module's build file, in the right source set (`commonTestDependencies` for test errors). Don't flip the link back. Watch for Metro `MissingBinding` errors too: a module that builds a graph (`aktual-app:*`, `aktual-test:smoke`) must depend on every module that contributes bindings, directly or through `api`. The smoke tests depend on the VM modules directly, so they won't catch a VM that dropped out of the app graphs. The script guards against that, but launch the app if in doubt.

Then regenerate atlas and remove redundant links:

```bash
./gradlew atlasGenerate
python3 .claude/skills/redundant-module-links/redundant_links.py --apply
```

Check no dependency block was left empty, and remove it if so.

### 5. Validate

Through the `gradle-runner` agent:

```bash
./gradlew sortDependencies
./gradlew compileAll --continue
./gradlew atlasGenerate
./gradlew :aktual-test:smoke:testAll
```

Use the full `compileAll` here, not `compile.sh`, since a changed `api` link can break a module that wasn't itself changed. The smoke tests catch missing graph bindings at runtime. The final `atlasGenerate` updates the README chart links. Finish with `./scripts/ktfmt.sh check`.

If a module fails to compile after removing a redundant link, restore that one line and say which.

## Rules the scripts apply

Unexposed `api`:

- The public API is every public or protected declaration in a public class or file: supertypes, type parameter bounds, constructors, functions, properties and typealiases, including generic arguments and value class types.
- Metro-generated classes (`$MetroFactory`, `$MetroContribution*`, `metro/hints`) and serializers are ignored. They're always public and expose every constructor param, but only graph code uses them.
- Annotation arguments don't count, e.g. `@ContributesBinding(AppScope::class)` doesn't need `di:core` as `api`.
- `aktual-app:*` modules are skipped, since `api` is fine at the top level.
- A link stays `api` if flipping it would take a module with Metro contributions (anything with `metro/hints` classes, e.g. a VM module) off the compile classpath of `aktual-app:android` or `aktual-app:desktop`. Metro only aggregates contributions it can see there, and a missing one isn't a compile error: it fails at runtime, e.g. `Unknown model class ...ViewModel` from `MetroViewModelFactory`. The report lists these as kept. This is why UI -> VM links stay `api`.
- Links added by a convention plugin are never changed.

Redundant:

- `A -> C` is redundant when `A` has another direct dependency `B` that reaches `C` through `api` links only.
- If `A -> C` is itself `api`, `B` must also be an `api` dependency of `A`. Otherwise removing it would stop `A` re-exporting `C`.
- `androidMain` links don't count as a route, since they expose nothing to desktop.
- Links not written in the module's build file come from a convention plugin (e.g. `di:core` from `ModuleViewModel.kt`). They're listed but never removed.

Both checks cover main source sets only. Atlas doesn't record test dependencies, so test source sets can still break after a flip.

## Notes

- All reported redundant links can be removed together. Removing a redundant link never breaks the chain another removal relies on.
- Some routes are incidental, e.g. `about:vm` getting `budget` through `di:runlevel > di:graphs`, or `list:ui` getting `core:ui` through `sync:ui`. Mention these in the summary so the explicit link can be kept if preferred.
