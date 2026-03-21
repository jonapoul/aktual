---
name: run-module-tests
description: Identify and run the correct Gradle test task for a given module based on its source sets and plugin type.
argument-hint: "<module-path>"
---

Run the correct test task(s) for a Gradle module in this KMP project.

## Arguments

- `$ARGUMENTS` should be the Gradle module path (e.g., `aktual-core:api`, `aktual-account:vm`, `aktual-app:desktop`)
  - If omitted, infer it from the most recently discussed or edited module in conversation context

## Step-by-Step Process

### 1. Determine what test source sets exist

Check which test source set directories exist for the module under `src/`:

```
src/commonTest/     -> shared tests (run by both androidHostTest and jvmTest tasks)
src/androidHostTest/ -> Android-specific tests (Robolectric, runs on host JVM)
src/jvmTest/        -> JVM/Desktop-specific tests
src/test/           -> Standard JVM test (JVM-only modules)
```

Use Glob to check: `<module-path>/src/*test*/` and `<module-path>/src/*Test*/`

The module path on disk uses `/` separators (e.g., `aktual-core/api/src/commonTest`), but the Gradle path uses `:` (e.g., `:aktual-core:api`).

### 2. Determine the module type

Check the module's `build.gradle.kts` for which convention plugin it uses:

| Plugin | Module Type | Available Test Tasks |
|--------|------------|---------------------|
| `aktual.module.kotlin` | KMP | `allTests`, `testAndroidHostTest`, `jvmTest` |
| `aktual.module.compose` | KMP + Compose | `allTests`, `testAndroidHostTest`, `jvmTest` |
| `aktual.module.viewmodel` | KMP + Compose + VM | `allTests`, `testAndroidHostTest`, `jvmTest` |
| `aktual.module.di` | KMP + DI | `allTests`, `testAndroidHostTest`, `jvmTest` |
| `aktual.module.jvm` | JVM-only | `test` |

### 3. Choose the right task

For **KMP modules** (kotlin/compose/viewmodel/di plugins):

| Source Sets Present | Recommended Task | Why |
|-------------------|-----------------|-----|
| Only `commonTest` | `testAndroidHostTest` | commonTest is compiled into both androidHostTest and jvmTest; pick one to avoid running twice. androidHostTest is preferred as it includes Robolectric support. |
| Only `androidHostTest` (with or without `commonTest`) | `testAndroidHostTest` | Runs commonTest + androidHostTest together |
| Only `jvmTest` (with or without `commonTest`) | `jvmTest` | Runs commonTest + jvmTest together |
| Both `androidHostTest` and `jvmTest` | `allTests` | Need both targets to cover all tests |
| `commonTest` + `androidHostTest` + `jvmTest` | `allTests` | Need both targets |

For **JVM-only modules** (`aktual.module.jvm`):
- Always use `test`

### 4. Run the test

Execute the Gradle task:

```bash
./gradlew :<gradle-module-path>:<task>
```

For example:
- `./gradlew :aktual-core:api:testAndroidHostTest`
- `./gradlew :aktual-account:vm:testAndroidHostTest`
- `./gradlew :aktual-budget:sync:vm:allTests`
- `./gradlew :aktual-app:desktop:test`

### 5. Report results

After running, report:
- Which task was run and why it was chosen
- Test results (passed/failed/skipped counts)
- Any failures with relevant details

## Important Notes

- NEVER run `allTests` or `testAll` at the project root level - this runs all tests across all modules and will overload the system
- When only `commonTest` exists, prefer `testAndroidHostTest` over `jvmTest` since it has Robolectric available for any Android-dependent code
- The `testAll` task (defined in ConventionTest.kt) is a convenience alias that depends on all `Test`-type tasks in the module - equivalent to `allTests` for KMP modules