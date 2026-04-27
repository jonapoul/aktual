# aktual-test:api

Test utilities for HTTP-layer code. Two things:

1. **Generated response constants** — the build script reads files under the top-level [`/api`](../../api/) directory and emits `*Responses` `object`s whose constants are the file contents as strings.
1. **Mock HTTP helpers** — Ktor `MockEngine` utilities for test clients.

## Generated responses

The build scans per subdirectory and produces one `*Responses` object per:

- `/api/actual/account/*.json` → `AccountResponses`
- `/api/actual/sync/*.json` → `SyncResponses`
- `/api/github/*.json` → `GithubResponses`
- `/api/theme/*.{css,json}` → `ThemeResponses`

Filename → constant: `login-success.200.json` → `LOGIN_SUCCESS_200`. Files with `.rest` extension or `>100KB` are excluded.

**To add a fixture:** drop the file in the right `/api/` subdir — the constant appears on the next build.

## Helpers

- `MockEngine.kt` — `respondJson()`, `emptyMockEngine()`, `enqueueResponse()`, `clear()`, `latestRequest*()`.
- `TestHttpClient.kt` — `testHttpClient(engine)` wraps the real `buildKtorClient` config with a mock engine.
- `TestHttpContainer.kt` — Metro `@BindingContainer` providing a mock `HttpClientEngine` in test DI.
- `PrettyJson.kt` — `AktualJson` with pretty-print enabled.

## Usage

```kotlin
// in commonTestDependencies { implementation(project(":aktual-test:api")) }

val engine = MockEngine { respondJson(AccountResponses.LOGIN_SUCCESS_200) }
val client = testHttpClient(engine)

// or queue-based:
val engine = emptyMockEngine()
engine.enqueueResponse(SyncResponses.LIST_USER_FILES_SUCCESS_200)
```
