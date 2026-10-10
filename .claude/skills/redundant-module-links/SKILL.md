---
name: redundant-module-links
description: Find and remove module dependencies that are already exposed through another dependency's `api` chain. Run as part of post-change validation whenever a module's project dependencies have changed.
argument-hint: "[--report-only]"
---

Find `project(...)` dependencies in `build.gradle.kts` files that are redundant because another direct dependency already exposes the same module through `api` links.

## When to run

After any change that adds, removes or switches (`api` <-> `implementation`) a `project(...)` dependency, alongside `compile.sh` and `ktfmt.sh`.

## Process

### 1. Refresh the atlas files

The script reads `**/build/atlas/project-links.json`, which only reflect the build files as of the last atlas run. Always regenerate first, through the `gradle-runner` agent:

```bash
./gradlew atlasGenerate
```

### 2. Report

```bash
python3 .claude/skills/redundant-module-links/redundant_links.py
```

Each line shows the build file line and the `api` chain that already exposes the dependency. If `$ARGUMENTS` contains `--report-only`, stop here and summarise.

### 3. Remove

```bash
python3 .claude/skills/redundant-module-links/redundant_links.py --apply
```

Then check no dependency block was left empty, and remove it if so.

### 4. Validate

Through the `gradle-runner` agent:

```bash
./gradlew compileAll --continue
./gradlew atlasGenerate
```

Use the full `compileAll` here, not `compile.sh`, since a removed `api` link can break a module that wasn't itself changed. The second `atlasGenerate` updates the README chart links. Finish with `./scripts/ktfmt.sh check`.

If a module fails to compile, restore that one line and say which.

## Rules the script applies

- `A -> C` is redundant when `A` has another direct dependency `B` that reaches `C` through `api` links only.
- If `A -> C` is itself `api`, `B` must also be an `api` dependency of `A`. Otherwise removing it would stop `A` re-exporting `C`.
- `androidMain` links don't count as a route, since they expose nothing to desktop.
- Links not written in the module's build file come from a convention plugin (e.g. `di:core` from `ModuleViewModel.kt`). They're listed but never removed.
- Only main source sets are covered. Atlas doesn't record test dependencies.

## Notes

- All reported links can be removed together. Removing a redundant link never breaks the chain another removal relies on.
- Some routes are incidental, e.g. `about:vm` getting `budget` through `di:runlevel > di:graphs`. Mention these in the summary so the explicit link can be kept if preferred.
