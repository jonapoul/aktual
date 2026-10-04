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

Budget screens implement `BudgetNavEntryContributor` with `@ContributesIntoSet(BudgetScope::class)`. `aktual-app:ui-budget` aggregates those `:ui` modules. Every `BudgetNavKey` carries a `tab: BudgetTab`. Register entries with `budgetEntry`, which sets a content key that's unique per entry (so each keeps its own saveable state and `ViewModelStore`) but prefixed with the tab, so the nav rail's transitions can recover it via `budgetTabOf`:

```kotlin
budgetEntry<YourNavRoute> { route -> ... }
```

Each `BudgetTab` gets its own stack in `BudgetNavRail`. Less frequently used tabs (e.g. `BankSync`) are listed in its `SecondaryTabs`, which puts them below the drawer's divider and in the side rail's menu instead of in the main tab list. `Home` is the default tab, and back from the root of any other tab returns to it.

`contribute` also receives `appStack`, the app-level stack, for budget screens that push an app route (e.g. `ScheduleSettingsNavigator(appStack)` from the schedules list).

## Window insets

Nav bar insets are consumed at the `AktualNavHost` level via `Modifier.consumeWindowInsets(WindowInsets.navigationBars)`, so screen Scaffolds will **not** see them in `innerPadding`. The haze-effect Column in `AktualAppContent` instead renders `BottomSpacing()`, which sums `bottomNavBarPadding()` with whatever height is provided via `LocalBottomSpacing` - screens that render their own bottom status bar publish its height through that composition local so a single spacer covers both.
