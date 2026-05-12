---
name: new-list-screen
description: Generate a new UI/VM module pairing with a scrolling list screen — blurred top bar, pull-to-refresh, shimmer loading, and Loading/Empty/Failure/Success states. Uses ListSchedulesScreen as the reference template.
argument-hint: "<FeatureName> <gradle-module-path>"
---

Generate a pair of VM and UI Gradle modules for a new scrolling list screen, following the exact patterns from
`aktual-budget:schedules`.

## Before starting — ask one question

**Ask the user:** "Is this feature budget-scoped? (i.e. does it live inside an open budget, using `BudgetNavEntryContributor` / `BudgetNavScope` / `BudgetNavKey`?)"

Wait for the answer, then proceed. Use `{budgetScoped}` = `true` or `false` to select the correct
`NavEntryContributor` template in Step 2 below.

## Arguments

`$ARGUMENTS` contains two space-separated values:

- **FeatureName** — PascalCase singular name for the feature, e.g. `Rules`
- **GradleModulePath** — colon-separated path **without** a leading colon, e.g. `aktual-budget:rules`

## Derived values

Given `FeatureName = Rules` and `GradleModulePath = aktual-budget:rules`:

| Variable        | Derivation                                                              | Example                    |
|-----------------|-------------------------------------------------------------------------|----------------------------|
| `{Name}`        | FeatureName as-is                                                       | `Rules`                    |
| `{name}`        | FeatureName lowercased                                                  | `rules`                    |
| `{dir}`         | GradleModulePath with `:` → `/`                                         | `aktual-budget/rules`      |
| `{pkg}`         | GradleModulePath with `:` → `.` and `-` → `.`                           | `aktual.budget.rules`      |
| `{vmGradle}`    | `:{GradleModulePath}:vm`                                                | `:aktual-budget:rules:vm`  |
| `{uiGradle}`    | `:{GradleModulePath}:ui`                                                | `:aktual-budget:rules:ui`  |
| `{snakeTitle}`  | `list_{name}_title`                                                     | `list_rules_title`         |

The `List{Name}` prefix (e.g. `ListRules`) is used for all list-specific classes.

---

## Step 1 — Create the VM module

### `{dir}/vm/build.gradle.kts`

```kotlin
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.viewmodel") }

kotlin {
  commonMainDependencies {
    api(project(":aktual-core:model"))
    // TODO: add feature-specific data/domain dependencies
  }
}
```

### `{dir}/vm/src/commonMain/kotlin/{pkg}/vm/{Name}.kt`

Placeholder item type — replace with the real model once defined.

```kotlin
package {pkg}.vm

import androidx.compose.runtime.Immutable

// TODO: replace with the real domain model
@Immutable
data class {Name}(val id: String)
```

### `{dir}/vm/src/commonMain/kotlin/{pkg}/vm/list/List{Name}State.kt`

```kotlin
package {pkg}.vm.list

import {pkg}.vm.{Name}
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable sealed interface List{Name}State

data object Loading : List{Name}State

@JvmInline value class Failure(val cause: String?) : List{Name}State

data object Empty : List{Name}State

@JvmInline value class Success(val items: ImmutableList<{Name}>) : List{Name}State
```

### `{dir}/vm/src/commonMain/kotlin/{pkg}/vm/list/List{Name}ViewModel.kt`

```kotlin
package {pkg}.vm.list

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import aktual.di.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
class List{Name}ViewModel : ViewModel() {
  private val mutableState = MutableStateFlow<List{Name}State>(Loading)
  val state: StateFlow<List{Name}State> = mutableState.asStateFlow()

  fun reload() {
    // TODO
  }
}
```

## Step 2 — Create the UI module

### `{dir}/ui/build.gradle.kts`

```kotlin
import aktual.gradle.dsl.EXPERIMENTAL_MATERIAL_3
import aktual.gradle.dsl.optIn
import blueprint.core.commonMainDependencies

plugins { id("aktual.module.compose") }

optIn(EXPERIMENTAL_MATERIAL_3)

kotlin {
  commonMainDependencies {
    api(project("{vmGradle}"))
    api(project(":aktual-core:ui"))
    implementation(project(":aktual-app:nav"))
    implementation(project(":aktual-core:l10n"))
    implementation(project(":aktual-core:model"))
  }
}
```

### `{dir}/ui/src/commonMain/kotlin/{pkg}/ui/list/List{Name}Action.kt`

```kotlin
package {pkg}.ui.list

import androidx.compose.runtime.Immutable

@Immutable internal sealed interface List{Name}Action

internal data object Reload : List{Name}Action

// TODO: add feature-specific actions (e.g. Open, CreateNew)

@Immutable
internal fun interface List{Name}ActionHandler {
  operator fun invoke(action: List{Name}Action)
}
```

### `{dir}/ui/src/commonMain/kotlin/{pkg}/ui/list/List{Name}DS.kt`

```kotlin
package {pkg}.ui.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

internal object List{Name}DS {
  val listPadding = PaddingValues(horizontal = 8.dp)
  val listItemSpacing = 4.dp

  val itemCardPadding = 8.dp
  val itemContentSpacing = 4.dp

  val shimmerItemTextHeight = 20.dp
}
```

### `{dir}/ui/src/commonMain/kotlin/{pkg}/ui/list/List{Name}Item.kt`

Skeleton item + shimmer — replace the body of `List{Name}Item` with real content once the model is defined.

```kotlin
package {pkg}.ui.list

import {pkg}.vm.{Name}
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.CardShape
import aktual.core.ui.RowShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

/** Keep in sync with [ShimmerList{Name}Item] */
@Composable
internal fun List{Name}Item(
  item: {Name},
  onAction: List{Name}ActionHandler,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(theme.tableBackground, RowShape)
        .border(Dp.Hairline, theme.tableBorder, RowShape)
        .padding(List{Name}DS.itemCardPadding)
        .clickable { /* TODO: onAction(Open(item.id)) */ },
    horizontalArrangement = Arrangement.spacedBy(List{Name}DS.itemContentSpacing, Alignment.Start),
    verticalAlignment = Alignment.Top,
  ) {
    // TODO: replace with real item content
    Text(text = item.id, color = theme.pageText)
  }
}

/** Keep in sync with [List{Name}Item] */
@Composable
internal fun ShimmerList{Name}Item(
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val shimmer = rememberShimmer(ShimmerBounds.Window)
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(theme.tableBackground, RowShape)
        .border(Dp.Hairline, theme.tableBorder, RowShape)
        .padding(List{Name}DS.itemCardPadding)
        .shimmer(shimmer),
    horizontalArrangement = Arrangement.spacedBy(List{Name}DS.itemContentSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier.fillMaxWidth(fraction = 0.55f)
          .height(List{Name}DS.shimmerItemTextHeight)
          .background(theme.pageText, CardShape)
    )
  }
}
```

### `{dir}/ui/src/commonMain/kotlin/{pkg}/ui/list/List{Name}Screen.kt`

```kotlin
package {pkg}.ui.list

import {pkg}.vm.{Name}
import {pkg}.vm.list.Empty
import {pkg}.vm.list.Failure
import {pkg}.vm.list.List{Name}State
import {pkg}.vm.list.List{Name}ViewModel
import {pkg}.vm.list.Loading
import {pkg}.vm.list.Success
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Refresh
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.BlurredPullToRefreshBox
import aktual.core.ui.BottomSpacing
import aktual.core.ui.FailureAction
import aktual.core.ui.FailureScreen
import aktual.core.ui.PageBackground
import aktual.core.ui.blurredTopBar
import aktual.core.ui.rememberBlurredTopBarState
import aktual.core.ui.scrollbar
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun List{Name}Screen(
  // TODO: add navigator parameters from aktual-app:nav once nav keys exist
  modifier: Modifier = Modifier,
  viewModel: List{Name}ViewModel = metroViewModel(),
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  List{Name}Scaffold(
    modifier = modifier,
    state = state,
    onAction = { action ->
      when (action) {
        Reload -> viewModel.reload()
      }
    },
  )
}

@Composable
private fun List{Name}Scaffold(
  state: List{Name}State,
  onAction: List{Name}ActionHandler,
  modifier: Modifier = Modifier,
) {
  val theme = LocalTheme.current
  val blurState = rememberBlurredTopBarState()
  val listState = rememberLazyListState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        modifier = Modifier.blurredTopBar(blurState, isScrolled = listState.canScrollBackward),
        colors = theme.transparentTopAppBarColors(),
        title = { Text(Strings.list{Name}Title) },
      )
    },
  ) { innerPadding ->
    Box {
      PageBackground()

      BlurredPullToRefreshBox(
        modifier = Modifier.padding(List{Name}DS.listPadding),
        contentAlignment = Alignment.Center,
        onRefresh = { onAction(Reload) },
        isRefreshing = state is Loading,
        blurState = blurState,
        innerPadding = innerPadding,
      ) { padding ->
        List{Name}Content(
          state = state,
          contentPadding = padding,
          onAction = onAction,
          listState = listState,
        )
      }
    }
  }
}

@Composable
private fun List{Name}Content(
  state: List{Name}State,
  contentPadding: PaddingValues,
  onAction: List{Name}ActionHandler,
  listState: LazyListState,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    verticalArrangement = Arrangement.Top,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    when (state) {
      Loading -> {
        Column(
          modifier = Modifier.padding(contentPadding),
          verticalArrangement = Arrangement.spacedBy(List{Name}DS.listItemSpacing),
        ) {
          repeat(times = 10) { ShimmerList{Name}Item() }
          BottomSpacing()
        }
      }
      Empty -> {
        FailureScreen(
          title = Strings.list{Name}Empty,
          reason = null,
          icon = null,
          background = theme.tableBackground,
          action = null,
        )
      }
      is Failure -> {
        FailureScreen(
          title = Strings.list{Name}FailureTitle,
          reason = state.cause ?: Strings.list{Name}FailureMessage,
          background = theme.tableBackground,
          action =
            FailureAction(
              text = { Strings.syncRetry },
              icon = MaterialIcons.Refresh,
              onClick = { onAction(Reload) },
            ),
        )
      }
      is Success -> {
        ContentSuccess(
          items = state.items,
          listState = listState,
          contentPadding = contentPadding,
          onAction = onAction,
        )
      }
    }
  }
}

@Composable
private fun ContentSuccess(
  items: ImmutableList<{Name}>,
  listState: LazyListState,
  contentPadding: PaddingValues,
  onAction: List{Name}ActionHandler,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.scrollbar(listState),
    state = listState,
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(List{Name}DS.listItemSpacing),
  ) {
    items(items) { item -> List{Name}Item(item, onAction) }
    item { BottomSpacing() }
  }
}
```

### `{dir}/ui/src/commonMain/kotlin/{pkg}/ui/{Name}NavEntryContributor.kt`

**If budget-scoped** (use `BudgetNavEntryContributor` / `BudgetNavScope` / `BudgetNavKey`):

```kotlin
package {pkg}.ui

import {pkg}.ui.list.List{Name}Screen
import aktual.app.nav.NavStack
import aktual.app.nav.BudgetNavEntryContributor
import aktual.app.nav.BudgetNavKey
import aktual.di.BudgetNavScope
import aktual.app.nav.List{Name}NavRoute
import aktual.app.nav.budgetEntry
import androidx.navigation3.runtime.EntryProviderScope
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(BudgetNavScope::class)
class {Name}NavEntryContributor : BudgetNavEntryContributor {
  override fun contribute(
    scope: EntryProviderScope<BudgetNavKey>,
    stack: NavStack<BudgetNavKey>,
  ) {
    scope.budgetEntry<List{Name}NavRoute> { List{Name}Screen(/* navigators */) }
  }
}
```

**If NOT budget-scoped** (use `NavEntryContributor` / `NavScope` / `NavKey`):

```kotlin
package {pkg}.ui

import {pkg}.ui.list.List{Name}Screen
import aktual.app.nav.NavStack
import aktual.app.nav.BackNavigator
import aktual.app.nav.{Name}NavRoute
import aktual.app.nav.NavEntryContributor
import aktual.di.NavScope
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.zacsweers.metro.ContributesIntoSet

@ContributesIntoSet(NavScope::class)
class {Name}NavEntryContributor : NavEntryContributor {
  override fun contribute(scope: EntryProviderScope<NavKey>, stack: NavStack<NavKey>) {
    scope.entry<{Name}NavRoute> { List{Name}Screen(BackNavigator(stack)) }
  }
}
```

---

## Step 3 — Register in settings.gradle.kts

Open `settings.gradle.kts` and add both new module paths to the `include(...)` block, maintaining alphabetical order:

```
"{vmGradle}",
"{uiGradle}",
```

---

## Step 4 — Add l10n strings

Open `aktual-core/l10n/src/commonMain/composeResources/values/strings-{feature-area}.xml`
(create a new file `strings-{name}.xml` if no appropriate file exists).

Add these strings — adjust wording as needed:

```xml
<string name="list_{name}_title">{Name}</string>
<string name="list_{name}_empty">No {name}s found</string>
<string name="list_{name}_failure_title">Could not load {name}s</string>
<string name="list_{name}_failure_message">An unexpected error occurred</string>
```

Then regenerate the catalog:

```bash
./gradlew :aktual-core:l10n:catalog
```

The screen references `Strings.list{Name}Title`, `Strings.list{Name}Empty`, `Strings.list{Name}FailureTitle`,
and `Strings.list{Name}FailureMessage`. After the catalog runs these will be generated.

---

## Step 5 — Compile both modules

```bash
./gradlew {vmGradle}:compileAll {uiGradle}:compileAll --continue 2>&1 | grep -E "^e: |error:|FAILED|BUILD|Exception" | head -100
```

Fix any errors (usually missing nav types or wrong string key names) and rerun until both modules build cleanly.

---

## Manual follow-up checklist

After the skill completes:

- [ ] Replace the placeholder `{Name}` data class in the VM with the real domain model
- [ ] Fill in `List{Name}Item` with real item UI
- [ ] Add feature-specific actions to `List{Name}Action` (e.g. `Open`, `CreateNew`)
- [ ] Wire up the `Open` action in `List{Name}Item.clickable`
- [ ] Implement `reload()` in the ViewModel
- [ ] Add the `{Name}NavRoute` and any feature-specific navigator to `aktual-app:nav` (see that module's CLAUDE.md)
- [ ] Add the `:ui` module to `aktual-app:nav:di/build.gradle.kts`
- [ ] Add VM dependencies for data access (DAO / repository)
