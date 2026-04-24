# aktual-app:nav

Navigation API: navigator classes, `NavKey` routes, stack extensions, and the `NavEntryContributor` interface. The sibling `:ui` module owns `AktualNavHost`, `AktualAppContent`, and `RootViewModel`.

## Instructions
- **IMPORTANT**: Whenever any navigation logic changes, keep the mermaid diagram at [navgraph.mmd](./navgraph.mmd) up to date.

## Conventions
- One navigator per file, co-located with its `NavKey` route (e.g., `ListBudgetsNavigator.kt` holds `ListBudgetsNavigator` + `ListBudgetsNavRoute`)
- Navigator is `@Immutable` with `operator fun invoke(...)` that pushes its route onto the stack
- `BackNavigator` (no route) handles `debugPop()`
- Pass navigator instances directly to screen composables (e.g., `back: BackNavigator, toSettings: SettingsNavigator`)
- For complex stack ops (e.g., `debugPopUpToAndPush`), pass lambdas from the `NavEntryContributor` instead

## Decentralized entries

Each `:ui` module owns its nav entries via a `NavEntryContributor` annotated `@ContributesIntoSet(NavScope::class)`. `NavGraph` is a `@GraphExtension(NavScope::class)` child of `AppScope` exposing `Set<NavEntryContributor>`; `RootViewModel` creates it and `AktualNavHost` iterates contributors to build the entry provider. `aktual-app:di` depends on all `:ui` modules so Metro can discover the contribution hints.

To add a screen: create `YourNavigator.kt` (+ `YourNavRoute`) here, then implement `NavEntryContributor` in your `:ui` module.

## Window insets

Nav bar insets are consumed at the `AktualNavHost` level via `Modifier.consumeWindowInsets(WindowInsets.navigationBars)`, so screen Scaffolds will **not** see them in `innerPadding`. The haze-effect Column in `AktualAppContent` instead renders `BottomSpacing()`, which sums `bottomNavBarPadding()` with whatever height is provided via `LocalBottomSpacing` — screens that render their own bottom status bar publish its height through that composition local so a single spacer covers both.
