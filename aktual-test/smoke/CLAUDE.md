# Smoke Tests

ViewModel DI smoke tests that verify all ViewModels can be instantiated through the Metro dependency graph without runtime errors.

## Purpose

Catches DI wiring breakages at compile/test time rather than at runtime. Each test creates a real `AppGraph` via `createDynamicGraph` and verifies that every registered ViewModel can be resolved and instantiated.

## Structure

- `ViewModelSmokeTest` (commonTest) — abstract base with all shared VM tests. Uses Burst's `@InterceptTest` for `TemporaryFolder` lifecycle.
- `JvmViewModelSmokeTest` (jvmTest) — desktop target, creates `TestJvmAppGraph`.
- `AndroidViewModelSmokeTest` (androidHostTest) — android target via Robolectric, creates `TestAndroidAppGraph`.
- `TestContainer` — `@BindingContainer` providing test-specific bindings (e.g. `BudgetFiles` with a temp directory).
- `TestAppGraph` — common interface extending `AppGraph`, exposing `BudgetGraph.Builder` and `BudgetGraphHolder`.

## Known Issues

### Robolectric `@Config` + Burst `@InterceptTest` compiler crash
Robolectric's `@Config` annotation cannot be used on the test class because Burst's `@InterceptTest` triggers a Kotlin compiler backend NPE during annotation lowering (`JvmAnnotationImplementationTransformer`). All `@Config` properties are in `robolectric.properties` instead.

### MetroAppComponentFactory + Robolectric SDK ordering
On SDK 29+, Robolectric creates providers/receivers before `Application.onCreate()`, so Metro's `appComponentFactoryBindings` isn't initialized. The test manifest replaces `android:appComponentFactory` with the base `AppComponentFactory` to work around this. See https://github.com/ZacSweers/metro/issues/1979.

### Robolectric SDK 35 `FileSystemAlreadyExistsException`
Robolectric's native runtime loader hits a zip filesystem conflict on SDK 35. The `AndroidViewModelSmokeTest.optionallySkip()` uses `assumeFalse` to skip the root VM test on that SDK level.

## Adding a New ViewModel

1. Add a `@Test` function in `ViewModelSmokeTest` using `testVm<YourViewModel>()` or `testAssistedVM<YourVM, YourVM.Factory> { create(...) }`.
2. Add the VM module as a `commonTestDependencies` implementation dependency in `build.gradle.kts`.
