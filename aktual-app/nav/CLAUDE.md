# aktual-app:nav

Navigation infrastructure using **Navigation 3** (`androidx.navigation3`) with a user-owned `SnapshotStateList<NavKey>` back stack.

## Key concepts

- Route objects implement `NavKey` (e.g., `@Serializable data class TransactionsNavRoute(val token: Token, val budgetId: BudgetId) : NavKey`)
- `NavDisplay` + `entryProvider { entry<T> { } }` replaces Nav2's `NavHost` + `composable<T> {}`
- Route parameters accessed directly from the typed lambda parameter — no `NavType`/URL-encoding needed
- Navigator interfaces passed to screens, backed by stack manipulation (`debugPush`, `debugPop`, `debugPopUpToAndPush`)
- Back stack created via `rememberBackStack(viewModel)` in `AktualAppContent`

## Window insets

Navigation bar insets are consumed at the `AktualNavHost` level via `Modifier.consumeWindowInsets(WindowInsets.navigationBars)`. This means individual screens' Scaffolds will **not** include bottom navigation bar padding in their `innerPadding`. The nav bar space is instead handled by `BottomNavBarSpacing()` in the haze-effect Column in `AktualAppContent`, which overlays both the bottom status bar and the system nav bar area with a blur effect.

## Key files

| File | Purpose |
|------|---------|
| `NavRoutes.kt` | All route definitions (implement `NavKey`) |
| `AktualNavHost.kt` | `NavDisplay` + `entryProvider` mapping routes to screens |
| `AktualAppContent.kt` | Root composable, creates back stack via `rememberBackStack` |
| `Navigators.kt` | Navigator interface implementations using stack ops |
| `Extensions.kt` | `debugPush`, `debugPop`, `debugPopUpToAndPush` helpers |
