# aktual-app:nav

Navigation host: `AktualNavHost`, `AktualAppContent`, `RootViewModel`, and `UseCases`. Navigator classes, `NavKey` routes, stack extensions, and the `NavEntryContributor` interface live in `aktual-core:nav`.

## Instructions
- **IMPORTANT**: Whenever any navigation logic changes, keep the mermaid diagram at [navgraph.mmd](./navgraph.mmd) up to date.

## Conventions
- One navigator per file, co-located with its `NavKey` route (e.g., `ListBudgetsNavigator.kt` holds `ListBudgetsNavigator` + `ListBudgetsNavRoute`)
- Navigator is `@Immutable` with `operator fun invoke(...)` that pushes its route onto the stack
- `BackNavigator` (no route) handles `debugPop()`
- Pass navigator instances directly to screen composables (e.g., `back: BackNavigator, toSettings: SettingsNavigator`)
- For complex stack ops (e.g., `debugPopUpToAndPush`), pass lambdas from the `NavEntryContributor` instead

## Decentralized entries

Each `:ui` module owns its nav entries via a `NavEntryContributor` annotated `@ContributesIntoSet(AppScope::class)`. `RootViewModel` collects them and `AktualNavHost` iterates contributors to build the entry provider. `aktual-app:ui-app` depends on all non-budget `:ui` modules so Metro can discover the contribution hints.

To add a screen: create `YourNavigator.kt` (+ `YourNavRoute`) in `aktual-core:nav`, then implement `NavEntryContributor` in your `:ui` module.

### Budget-scoped entries

Budget screens implement `BudgetNavEntryContributor` with `@ContributesIntoSet(BudgetScope::class)`. `aktual-app:ui-budget` aggregates those `:ui` modules. Every `BudgetNavKey` carries a `tab: BudgetTab`, and entries **must** pass it as the content key so the nav rail can track the active tab:

```kotlin
scope.entry<YourNavRoute>(clazzContentKey = { it.tab }) { route -> ... }
```

## Window insets

Nav bar insets are consumed at the `AktualNavHost` level via `Modifier.consumeWindowInsets(WindowInsets.navigationBars)`, so screen Scaffolds will **not** see them in `innerPadding`. The haze-effect Column in `AktualAppContent` instead renders `BottomSpacing()`, which sums `bottomNavBarPadding()` with whatever height is provided via `LocalBottomSpacing` — screens that render their own bottom status bar publish its height through that composition local so a single spacer covers both.
