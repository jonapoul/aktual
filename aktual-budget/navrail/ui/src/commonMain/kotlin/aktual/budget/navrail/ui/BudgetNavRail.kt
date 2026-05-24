package aktual.budget.navrail.ui

import aktual.budget.navrail.vm.BudgetNavRailViewModel
import aktual.core.icons.AktualIcons
import aktual.core.icons.Calendar3
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
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.BudgetTab
import aktual.core.nav.ListRulesNavRoute
import aktual.core.nav.ListSchedulesNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.NavStackImpl
import aktual.core.nav.ReportsListNavRoute
import aktual.core.nav.TransactionsNavRoute
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTypography
import aktual.core.ui.BackHandler
import aktual.core.ui.BottomSpacing
import aktual.core.ui.LocalBottomSpacing
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.SideSpacing
import aktual.core.ui.TabletPreview
import aktual.core.ui.ThemeParameters
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.serialization.json.Json

@Composable
internal fun BudgetNavRail(
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
  viewModel: BudgetNavRailViewModel = metroViewModel(),
) {
  val contributors = viewModel.budgetNavEntryContributors

  val transactionsStack = stackWithDefault(TransactionsNavRoute)
  val reportsStack = stackWithDefault(ReportsListNavRoute)
  val schedulesStack = stackWithDefault(ListSchedulesNavRoute)
  val rulesStack = stackWithDefault(ListRulesNavRoute)

  val tabStacks =
    remember(transactionsStack, reportsStack, rulesStack) {
      persistentMapOf(
        BudgetTab.Transactions to transactionsStack,
        BudgetTab.Reports to reportsStack,
        BudgetTab.Schedules to schedulesStack,
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
  activeStack: NavStack<BudgetNavKey>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onPopBackToTransactions: () -> Unit,
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
) {
  var sheetExpanded by remember { mutableStateOf(false) }
  val density = LocalDensity.current
  val localHazeState = rememberHazeState()
  var navBarHeight by remember { mutableStateOf(0.dp) }
  var handleHeight by remember { mutableStateOf(0.dp) }
  val height = navBarHeight + handleHeight
  val rootBottomChromeHeight = LocalBottomSpacing.current

  BackHandler(enabled = sheetExpanded) { sheetExpanded = false }

  val dragAccumulator = remember { floatArrayOf(0f) }
  val dragThreshold = with(density) { 40.dp.toPx() }
  val draggableState = rememberDraggableState { delta -> dragAccumulator[0] += delta }

  Box(modifier = modifier.fillMaxSize()) {
    CompositionLocalProvider(LocalBottomSpacing provides rootBottomChromeHeight + height) {
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
      NavSheetHandle(
        modifier =
          Modifier.onSizeChanged { size -> handleHeight = with(density) { size.height.toDp() } }
            .draggable(
              state = draggableState,
              orientation = Orientation.Vertical,
              onDragStopped = {
                val d = dragAccumulator[0]
                dragAccumulator[0] = 0f
                when {
                  d < -dragThreshold -> sheetExpanded = true
                  d > dragThreshold -> sheetExpanded = false
                }
              },
            )
            .clickable { sheetExpanded = !sheetExpanded }
      )

      SharedTransitionLayout(modifier = Modifier.fillMaxWidth()) {
        AnimatedContent(
          targetState = sheetExpanded,
          transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
          modifier = Modifier.fillMaxWidth(),
        ) { expanded ->
          if (expanded) {
            ExpandedSheetContent(
              selectedTab = selectedTab,
              onSelectTab = onSelectTab,
              onAction = onAction,
              animatedContentScope = this,
            )
          } else {
            CollapsedSheetContent(
              selectedTab = selectedTab,
              onSelectTab = onSelectTab,
              onMenuClick = { sheetExpanded = true },
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
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onMenuClick: () -> Unit,
  onChangeNavBarHeight: (Dp) -> Unit,
  animatedContentScope: AnimatedContentScope,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val density = LocalDensity.current
  NavigationBar(
    modifier =
      modifier.fillMaxWidth().onSizeChanged { size ->
        onChangeNavBarHeight(with(density) { size.height.toDp() })
      },
    containerColor = Color.Transparent,
    contentColor = theme.sidebarItemText,
  ) {
    for (tab in BudgetTab.entries) {
      NavigationBarItem(
        modifier =
          Modifier.sharedBounds(
            sharedContentState = rememberSharedContentState(key = "nav_tab_${tab.name}"),
            animatedVisibilityScope = animatedContentScope,
          ),
        icon = { Icon(tab.icon(), contentDescription = tab.label()) },
        label = { Text(text = tab.label(), color = LocalContentColor.current) },
        selected = selectedTab == tab,
        onClick = { onSelectTab(tab) },
        colors = theme.navBarItem(),
      )
    }
    // Menu item has no sharedBounds — fades out with the default AnimatedContent transition
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
private fun SharedTransitionScope.ExpandedSheetContent(
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onAction: BudgetNavActionHandler,
  animatedContentScope: AnimatedContentScope,
  modifier: Modifier = Modifier,
) {
  FlowRow(modifier = modifier.fillMaxWidth(), maxItemsInEachRow = BudgetTab.entries.size) {
    for (tab in BudgetTab.entries) {
      NavSheetItem(
        modifier =
          Modifier.weight(1f)
            .sharedBounds(
              sharedContentState = rememberSharedContentState(key = "nav_tab_${tab.name}"),
              animatedVisibilityScope = animatedContentScope,
            ),
        icon = tab.icon(),
        label = tab.label(),
        selected = selectedTab == tab,
        onClick = { onSelectTab(tab) },
      )
    }
    // action shortcuts — fade in via default AnimatedContent transition
    NavSheetItem(
      modifier = Modifier.weight(1f),
      icon = MaterialIcons.SwapHoriz,
      label = Strings.budgetNavMenuSwitchBudget,
      selected = false,
      onClick = { onAction(SwitchFile) },
    )
    NavSheetItem(
      modifier = Modifier.weight(1f),
      icon = MaterialIcons.Logout,
      label = Strings.budgetNavMenuLogOut,
      selected = false,
      onClick = { onAction(LogOut) },
    )
    NavSheetItem(
      modifier = Modifier.weight(1f),
      icon = MaterialIcons.Settings,
      label = Strings.budgetNavMenuSettings,
      selected = false,
      onClick = { onAction(Settings) },
    )
    NavSheetItem(
      modifier = Modifier.weight(1f),
      icon = MaterialIcons.Info,
      label = Strings.budgetNavMenuAbout,
      selected = false,
      onClick = { onAction(About) },
    )
  }
}

@Composable
private fun NavSheetHandle(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier =
        Modifier.fillMaxWidth(fraction = 0.3f)
          .height(4.dp)
          .clip(RoundedCornerShape(percent = 50))
          .background(LocalTheme.current.sidebarItemText.copy(alpha = 0.4f))
    )
  }
}

@Composable
private fun NavSheetItem(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val indicatorColor = if (selected) theme.sidebarItemTextSelected.disabled else Color.Transparent
  val contentColor = if (selected) theme.sidebarItemTextSelected else theme.sidebarItemText
  Column(
    modifier = modifier.clickable(onClick = onClick).padding(vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Box(
      modifier =
        Modifier.size(width = 64.dp, height = 32.dp)
          .clip(RoundedCornerShape(percent = 50))
          .background(indicatorColor),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(24.dp),
      )
    }
    Text(text = label, style = AktualTypography.labelSmall, color = contentColor)
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
          contributor.contribute(scope = this, activeStack)
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

@Composable
private fun BudgetTab.label(): String =
  when (this) {
    BudgetTab.Transactions -> Strings.transactionsTitle
    BudgetTab.Reports -> Strings.reportsTitle
    BudgetTab.Schedules -> Strings.listSchedulesTitle
    BudgetTab.Rules -> Strings.rulesTitle
  }

@Stable
private fun BudgetTab.icon(): ImageVector =
  when (this) {
    BudgetTab.Transactions -> MaterialIcons.LinearScale
    BudgetTab.Reports -> AktualIcons.Reports
    BudgetTab.Schedules -> AktualIcons.Calendar3
    BudgetTab.Rules -> AktualIcons.Tuning
  }

@PortraitPreview
@Composable
private fun PreviewNavSheetCollapsed(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    Column(modifier = Modifier.fillMaxSize()) {
      PreviewContent(modifier = Modifier.weight(1f))
      NavSheetHandle()
      CollapsedNavSheetPreviewContent(selectedTab = BudgetTab.Transactions)
    }
  }

@PortraitPreview
@Composable
private fun PreviewNavSheetExpanded(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) {
    Column(modifier = Modifier.fillMaxSize()) {
      PreviewContent(modifier = Modifier.weight(1f))
      NavSheetHandle()
      ExpandedNavSheetPreviewContent(selectedTab = BudgetTab.Transactions)
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
private fun CollapsedNavSheetPreviewContent(
  selectedTab: BudgetTab,
  theme: Theme = LocalTheme.current,
) {
  NavigationBar(
    modifier = Modifier.fillMaxWidth(),
    containerColor = Color.Transparent,
    contentColor = theme.sidebarItemText,
  ) {
    for (tab in BudgetTab.entries) {
      NavigationBarItem(
        icon = { Icon(tab.icon(), contentDescription = tab.label()) },
        label = {
          Text(text = tab.label(), color = LocalContentColor.current, textAlign = TextAlign.Center)
        },
        selected = selectedTab == tab,
        onClick = {},
        colors = theme.navBarItem(),
      )
    }
    NavigationBarItem(
      icon = { Icon(MaterialIcons.Menu, contentDescription = Strings.budgetNavMenu) },
      label = { Text(text = Strings.budgetNavMenu, color = LocalContentColor.current) },
      selected = false,
      onClick = {},
      colors = theme.navBarItem(),
    )
  }
}

@Composable
private fun ExpandedNavSheetPreviewContent(selectedTab: BudgetTab) {
  FlowRow(modifier = Modifier.fillMaxWidth(), maxItemsInEachRow = BudgetTab.entries.size) {
    for (tab in BudgetTab.entries) {
      NavSheetItem(
        modifier = Modifier.weight(1f),
        icon = tab.icon(),
        label = tab.label(),
        selected = selectedTab == tab,
        onClick = {},
      )
    }
    NavSheetItem(
      modifier = Modifier.weight(1f),
      icon = MaterialIcons.SwapHoriz,
      label = Strings.budgetNavMenuSwitchBudget,
      selected = false,
      onClick = {},
    )
    NavSheetItem(
      modifier = Modifier.weight(1f),
      icon = MaterialIcons.Logout,
      label = Strings.budgetNavMenuLogOut,
      selected = false,
      onClick = {},
    )
    NavSheetItem(
      modifier = Modifier.weight(1f),
      icon = MaterialIcons.Settings,
      label = Strings.budgetNavMenuSettings,
      selected = false,
      onClick = {},
    )
    NavSheetItem(
      modifier = Modifier.weight(1f),
      icon = MaterialIcons.Info,
      label = Strings.budgetNavMenuAbout,
      selected = false,
      onClick = {},
    )
  }
}

@Composable
private fun PreviewContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = "Content", style = AktualTypography.headlineMedium)
  }
}
