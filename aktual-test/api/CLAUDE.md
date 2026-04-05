# aktual-test:api

## Overview

Test utilities and auto-generated API response fixtures for testing HTTP-layer code. This module provides two things:

1. **Generated response constants** — The build script reads JSON/HTML/CSS files from the top-level [`/api`](../../api/) directory and generates Kotlin `object` classes (via `buildconfig`) containing those file contents as string constants. These are used throughout test suites to provide realistic API responses without hardcoding JSON in tests.

2. **Mock HTTP helpers** — Utilities for setting up Ktor `MockEngine`-based HTTP clients in tests.

## How Response Generation Works

The build script scans `/api/actual/<subdirectory>/` and `/api/github/` and `/api/theme/`, creating a generated `*Responses` object for each directory:

- `/api/actual/account/*.json` → `AccountResponses` object
- `/api/actual/sync/*.json` → `SyncResponses` object
- `/api/github/*.json` → `GithubResponses` object
- `/api/theme/*.css`, `*.json` → `ThemeResponses` object

File names are converted to constant names: `login-success.200.json` → `LOGIN_SUCCESS_200`. Files with `.rest` extension or larger than 100KB are excluded.

**To add a new test fixture:** place a JSON (or other supported) file in the appropriate `/api/` subdirectory. The constant will be generated automatically on the next build.

## Key Files

- **`MockEngine.kt`** — Extension functions for Ktor's `MockEngine`: `respondJson()`, `emptyMockEngine()`, `clear()`, `latestRequest()`, `latestRequestHeaders()`, `latestRequestUrl()`, and queue-based response helpers.
- **`TestHttpClient.kt`** — `testHttpClient()` factory that builds a Ktor `HttpClient` using the project's real client configuration (`buildKtorClient`) but with a mock engine.
- **`TestHttpContainer.kt`** — Metro `@BindingContainer` for providing a mock `HttpClientEngine` in test DI graphs.
- **`PrettyJson.kt`** — A `Json` instance based on `AktualJson` with pretty-printing enabled, used as the default serializer in test clients.

## Usage

Depend on this module in your test source set:

```kotlin
commonTestDependencies {
  implementation(project(":aktual-test:api"))
}
```

Then in tests:

```kotlin
// Use a generated response constant
val engine = MockEngine { respondJson(AccountResponses.LOGIN_SUCCESS_200) }
val client = testHttpClient(engine)

// Or use the queue-based engine
val engine = emptyMockEngine()
engine.enqueueResponse(SyncResponses.LIST_USER_FILES_SUCCESS_200)
```

## Consumers

This module is used by: `aktual-account:domain`, `aktual-account:vm`, `aktual-budget:list:vm`, `aktual-budget:sync:vm`, `aktual-budget:model`, `aktual-core:api`, `aktual-core:api:impl`, `aktual-core:theme:impl`, and `aktual-about:data`.
