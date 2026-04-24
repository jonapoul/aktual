package aktual.budget.navrail.ui

import aktual.app.nav.AktualNavStack
import aktual.app.nav.BudgetNavEntryContributor
import aktual.app.nav.BudgetNavKey
import aktual.app.nav.ListRulesNavRoute
import aktual.app.nav.ReportsListNavRoute
import aktual.app.nav.TransactionsNavRoute
import aktual.budget.model.BudgetId
import aktual.budget.navrail.vm.BudgetNavRailViewModel
import aktual.core.icons.AktualIcons
import aktual.core.icons.Reports
import aktual.core.icons.Tuning
import aktual.core.icons.material.Info
import aktual.core.icons.material.LinearScale
import aktual.core.icons.material.Logout
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Menu
import aktual.core.icons.material.Settings
import aktual.core.icons.material.SwapHoriz
import aktual.core.l10n.Strings
import aktual.core.model.Token
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.BottomSpacing
import aktual.core.ui.LocalBottomSpacing
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.TabletPreview
import aktual.core.ui.ThemeParameters
import aktual.core.ui.ThemedDropdownMenu
import aktual.core.ui.ThemedDropdownMenuItem
import aktual.core.ui.blurredBottomBar
import aktual.core.ui.disabled
import aktual.core.ui.isCompactWidth
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.json.Json

@Composable
fun BudgetNavRail(
  token: Token,
  budgetId: BudgetId,
  onAction: (BudgetNavAction) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: BudgetNavRailViewModel = metroViewModel(budgetId),
) {
  val contributors = viewModel.budgetNavEntryContributors

  val transactionsStack = stackWithDefault(TransactionsNavRoute(token, budgetId))
  val reportsStack = stackWithDefault(ReportsListNavRoute(token, budgetId))
  val rulesStack = stackWithDefault(ListRulesNavRoute)

  val tabStacks =
    remember(transactionsStack, reportsStack, rulesStack) {
      persistentMapOf(
        BudgetTab.Transactions to transactionsStack,
        BudgetTab.Reports to reportsStack,
        BudgetTab.Rules to rulesStack,
      )
    }

  var selectedTab by
    rememberSaveable(stateSaver = TabSaver) { mutableStateOf(BudgetTab.Transactions) }

  val activeStack = remember(tabStacks, selectedTab) { tabStacks.getValue(selectedTab) }

  val onSelectTab: (BudgetTab) -> Unit = { tab ->
    if (tab == selectedTab) {
      val stack = tabStacks.getValue(tab)
      while (stack.size > 1) stack.removeAt(stack.lastIndex)
    } else {
      selectedTab = tab
    }
  }

  if (isCompactWidth()) {
    BottomNavLayout(
      contributors = contributors,
      activeStack = activeStack,
      selectedTab = selectedTab,
      onSelectTab = onSelectTab,
      onAction = onAction,
      modifier = modifier,
    )
  } else {
    SideNavLayout(
      contributors = contributors,
      activeStack = activeStack,
      selectedTab = selectedTab,
      onSelectTab = onSelectTab,
      onAction = onAction,
      modifier = modifier,
    )
  }
}

@Composable
private fun stackWithDefault(default: BudgetNavKey): AktualNavStack<BudgetNavKey> =
  rememberSaveable(saver = budgetNavKeyStackSaver()) {
    AktualNavStack(appCloser = null, stack = mutableStateListOf(default))
  }

@Composable
private fun BottomNavLayout(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  activeStack: AktualNavStack<BudgetNavKey>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onAction: (BudgetNavAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showMenu by remember { mutableStateOf(false) }
  val density = LocalDensity.current

  // Use a local HazeState so the nav rail's hazeEffect samples BudgetNavDisplay rather than the
  // root AktualNavHost. hazeEffect doesn't work when the effect composable is *inside* the
  // hazeSource — it just renders transparent. Giving the nav display its own hazeSource with a
  // local state avoids that problem while keeping the same color/style as the root bottom bar
  val localHazeState = rememberHazeState()
  var height by remember { mutableStateOf(0.dp) }
  val rootBottomChromeHeight = LocalBottomSpacing.current

  Box(modifier = modifier.fillMaxSize()) {
    // Child screens end their content with BottomSpacing() — extend that reservation
    // to also cover the overlaid BottomNavBar, so the last visible items aren't hidden behind it
    CompositionLocalProvider(LocalBottomSpacing provides rootBottomChromeHeight + height) {
      BudgetNavDisplay(
        contributors = contributors,
        activeStack = activeStack,
        modifier = Modifier.fillMaxSize().hazeSource(localHazeState),
      )
    }

    // Blur the entire Column (nav rail + spacers) so the seam between the local and root haze
    // effects falls behind the root bottom-bar Column in AktualAppContent, which sits on top in
    // Z-order and covers the spacer area
    Column(
      modifier =
        Modifier.align(Alignment.BottomCenter)
          .fillMaxWidth()
          .blurredBottomBar(state = localHazeState)
    ) {
      Box(contentAlignment = Alignment.TopEnd) {
        BottomNavRail(
          selectedTab = selectedTab,
          onSelectTab = onSelectTab,
          onMenuClick = { showMenu = true },
          modifier =
            Modifier.onSizeChanged { size -> height = with(density) { size.height.toDp() } },
        )
        BudgetMenu(
          expanded = showMenu,
          onAction = onAction,
          onDismissRequest = { showMenu = false },
        )
      }
      BottomSpacing(height = rootBottomChromeHeight)
    }
  }
}

@Composable
private fun SideNavLayout(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  activeStack: AktualNavStack<BudgetNavKey>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onAction: (BudgetNavAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showMenu by remember { mutableStateOf(false) }
  Row(modifier = modifier.fillMaxSize()) {
    Box(contentAlignment = Alignment.TopStart) {
      SideNavRail(selectedTab, onSelectTab, onMenuClick = { showMenu = true })
      BudgetMenu(
        expanded = showMenu,
        onAction = onAction,
        onDismissRequest = { showMenu = false },
        modifier = Modifier.align(Alignment.TopEnd),
      )
    }
    BudgetNavDisplay(
      contributors = contributors,
      activeStack = activeStack,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun BudgetNavDisplay(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  activeStack: AktualNavStack<BudgetNavKey>,
  modifier: Modifier = Modifier,
) {
  NavDisplay(
    modifier = modifier,
    backStack = activeStack,
    onBack = { activeStack.pop() },
    // Forward: new screen slides in from right, old screen slides out to left
    transitionSpec = {
      slideInHorizontally(initialOffsetX = { width -> width }) togetherWith
        slideOutHorizontally(targetOffsetX = { width -> -width })
    },
    // Back: previous screen slides in from left, current screen slides out to right
    popTransitionSpec = {
      slideInHorizontally(initialOffsetX = { width -> -width }) togetherWith
        slideOutHorizontally(targetOffsetX = { width -> width })
    },
    // Predictive back gesture: same as pop
    predictivePopTransitionSpec = {
      slideInHorizontally(initialOffsetX = { width -> -width }) togetherWith
        slideOutHorizontally(targetOffsetX = { width -> width })
    },
    entryDecorators =
      listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
    entryProvider =
      entryProvider {
        for (contributor in contributors) {
          contributor.contribute(scope = this, activeStack)
        }
      },
  )
}

@Composable
private fun BottomNavRail(
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onMenuClick: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  // containerColor is transparent so the blur applied via Modifier.blurred(...) on the modifier
  // above shows through — an opaque color here would paint over the hazeEffect
  NavigationBar(
    modifier = modifier,
    containerColor = Color.Transparent,
    contentColor = theme.sidebarItemText,
  ) {
    for (tab in BudgetTab.entries) {
      NavigationBarItem(
        icon = { Icon(tab.icon(), contentDescription = tab.label()) },
        label = { Text(text = tab.label(), color = LocalContentColor.current) },
        selected = selectedTab == tab,
        onClick = { onSelectTab(tab) },
        colors = theme.navBarItem(),
      )
    }
    NavigationBarItem(
      icon = { Icon(MaterialIcons.Menu, contentDescription = Strings.budgetNavMenu) },
      label = { Text(text = Strings.budgetNavMenu, color = LocalContentColor.current) },
      selected = false,
      onClick = onMenuClick,
      colors = theme.navBarItem(),
    )
  }
}

@Composable
private fun SideNavRail(
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onMenuClick: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  NavigationRail(
    modifier = modifier,
    containerColor = theme.sidebarBackground,
    contentColor = theme.sidebarItemText,
  ) {
    for (tab in BudgetTab.entries) {
      NavigationRailItem(
        icon = { Icon(tab.icon(), contentDescription = tab.label()) },
        label = { Text(text = tab.label(), color = LocalContentColor.current) },
        alwaysShowLabel = true,
        selected = selectedTab == tab,
        onClick = { onSelectTab(tab) },
        colors = theme.navRailItem(),
      )
    }
    NavigationRailItem(
      icon = { Icon(MaterialIcons.Menu, contentDescription = Strings.budgetNavMenu) },
      label = { Text(text = Strings.budgetNavMenu, color = LocalContentColor.current) },
      alwaysShowLabel = true,
      selected = false,
      onClick = onMenuClick,
      colors = theme.navRailItem(),
    )
  }
}

@Composable
private fun Theme.navBarItem(): NavigationBarItemColors =
  NavigationBarItemColors(
    selectedIconColor = sidebarItemTextSelected,
    selectedTextColor = sidebarItemTextSelected,
    selectedIndicatorColor = sidebarItemTextSelected.disabled,
    unselectedIconColor = sidebarItemText,
    unselectedTextColor = sidebarItemText,
    disabledIconColor = sidebarItemText.disabled,
    disabledTextColor = sidebarItemText.disabled,
  )

@Composable
private fun Theme.navRailItem(): NavigationRailItemColors =
  NavigationRailItemColors(
    selectedIconColor = sidebarItemTextSelected,
    selectedTextColor = sidebarItemTextSelected,
    selectedIndicatorColor = sidebarItemTextSelected.disabled,
    unselectedIconColor = sidebarItemText,
    unselectedTextColor = sidebarItemText,
    disabledIconColor = sidebarItemText.disabled,
    disabledTextColor = sidebarItemText.disabled,
  )

@Composable
private fun metroViewModel(budgetId: BudgetId) =
  assistedMetroViewModel<BudgetNavRailViewModel, BudgetNavRailViewModel.Factory>(
    key = budgetId.value,
    createViewModel = { create(budgetId) },
  )

private val TabSaver: Saver<BudgetTab, Int> =
  Saver(save = { it.ordinal }, restore = { BudgetTab.entries[it] })

private fun budgetNavKeyStackSaver() =
  Saver<AktualNavStack<BudgetNavKey>, String>(
    save = { stack -> Json.encodeToString(stack.toList()) },
    restore = { json ->
      AktualNavStack(
        appCloser = null,
        stack = mutableStateListOf<BudgetNavKey>().apply { addAll(Json.decodeFromString(json)) },
      )
    },
  )

@Composable
private fun BudgetMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  onAction: (BudgetNavAction) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    ThemedDropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
      ThemedDropdownMenuItem(
        text = Strings.budgetNavMenuSwitchBudget,
        leadingIcon = MaterialIcons.SwapHoriz,
        onClick = {
          onDismissRequest()
          onAction(BudgetNavAction.SwitchFile)
        },
      )
      ThemedDropdownMenuItem(
        text = Strings.budgetNavMenuLogOut,
        leadingIcon = MaterialIcons.Logout,
        onClick = {
          onDismissRequest()
          onAction(BudgetNavAction.LogOut)
        },
      )
      ThemedDropdownMenuItem(
        text = Strings.budgetNavMenuSettings,
        leadingIcon = MaterialIcons.Settings,
        onClick = {
          onDismissRequest()
          onAction(BudgetNavAction.Settings)
        },
      )
      ThemedDropdownMenuItem(
        text = Strings.budgetNavMenuAbout,
        leadingIcon = MaterialIcons.Info,
        onClick = {
          onDismissRequest()
          onAction(BudgetNavAction.About)
        },
      )
    }
  }
}

private enum class BudgetTab {
  Transactions,
  Reports,
  Rules,
}

@Composable
private fun BudgetTab.label(): String =
  when (this) {
    BudgetTab.Transactions -> Strings.transactionsTitle
    BudgetTab.Reports -> Strings.reportsTitle
    BudgetTab.Rules -> Strings.rulesTitle
  }

@Composable
private fun BudgetTab.icon(): ImageVector =
  when (this) {
    BudgetTab.Transactions -> MaterialIcons.LinearScale
    BudgetTab.Reports -> AktualIcons.Reports
    BudgetTab.Rules -> AktualIcons.Tuning
  }

@Preview
@Composable
private fun PreviewBudgetMenu(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    Box(modifier = Modifier.fillMaxSize()) {
      BudgetMenu(expanded = true, onAction = {}, onDismissRequest = {})
    }
  }

@PortraitPreview
@Composable
private fun PreviewBottomNavRail(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    Column(modifier = Modifier.fillMaxSize()) {
      PreviewContent(modifier = Modifier.weight(1f))
      BottomNavRail(selectedTab = BudgetTab.Transactions, onSelectTab = {}, onMenuClick = {})
    }
  }

@TabletPreview
@Composable
private fun PreviewSideNavRail(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    Row(modifier = Modifier.fillMaxSize()) {
      SideNavRail(selectedTab = BudgetTab.Transactions, onSelectTab = {}, onMenuClick = {})
      PreviewContent(modifier = Modifier.weight(1f))
    }
  }

@Composable
private fun PreviewContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = "Content", style = AktualTypography.headlineMedium)
  }
}
