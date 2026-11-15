# aktual-budget

Everything that works on an open budget: its data, sync, and the screens inside it.

## Modules

- `utils` - shared budget-level types: `BudgetFiles`, `BudgetLocalPreferences`, `BudgetSync`, `SyncEvents`, `SyncStateHolder`.
- `model` - budget model types and ID value classes.
- `data:db` - SQLDelight schema, DAOs and migrations. See [data/db](data/db/CLAUDE.md).
- `data:encryption`, `data:proto`, `data:impl` - encryption, sync protobuf encoding, and their implementations. `data:impl` also holds the database migrations and `AndroidxSqlDriverFactory`.
- `sync` - syncing with the server (`BudgetSyncImpl`), the implementation of `BudgetSync` in `utils`.
- `formula` - formula parser and evaluator (spreadsheet-style functions), ported from upstream.
- `demo` - the demo budget, which opens without a server.
- `list` - picking a budget. It runs before a budget is open, so its VMs live in `LoggedInScope`, not `BudgetScope`.
- The rest (`accounts`, `transactions`, `schedules`, `rules`, ...) are feature modules following the usual `domain` / `vm` / `ui` / `components` split.
- `banksync`, `budgeting`, `payees`, `rules`, `schedules`, `tags` and `transactions` instead split their domain layer in two. The feature's root module (e.g. `:aktual-budget:rules`) holds the models and interfaces (`TransactionRulesLoader`, `TransactionWriter`, `RulesLoader`, `SchedulesLoader`, ...) that VMs and other modules use, and `:impl` holds the implementations, so callers don't depend on them. Keep the root modules free of `data:db` where possible; only `transactions` and `tags` use it, for models built from db rows. `budgeting:vm`, `home:vm`, `rules:vm`, `schedules:vm` and `transactions:vm` only depend on root modules; their tests pull in the `impl` ones to build the real implementations. The app gets each `impl` through some other module's `api` chain, or `aktual-app:di` if nothing else exports it, so Metro sees its bindings.
- `reports` has no `impl`: its root module holds the report models (`ChartData`, `ReportMeta`, ...) and the `PinnedReports` interface, so `home:vm` and `reports:components` don't depend on `reports:vm`, which holds the implementations. It keeps the `aktual.budget.reports.vm` package.

## DI

- `BudgetGraph` (in `aktual-di:runlevel`) is a child of `AppGraph`, not of `LoggedInGraph`, so a budget can open with no server (the demo budget). Bindings from `ServerChosenScope` and `LoggedInScope` aren't visible here.
- Server details come from the `BudgetServer` the graph is created with: `Remote(url, token)` or `None`. Code needing the server injects `BudgetServer` and handles `None`, e.g. by hiding or disabling the feature (see `BankSyncViewModel`).
- The graph also provides `BudgetId`, `DbMetadata` and the `SqlDriver`, which closes with the graph.
- VMs needing the open budget use `@ContributesIntoMap(BudgetScope::class)`. Long-running work uses `BudgetCoroutineScope`.
- Lifecycle hooks use `binding<@ForScope(BudgetScope::class) Initializable>()` / `Closeable`, like `ScheduleAdvancer` and `BudgetSyncImpl`.

## Screens

- Implement `BudgetNavEntryContributor` with `@ContributesIntoSet(BudgetScope::class)` instead of `NavEntryContributor`.
- Add the new `:ui` module to `aktual-app/ui-budget/build.gradle.kts`, not `ui-app`, so Metro finds it.
