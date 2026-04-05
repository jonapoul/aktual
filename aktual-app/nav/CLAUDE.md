# aktual-app:nav

Navigation API module containing navigator classes, route definitions, stack extensions, and the `NavEntryContributor` interface.

## Instructions
- **IMPORTANT**: Whenever any navigation logic changes, make sure to keep the mermaid diagram at [navgraph.mmd](./navgraph.mmd) up to date.

## Key concepts

- Each navigator class (e.g., `ListBudgetsNavigator`) is an `@Immutable` class with `operator fun invoke(...)` that pushes its route onto the stack
- Each navigator file co-locates the route definition (`NavKey`) below the navigator class (e.g., `ListBudgetsNavRoute` is in `ListBudgetsNavigator.kt`)
- `BackNavigator` handles `debugPop()` — it has no associated route
- `NavDisplay` + `entryProvider { entry<T> { } }` replaces Nav2's `NavHost` + `composable<T> {}`
- Route parameters accessed directly from the typed lambda parameter — no `NavType`/URL-encoding needed
- Navigator classes are passed directly to screen composables as parameters (e.g., `back: BackNavigator, toSettings: SettingsNavigator`)
- For complex stack operations (`debugPopUpToAndPush`), lambdas are passed from the `NavEntryContributor` instead of navigator classes
- Back stack created via `rememberBackStack(viewModel)` in `AktualAppContent`

## Architecture

Navigation entries are **decentralized** via `NavEntryContributor`. Each `:ui` module contributes its own entries:

1. Navigator classes, routes, stack extensions, `NavEntryContributor` interface, `NavScope`, and `NavGraph` live in **`aktual-app:nav`** (this module)
2. Each `:ui` module implements `NavEntryContributor` with `@ContributesIntoSet(NavScope::class)`, creating navigator instances and passing them to screens
3. `NavGraph` is a `@GraphExtension(NavScope::class)` child of `AppScope` — its `Factory` is contributed to `AppScope` via `@ContributesTo`, so any `AppScope` graph can create a `NavGraph` to access the contributors
4. **`aktual-app:nav:ui`** `RootViewModel` injects `NavGraph.Factory`, creates the graph, and exposes the `Set<NavEntryContributor>` for `AktualNavHost`
5. **`aktual-app:di`** holds `:ui` module dependencies so Metro can discover contribution hints

To add a new screen:
1. Create a navigator class + route in this module (e.g., `YourNavigator.kt` with `YourNavRoute`)
2. Create the `NavEntryContributor` in your `:ui` module, passing navigator instances to the screen

## Window insets

Navigation bar insets are consumed at the `AktualNavHost` level via `Modifier.consumeWindowInsets(WindowInsets.navigationBars)`. This means individual screens' Scaffolds will **not** include bottom navigation bar padding in their `innerPadding`. The nav bar space is instead handled by `BottomNavBarSpacing()` in the haze-effect Column in `AktualAppContent`, which overlays both the bottom status bar and the system nav bar area with a blur effect.

## Key files

### aktual-app:nav (this module)

| File | Purpose |
|------|---------|
| `*Navigator.kt` | Navigator class + co-located `NavKey` route definition (e.g., `ListBudgetsNavigator` + `ListBudgetsNavRoute`) |
| `BackNavigator.kt` | Navigator for popping the back stack (no route) |
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
