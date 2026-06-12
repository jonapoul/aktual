package aktual.budget.navrail.ui

import aktual.budget.navrail.vm.BudgetNavRailViewModel
import aktual.core.icons.AktualIcons
import aktual.core.icons.ArrowThickDown
import aktual.core.icons.material.Edit
import aktual.core.icons.material.GridOn
import aktual.core.icons.material.Info
import aktual.core.icons.material.Logout
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Menu
import aktual.core.icons.material.Settings
import aktual.core.icons.material.SwapHoriz
import aktual.core.l10n.Strings
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.BudgetTab
import aktual.core.nav.ListRulesNavRoute
import aktual.core.nav.ListSchedulesNavRoute
import aktual.core.nav.ListTagsNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.NavStackImpl
import aktual.core.nav.ReportsListNavRoute
import aktual.core.nav.TransactionsNavRoute
import aktual.core.theme.Colors
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.BottomSpacing
import aktual.core.ui.ColoredParameters
import aktual.core.ui.LocalBottomSpacing
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.SideSpacing
import aktual.core.ui.TabletPreview
import aktual.core.ui.blurredBottomBar
import aktual.core.ui.disabled
import aktual.core.ui.isCompactWidth
import aktual.core.ui.isMobileLandscape
import aktual.core.ui.rememberAppCloser
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.End
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.Json

// Max number of cells across the collapsed bottom bar. The last slot is always the expand button,
// so the bar shows at most NAV_GRID_COLUMNS - 1 tab shortcuts; remaining tabs live only in the
// expanded grid. The expanded sheet and edit grid use the same width
internal const val NAV_GRID_COLUMNS = 4

@Composable
internal fun BudgetNavRail(
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
  viewModel: BudgetNavRailViewModel = metroViewModel(),
) {
  val contributors = viewModel.budgetNavEntryContributors
  val navGridOrder by viewModel.navGridOrder.collectAsState()

  val transactionsStack = stackWithDefault(TransactionsNavRoute)
  val reportsStack = stackWithDefault(ReportsListNavRoute)
  val schedulesStack = stackWithDefault(ListSchedulesNavRoute)
  val rulesStack = stackWithDefault(ListRulesNavRoute)
  val tagsStack = stackWithDefault(ListTagsNavRoute)

  val tabStacks =
    remember(transactionsStack, reportsStack, schedulesStack, rulesStack, tagsStack) {
      persistentMapOf(
        BudgetTab.Transactions to transactionsStack,
        BudgetTab.Reports to reportsStack,
        BudgetTab.Schedules to schedulesStack,
        BudgetTab.Rules to rulesStack,
        BudgetTab.Tags to tagsStack,
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

  val closeApp = rememberAppCloser()
  val onPopBackToTransactions: () -> Unit = {
    if (selectedTab != BudgetTab.Transactions) {
      selectedTab = BudgetTab.Transactions
    } else {
      closeApp()
    }
  }

  if (isCompactWidth()) {
    BottomNavLayout(
      contributors = contributors,
      order = navGridOrder,
      activeStack = activeStack,
      selectedTab = selectedTab,
      onSelectTab = onSelectTab,
      onPopBackToTransactions = onPopBackToTransactions,
      onAction = onAction,
      modifier = modifier,
    )
  } else {
    SideNavLayout(
      contributors = contributors,
      activeStack = activeStack,
      selectedTab = selectedTab,
      onSelectTab = onSelectTab,
      onPopBackToTransactions = onPopBackToTransactions,
      onAction = onAction,
      modifier = modifier,
    )
  }
}

@Composable
private fun stackWithDefault(default: BudgetNavKey): NavStack<BudgetNavKey> =
  rememberSaveable(saver = budgetNavKeyStackSaver()) {
    NavStackImpl(appCloser = null, stack = mutableStateListOf(default))
  }

@Composable
private fun BottomNavLayout(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  order: ImmutableList<BudgetTab>,
  activeStack: NavStack<BudgetNavKey>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onPopBackToTransactions: () -> Unit,
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
) {
  var sheetExpanded by remember { mutableStateOf(false) }
  val localHazeState = rememberHazeState()
  var navBarHeight by remember { mutableStateOf(0.dp) }
  val rootBottomChromeHeight = LocalBottomSpacing.current

  BackHandler(enabled = sheetExpanded) { sheetExpanded = false }

  Box(modifier = modifier.fillMaxSize()) {
    CompositionLocalProvider(LocalBottomSpacing provides rootBottomChromeHeight + navBarHeight) {
      BudgetNavDisplay(
        contributors = contributors,
        activeStack = activeStack,
        onPopBackToTransactions = onPopBackToTransactions,
        modifier = Modifier.fillMaxSize().hazeSource(localHazeState),
      )
    }

    Column(
      modifier =
        Modifier.align(Alignment.BottomCenter)
          .fillMaxWidth()
          .blurredBottomBar(state = localHazeState)
    ) {
      SharedTransitionLayout(modifier = Modifier.fillMaxWidth()) {
        AnimatedContent(
          targetState = sheetExpanded,
          transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
          modifier = Modifier.fillMaxWidth(),
        ) { expanded ->
          if (expanded) {
            ExpandedSheetContent(
              order = order,
              selectedTab = selectedTab,
              onSelectTab = onSelectTab,
              onCollapse = { sheetExpanded = false },
              onAction = onAction,
              animatedContentScope = this,
            )
          } else {
            CollapsedSheetContent(
              order = order,
              selectedTab = selectedTab,
              onSelectTab = onSelectTab,
              onExpand = { sheetExpanded = true },
              onAction = onAction,
              onChangeNavBarHeight = { navBarHeight = it },
              animatedContentScope = this,
            )
          }
        }
      }

      BottomSpacing(height = rootBottomChromeHeight)
    }
  }
}

@Composable
private fun SharedTransitionScope.CollapsedSheetContent(
  order: ImmutableList<BudgetTab>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onExpand: () -> Unit,
  onAction: BudgetNavActionHandler,
  onChangeNavBarHeight: (Dp) -> Unit,
  animatedContentScope: AnimatedContentScope,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current
  NavigationBar(
    modifier =
      modifier.fillMaxWidth().onSizeChanged { size ->
        onChangeNavBarHeight(with(density) { size.height.toDp() })
      },
    containerColor = Color.Transparent,
    contentColor = colors.sidebarItemText,
  ) {
    // The first row of the saved grid doubles as the collapsed bar's shortcuts, minus its last
    // slot which is the expand button
    for (tab in order.take(NAV_GRID_COLUMNS - 1)) {
      val action = tab.asNavAction()
      NavigationBarItem(
        tab = tab,
        isSelected = action == null && selectedTab == tab,
        onClick = { if (action != null) onAction(action) else onSelectTab(tab) },
        animatedContentScope = animatedContentScope,
      )
    }

    // Expand item has no sharedBounds — fades out with the default AnimatedContent transition
    NavigationBarItem(
      icon = { Icon(MaterialIcons.GridOn, contentDescription = Strings.budgetNavExpand) },
      label = { Text(text = Strings.budgetNavExpand, color = LocalContentColor.current) },
      selected = false,
      onClick = onExpand,
      colors = colors.navBarItem(),
    )
  }
}

@Composable
context(transitionScope: SharedTransitionScope)
private fun RowScope.NavigationBarItem(
  tab: BudgetTab,
  isSelected: Boolean,
  onClick: () -> Unit,
  animatedContentScope: AnimatedContentScope,
  modifier: Modifier = Modifier,
) {
  with(transitionScope) {
    NavigationBarItem(
      modifier =
        modifier.sharedBounds(
          sharedContentState =
            transitionScope.rememberSharedContentState(key = "nav_tab_${tab.name}"),
          animatedVisibilityScope = animatedContentScope,
        ),
      icon = { Icon(tab.icon(), contentDescription = tab.label()) },
      label = { Text(text = tab.label(), color = LocalContentColor.current) },
      selected = isSelected,
      onClick = onClick,
      colors = colors.navBarItem(),
    )
  }
}

@Composable
private fun SharedTransitionScope.ExpandedSheetContent(
  order: ImmutableList<BudgetTab>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onCollapse: () -> Unit,
  onAction: BudgetNavActionHandler,
  animatedContentScope: AnimatedContentScope,
  modifier: Modifier = Modifier,
) {
  val onTabClick: (BudgetTab) -> Unit = { tab ->
    val action = tab.asNavAction()
    if (action != null) {
      onAction(action)
    } else {
      onSelectTab(tab)
      onCollapse()
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      maxItemsInEachRow = NAV_GRID_COLUMNS,
      horizontalArrangement = Arrangement.Start,
    ) {
      // The first NAV_GRID_COLUMNS - 1 items mirror the collapsed bar's shortcuts, so they keep the
      // shared-element transition
      order.take(NAV_GRID_COLUMNS - 1).fastForEach { tab ->
        NavSheetItem(
          modifier =
            Modifier.fillMaxWidth(1f / NAV_GRID_COLUMNS)
              .sharedBounds(
                sharedContentState = rememberSharedContentState(key = "nav_tab_${tab.name}"),
                animatedVisibilityScope = animatedContentScope,
              ),
          icon = tab.icon(),
          label = tab.label(),
          selected = tab.asNavAction() == null && selectedTab == tab,
          onClick = { onTabClick(tab) },
        )
      }

      // The collapse toggle takes the last cell of the first row, mirroring the expand button's
      // slot in the collapsed bar
      NavSheetItem(
        modifier = Modifier.fillMaxWidth(1f / NAV_GRID_COLUMNS),
        icon = AktualIcons.ArrowThickDown,
        label = Strings.budgetNavCollapse,
        selected = false,
        onClick = onCollapse,
      )

      // Remaining items have no counterpart in the collapsed bar, so they just fade in
      order.drop(NAV_GRID_COLUMNS - 1).fastForEach { tab ->
        NavSheetItem(
          modifier = Modifier.fillMaxWidth(1f / NAV_GRID_COLUMNS),
          icon = tab.icon(),
          label = tab.label(),
          selected = tab.asNavAction() == null && selectedTab == tab,
          onClick = { onTabClick(tab) },
        )
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      NormalTextButton(
        text = Strings.budgetNavGridEdit,
        onClick = { onAction(EditNavGrid) },
        prefix = {
          Icon(
            imageVector = MaterialIcons.Edit,
            contentDescription = Strings.budgetNavGridEdit,
            tint = colors.sidebarItemText,
          )
        },
      )
    }
  }
}

@Composable
private fun SideNavLayout(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  activeStack: NavStack<BudgetNavKey>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onPopBackToTransactions: () -> Unit,
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
) {
  var showMenu by remember { mutableStateOf(false) }
  Row(modifier = modifier.fillMaxSize()) {
    Box(modifier = Modifier.zIndex(1f), contentAlignment = Alignment.TopStart) {
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
      onPopBackToTransactions = onPopBackToTransactions,
      modifier = Modifier.weight(1f),
    )

    if (isMobileLandscape()) {
      SideSpacing()
    }
  }
}

@Composable
private fun BudgetNavDisplay(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  activeStack: NavStack<BudgetNavKey>,
  onPopBackToTransactions: () -> Unit,
  modifier: Modifier = Modifier,
) {
  NavDisplay(
    modifier = modifier,
    backStack = activeStack,
    onBack = { if (activeStack.size > 1) activeStack.pop() else onPopBackToTransactions() },
    transitionSpec = {
      val initialTab = initialState.key as BudgetTab
      val targetTab = targetState.key as BudgetTab
      val direction = initialTab.ordinal - targetTab.ordinal
      when {
        direction < 0 -> slideIntoContainer(towards = Start) togetherWith fadeOut()
        direction > 0 -> slideIntoContainer(towards = End) togetherWith fadeOut()
        else -> EnterTransition.None togetherWith ExitTransition.None
      }
    },
    popTransitionSpec = { slideIntoContainer(towards = End) togetherWith fadeOut() },
    predictivePopTransitionSpec = { slideIntoContainer(towards = End) togetherWith fadeOut() },
    entryDecorators =
      listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
    entryProvider =
      entryProvider {
        for (contributor in contributors) {
          with(contributor) { contribute(activeStack) }
        }
      },
  )
}

@Composable
private fun SideNavRail(
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onMenuClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  NavigationRail(
    modifier = modifier,
    containerColor = colors.sidebarBackground,
    contentColor = colors.sidebarItemText,
  ) {
    for (tab in BudgetTab.tabs) {
      NavigationRailItem(
        icon = { Icon(tab.icon(), contentDescription = tab.label()) },
        label = { Text(text = tab.label(), color = LocalContentColor.current) },
        alwaysShowLabel = true,
        selected = selectedTab == tab,
        onClick = { onSelectTab(tab) },
        colors = colors.navRailItem(),
      )
    }
    NavigationRailItem(
      icon = { Icon(MaterialIcons.Menu, contentDescription = Strings.budgetNavMenu) },
      label = { Text(text = Strings.budgetNavMenu, color = LocalContentColor.current) },
      alwaysShowLabel = true,
      selected = false,
      onClick = onMenuClick,
      colors = colors.navRailItem(),
    )
  }
}

@Stable
private fun Colors.navBarItem(): NavigationBarItemColors =
  NavigationBarItemColors(
    selectedIconColor = sidebarItemTextSelected,
    selectedTextColor = sidebarItemTextSelected,
    selectedIndicatorColor = sidebarItemTextSelected.disabled,
    unselectedIconColor = sidebarItemText,
    unselectedTextColor = sidebarItemText,
    disabledIconColor = sidebarItemText.disabled,
    disabledTextColor = sidebarItemText.disabled,
  )

@Stable
private fun Colors.navRailItem(): NavigationRailItemColors =
  NavigationRailItemColors(
    selectedIconColor = sidebarItemTextSelected,
    selectedTextColor = sidebarItemTextSelected,
    selectedIndicatorColor = sidebarItemTextSelected.disabled,
    unselectedIconColor = sidebarItemText,
    unselectedTextColor = sidebarItemText,
    disabledIconColor = sidebarItemText.disabled,
    disabledTextColor = sidebarItemText.disabled,
  )

private val TabSaver: Saver<BudgetTab, Int> =
  Saver(save = { it.ordinal }, restore = { BudgetTab.entries[it] })

private fun budgetNavKeyStackSaver() =
  Saver<NavStack<BudgetNavKey>, String>(
    save = { stack -> Json.encodeToString(stack.toList()) },
    restore = { json ->
      NavStackImpl(
        appCloser = null,
        stack = mutableStateListOf<BudgetNavKey>().apply { addAll(Json.decodeFromString(json)) },
      )
    },
  )

@Composable
private fun BudgetMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    AktualDropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
      AktualDropdownMenuItem(
        text = Strings.budgetNavMenuSwitchBudget,
        leadingIcon = MaterialIcons.SwapHoriz,
        onClick = {
          onDismissRequest()
          onAction(SwitchFile)
        },
      )
      AktualDropdownMenuItem(
        text = Strings.budgetNavMenuLogOut,
        leadingIcon = MaterialIcons.Logout,
        onClick = {
          onDismissRequest()
          onAction(LogOut)
        },
      )
      AktualDropdownMenuItem(
        text = Strings.budgetNavMenuSettings,
        leadingIcon = MaterialIcons.Settings,
        onClick = {
          onDismissRequest()
          onAction(Settings)
        },
      )
      AktualDropdownMenuItem(
        text = Strings.budgetNavMenuAbout,
        leadingIcon = MaterialIcons.Info,
        onClick = {
          onDismissRequest()
          onAction(About)
        },
      )
    }
  }
}

@PortraitPreview
@Composable
private fun PreviewNavSheetCollapsed(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    Column(modifier = Modifier.fillMaxSize()) {
      PreviewContent(modifier = Modifier.weight(1f))
      NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Color.Transparent,
        contentColor = colors.sidebarItemText,
      ) {
        for (tab in BudgetTab.tabs.take(NAV_GRID_COLUMNS - 1)) {
          NavigationBarItem(
            icon = { Icon(tab.icon(), contentDescription = tab.label()) },
            label = {
              Text(
                text = tab.label(),
                color = LocalContentColor.current,
                textAlign = TextAlign.Center,
              )
            },
            selected = tab == BudgetTab.Transactions,
            onClick = {},
            colors = colors.navBarItem(),
          )
        }
        NavigationBarItem(
          icon = { Icon(MaterialIcons.GridOn, contentDescription = Strings.budgetNavExpand) },
          label = { Text(text = Strings.budgetNavExpand, color = LocalContentColor.current) },
          selected = false,
          onClick = {},
          colors = colors.navBarItem(),
        )
      }
    }
  }

@PortraitPreview
@Composable
private fun PreviewNavSheetExpanded(@PreviewParameter(ColoredParameters::class) colors: Colors) {
  PreviewWithColors(colors) {
    Column(modifier = Modifier.fillMaxSize()) {
      PreviewContent(modifier = Modifier.weight(1f))
      SharedTransitionLayout(modifier = Modifier.fillMaxWidth()) {
        AnimatedContent(targetState = true, modifier = Modifier.fillMaxWidth()) { expanded ->
          if (expanded) {
            ExpandedSheetContent(
              order = BudgetTab.entries.toImmutableList(),
              selectedTab = BudgetTab.Transactions,
              onSelectTab = {},
              onCollapse = {},
              onAction = {},
              animatedContentScope = this,
            )
          }
        }
      }
    }
  }
}

@TabletPreview
@Composable
private fun PreviewSideNavRail(@PreviewParameter(ColoredParameters::class) colors: Colors) {
  PreviewWithColors(colors) {
    Row(modifier = Modifier.fillMaxSize()) {
      SideNavRail(selectedTab = BudgetTab.Transactions, onSelectTab = {}, onMenuClick = {})
      PreviewContent(modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun PreviewContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = "Content", style = typography.headlineMedium)
  }
}
