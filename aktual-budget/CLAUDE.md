# aktual-budget

Everything that works on an open budget: its data, sync, and the screens inside it.

## Modules

- `utils` - shared budget-level types: `BudgetFiles`, `BudgetLocalPreferences`, `BudgetSyncController`, `SyncEvents`, `SyncStateHolder`.
- `model` - budget model types and ID value classes.
- `data:db` - SQLDelight schema, DAOs and migrations. See [data/db](data/db/CLAUDE.md).
- `data:encryption`, `data:proto`, `data:impl` - encryption, sync protobuf encoding, and their implementations.
- `sync` - syncing with the server (`BudgetSyncControllerImpl`).
- `formula` - formula parser and evaluator (spreadsheet-style functions), ported from upstream.
- `demo` - the demo budget, which opens without a server.
- `list` - picking a budget. It runs before a budget is open, so its VMs live in `LoggedInScope`, not `BudgetScope`.
- The rest (`accounts`, `transactions`, `schedules`, `rules`, ...) are feature modules following the usual `domain` / `vm` / `ui` / `components` split.

## DI

- `BudgetGraph` (in `aktual-di:runlevel`) is a child of `AppGraph`, not of `LoggedInGraph`, so a budget can open with no server (the demo budget). Bindings from `ServerChosenScope` and `LoggedInScope` aren't visible here.
- Server details come from the `BudgetServer` the graph is created with: `Remote(url, token)` or `None`. Code needing the server injects `BudgetServer` and handles `None`, e.g. by hiding or disabling the feature (see `BankSyncViewModel`).
- The graph also provides `BudgetId`, `DbMetadata` and the `SqlDriver`, which closes with the graph.
- VMs needing the open budget use `@ContributesIntoMap(BudgetScope::class)`. Long-running work uses `BudgetCoroutineScope`.
- Lifecycle hooks use `binding<@ForScope(BudgetScope::class) Initializable>()` / `Closeable`, like `ScheduleAdvancer` and `BudgetSyncControllerImpl`.

## Screens

- Implement `BudgetNavEntryContributor` with `@ContributesIntoSet(BudgetScope::class)` instead of `NavEntryContributor`.
- Add the new `:ui` module to `aktual-app/ui-budget/build.gradle.kts`, not `ui-app`, so Metro finds it.
