---
name: run-module-tests
description: Identify and run the correct Gradle test task for a given module based on its source sets and plugin type.
argument-hint: "<module-path>"
---

Run the correct test task(s) for a Gradle module in this KMP project.

## Arguments

- `$ARGUMENTS` should be the Gradle module path (e.g., `aktual-api`, `aktual-account:vm`, `aktual-app:desktop`)
  - If omitted, infer it from the most recently discussed or edited module in conversation context

## Step-by-Step Process

### 1. Determine what test source sets exist

Check which test source set directories exist for the module under `src/`:

```
src/commonTest/     -> shared tests (run by desktopTest, and by testAndroidHostTest if the module has an android target)
src/androidHostTest/ -> Android-specific tests (runs on host JVM, Robolectric where a class opts in)
src/desktopTest/    -> JVM/Desktop-specific tests
src/test/           -> Standard JVM test (JVM-only modules)
```

Use Glob to check: `<module-path>/src/*test*/` and `<module-path>/src/*Test*/`

The module path on disk uses `/` separators (e.g., `aktual-core/api/src/commonTest`), but the Gradle path uses `:` (e.g., `:aktual-api`).

### 2. Determine the module type

Check the module's `build.gradle.kts` for which convention plugin it uses:

| Plugin | Module Type | Available Test Tasks |
|--------|------------|---------------------|
| `aktual.module.kotlin` / `aktual.module.di` without `aktual.module.kmp-android` | KMP, JVM target only | `desktopTest` |
| `aktual.module.kotlin` / `aktual.module.di` with `aktual.module.kmp-android` | KMP + Android | `allTests`, `testAndroidHostTest`, `desktopTest` |
| `aktual.module.compose` | KMP + Android + Compose | `allTests`, `testAndroidHostTest`, `desktopTest` |
| `aktual.module.viewmodel` | KMP + Android + Compose + VM | `allTests`, `testAndroidHostTest`, `desktopTest` |
| `aktual.module.jvm` | Plain JVM, not KMP | `test` |
| `aktual.module.android` | Plain Android library, not KMP | `testDebugUnitTest` |

### 3. Choose the right task

For **KMP modules without an android target**: always use `desktopTest`.

For **KMP modules with an android target** (compose/viewmodel, or `aktual.module.kmp-android` applied):

| Source Sets Present | Recommended Task | Why |
|-------------------|-----------------|-----|
| Only `commonTest` | `testAndroidHostTest` | commonTest is compiled into both androidHostTest and desktopTest; pick one to avoid running twice. androidHostTest is preferred as it includes Robolectric support. |
| Only `androidHostTest` (with or without `commonTest`) | `testAndroidHostTest` | Runs commonTest + androidHostTest together |
| Only `desktopTest` (with or without `commonTest`) | `desktopTest` | Runs commonTest + desktopTest together |
| Both `androidHostTest` and `desktopTest` | `allTests` | Need both targets to cover all tests |
| `commonTest` + `androidHostTest` + `desktopTest` | `allTests` | Need both targets |

For **JVM-only modules** (`aktual.module.jvm`):
- Always use `test`

### 4. Run the test

Execute the Gradle task:

```bash
./gradlew :<gradle-module-path>:<task>
```

For example:
- `./gradlew :aktual-api:testAndroidHostTest`
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
- In a module with an android target where only `commonTest` exists, prefer `testAndroidHostTest` over `desktopTest` since it has Robolectric available for any Android-dependent code
- The `testAll` task (defined in ConventionTest.kt) is a convenience alias that depends on all `Test`-type tasks in the module - equivalent to `allTests` for KMP modules
