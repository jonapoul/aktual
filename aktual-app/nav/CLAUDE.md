# aktual-app:nav

Navigation API module containing route definitions, stack extensions, and the `NavEntryContributor` interface.

## Key concepts

- Route objects implement `NavKey` (e.g., `@Serializable data class TransactionsNavRoute(val token: Token, val budgetId: BudgetId) : NavKey`)
- `NavDisplay` + `entryProvider { entry<T> { } }` replaces Nav2's `NavHost` + `composable<T> {}`
- Route parameters accessed directly from the typed lambda parameter — no `NavType`/URL-encoding needed
- Navigator interfaces passed to screens, backed by stack manipulation (`debugPush`, `debugPop`, `debugPopUpToAndPush`)
- Back stack created via `rememberBackStack(viewModel)` in `AktualAppContent`

## Architecture

Navigation entries are **decentralized** via `NavEntryContributor`. Each `:ui` module contributes its own entries:

1. Routes, stack extensions, `NavEntryContributor` interface, `NavScope`, and `NavGraph` live in **`aktual-app:nav`** (this module)
2. Each `:ui` module implements `NavEntryContributor` with `@ContributesIntoSet(NavScope::class)`, owning its navigator impls and entry registrations
3. `NavGraph` is a `@GraphExtension(NavScope::class)` child of `AppScope` — its `Factory` is contributed to `AppScope` via `@ContributesTo`, so any `AppScope` graph can create a `NavGraph` to access the contributors
4. **`aktual-app:nav:ui`** `RootViewModel` injects `NavGraph.Factory`, creates the graph, and exposes the `Set<NavEntryContributor>` for `AktualNavHost`
5. **`aktual-app:di`** holds `:ui` module dependencies so Metro can discover contribution hints

To add a new screen: create the contributor in your `:ui` module — no changes to `aktual-app:nav` or `aktual-app:nav:ui` needed.

## Window insets

Navigation bar insets are consumed at the `AktualNavHost` level via `Modifier.consumeWindowInsets(WindowInsets.navigationBars)`. This means individual screens' Scaffolds will **not** include bottom navigation bar padding in their `innerPadding`. The nav bar space is instead handled by `BottomNavBarSpacing()` in the haze-effect Column in `AktualAppContent`, which overlays both the bottom status bar and the system nav bar area with a blur effect.

## Key files

### aktual-app:nav (this module)

| File | Purpose |
|------|---------|
| `NavRoutes.kt` | All route definitions (implement `NavKey`) |
| `NavEntryContributor.kt` | Interface for feature modules to contribute nav entries |
| `NavScope.kt` | Scope marker for nav entry contributor bindings (child of `AppScope`) |
| `NavGraph.kt` | `@GraphExtension(NavScope::class)` exposing `Set<NavEntryContributor>` |
| `Extensions.kt` | `debugPush`, `debugPop`, `debugPopUpToAndPush` helpers |

### aktual-app:nav:ui

| File | Purpose |
|------|---------|
| `AktualNavHost.kt` | `NavDisplay` that iterates contributors to build the entry provider |
| `AktualAppContent.kt` | Root composable, creates back stack via `rememberBackStack` |
| `RootViewModel.kt` | Abstract ViewModel holding `navEntryContributors` and app-level state |
| `UseCases.kt` | App-level use cases (initial route, format config, blur, bottom bar) |
