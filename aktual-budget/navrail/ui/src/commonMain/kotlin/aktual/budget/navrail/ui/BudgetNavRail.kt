package aktual.budget.navrail.ui

import aktual.budget.navrail.vm.BudgetNavRailViewModel
import aktual.core.icons.AktualIcons
import aktual.core.icons.Calendar3
import aktual.core.icons.Reports
import aktual.core.icons.Tag
import aktual.core.icons.Tuning
import aktual.core.icons.material.AccountBalance
import aktual.core.icons.material.AccountBalanceWallet
import aktual.core.icons.material.Edit
import aktual.core.icons.material.Home
import aktual.core.icons.material.Info
import aktual.core.icons.material.Logout
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Menu
import aktual.core.icons.material.ReceiptLong
import aktual.core.icons.material.Settings
import aktual.core.icons.material.SwapHoriz
import aktual.core.icons.material.Visibility
import aktual.core.icons.material.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.nav.BankSyncNavRoute
import aktual.core.nav.BudgetEntryScope
import aktual.core.nav.BudgetNavEntryContributor
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.BudgetNavRoute
import aktual.core.nav.BudgetTab
import aktual.core.nav.HomeNavRoute
import aktual.core.nav.ListRulesNavRoute
import aktual.core.nav.ListSchedulesNavRoute
import aktual.core.nav.ListTagsNavRoute
import aktual.core.nav.NavStack
import aktual.core.nav.NavStackImpl
import aktual.core.nav.ReportsListNavRoute
import aktual.core.nav.TransactionsNavRoute
import aktual.core.nav.budgetTabOf
import aktual.core.theme.Colors
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BackHandler
import aktual.core.ui.ColoredParameters
import aktual.core.ui.LocalNavDrawerOpener
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RootOverlayContent
import aktual.core.ui.SideSpacing
import aktual.core.ui.TabletPreview
import aktual.core.ui.disabled
import aktual.core.ui.isCompactWidth
import aktual.core.ui.isExpandedWidth
import aktual.core.ui.isMobileLandscape
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
internal fun BudgetNavRail(
  appStack: NavStack<NavKey>,
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
  viewModel: BudgetNavRailViewModel = metroViewModel(),
) {
  val contributors = viewModel.budgetNavEntryContributors
  val headerState by viewModel.headerState.collectAsState()
  val accounts by viewModel.accounts.collectAsState()

  val tabStacks = rememberTabStacks()
  val transactionsStack = tabStacks.getValue(Transactions)

  var selectedTab by rememberSaveable(stateSaver = TabSaver) { mutableStateOf(BudgetTab.Home) }
  var showRenameDialog by rememberSaveable { mutableStateOf(false) }

  val activeStack = remember(tabStacks, selectedTab) { tabStacks.getValue(selectedTab) }

  val onSelectTab: (BudgetTab) -> Unit = { tab ->
    if (tab == selectedTab) {
      val stack = tabStacks.getValue(tab)
      while (stack.size > 1) stack.removeAt(stack.lastIndex)
    } else {
      selectedTab = tab
    }
  }

  // Opened from the sidebar's account list, which replaces whatever the tab was showing
  val onOpenTransactions: (BudgetNavKey.Transactions) -> Unit = { route ->
    transactionsStack.replaceAll(route)
    selectedTab = Transactions
  }

  val sidebar: @Composable ColumnScope.(close: () -> Unit) -> Unit = { close ->
    BudgetSidebarContent(
      headerState = headerState,
      accounts = accounts,
      selectedTab = selectedTab,
      transactionsRoot = transactionsStack.firstOrNull().takeIf { selectedTab == Transactions },
      onSelectTab = { tab ->
        close()
        onSelectTab(tab)
      },
      onOpenTransactions = { route ->
        close()
        onOpenTransactions(route)
      },
      onRename = {
        close()
        showRenameDialog = true
      },
      // Stays open, so the balances can be seen changing
      onSetPrivacyMode = viewModel::setPrivacyMode,
      onAction = { action ->
        close()
        onAction(action)
      },
    )
  }

  // NavDisplay only handles back when the stack has more than one entry
  BackHandler(enabled = selectedTab != BudgetTab.Home && activeStack.size == 1) {
    selectedTab = BudgetTab.Home
  }

  if (isCompactWidth()) {
    DrawerNavLayout(
      contributors = contributors,
      appStack = appStack,
      activeStack = activeStack,
      selectedTab = selectedTab,
      sidebar = sidebar,
      modifier = modifier,
    )
  } else if (isExpandedWidth()) {
    SidebarNavLayout(
      contributors = contributors,
      appStack = appStack,
      activeStack = activeStack,
      selectedTab = selectedTab,
      sidebar = { sidebar {} },
      modifier = modifier,
    )
  } else {
    SideNavLayout(
      contributors = contributors,
      isDemo = headerState.isDemo,
      appStack = appStack,
      activeStack = activeStack,
      selectedTab = selectedTab,
      onSelectTab = onSelectTab,
      onRename = { showRenameDialog = true },
      onSetPrivacyMode = viewModel::setPrivacyMode,
      onAction = onAction,
      modifier = modifier,
    )
  }

  if (showRenameDialog) {
    RenameBudgetDialog(
      currentName = headerState.budgetName.orEmpty(),
      onConfirm = { name ->
        showRenameDialog = false
        viewModel.rename(name)
      },
      onDismiss = { showRenameDialog = false },
    )
  }
}

@Composable
private fun rememberTabStacks(): ImmutableMap<BudgetTab, NavStack<BudgetNavKey>> {
  val homeStack = stackWithDefault(HomeNavRoute)
  val budgetStack = stackWithDefault(BudgetNavRoute())
  val transactionsStack = stackWithDefault(TransactionsNavRoute)
  val reportsStack = stackWithDefault(ReportsListNavRoute)
  val schedulesStack = stackWithDefault(ListSchedulesNavRoute)
  val rulesStack = stackWithDefault(ListRulesNavRoute)
  val tagsStack = stackWithDefault(ListTagsNavRoute)
  val bankSyncStack = stackWithDefault(BankSyncNavRoute)

  return remember(
    homeStack,
    budgetStack,
    transactionsStack,
    reportsStack,
    schedulesStack,
    rulesStack,
    tagsStack,
    bankSyncStack,
  ) {
    persistentMapOf(
      BudgetTab.Home to homeStack,
      BudgetTab.Budget to budgetStack,
      BudgetTab.Transactions to transactionsStack,
      BudgetTab.Reports to reportsStack,
      BudgetTab.Schedules to schedulesStack,
      BudgetTab.Rules to rulesStack,
      BudgetTab.Tags to tagsStack,
      BudgetTab.BankSync to bankSyncStack,
    )
  }
}

@Composable
private fun stackWithDefault(default: BudgetNavKey): NavStack<BudgetNavKey> =
  rememberSaveable(saver = budgetNavKeyStackSaver()) {
    NavStackImpl(appCloser = null, stack = mutableStateListOf(default))
  }

@Composable
private fun DrawerNavLayout(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  appStack: NavStack<NavKey>,
  activeStack: NavStack<BudgetNavKey>,
  selectedTab: BudgetTab,
  sidebar: @Composable ColumnScope.(close: () -> Unit) -> Unit,
  modifier: Modifier = Modifier,
) {
  val drawerState = rememberDrawerState(initialValue = Closed)
  val scope = rememberCoroutineScope()
  val openDrawer: () -> Unit =
    remember(drawerState, scope) { { scope.launch { drawerState.open() } } }
  val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

  BackHandler(enabled = drawerState.isOpen) { closeDrawer() }

  // Draw the drawer from the app root so it sits above the bottom status bar
  RootOverlayContent {
    ModalNavigationDrawer(
      modifier =
        Modifier.passTouchesThrough(!drawerState.isOpen && !drawerState.isAnimationRunning),
      drawerState = drawerState,
      // Only allow swiping to close, so opening doesn't clash with horizontal gestures in the
      // content
      gesturesEnabled = drawerState.isOpen,
      drawerContent = { BudgetDrawerSheet { sidebar(closeDrawer) } },
      content = {},
    )
  }

  CompositionLocalProvider(LocalNavDrawerOpener provides openDrawer) {
    BudgetNavDisplay(
      contributors = contributors,
      appStack = appStack,
      activeStack = activeStack,
      selectedTab = selectedTab,
      modifier = modifier.fillMaxSize(),
    )
  }
}

// The drawer's drag handler catches touches even when disabled, so while closed let them through
// to the content underneath
private fun Modifier.passTouchesThrough(enabled: Boolean): Modifier =
  this then PassTouchesThroughElement(enabled)

private data class PassTouchesThroughElement(val enabled: Boolean) :
  ModifierNodeElement<PassTouchesThroughNode>() {
  override fun create() = PassTouchesThroughNode(enabled)

  override fun update(node: PassTouchesThroughNode) {
    node.enabled = enabled
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "passTouchesThrough"
    properties["enabled"] = enabled
  }
}

private class PassTouchesThroughNode(var enabled: Boolean) :
  Modifier.Node(), PointerInputModifierNode {
  override fun onPointerEvent(pointerEvent: PointerEvent, pass: PointerEventPass, bounds: IntSize) =
    Unit

  override fun onCancelPointerInput() = Unit

  override fun sharePointerInputWithSiblings(): Boolean = enabled
}

@Composable
private fun SideNavLayout(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  isDemo: Boolean,
  appStack: NavStack<NavKey>,
  activeStack: NavStack<BudgetNavKey>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onRename: () -> Unit,
  onSetPrivacyMode: (Boolean) -> Unit,
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
) {
  var showMenu by remember { mutableStateOf(false) }
  Row(modifier = modifier.fillMaxSize()) {
    Box(modifier = Modifier.zIndex(1f), contentAlignment = TopStart) {
      SideNavRail(selectedTab, onSelectTab, onMenuClick = { showMenu = true })
      BudgetMenu(
        expanded = showMenu,
        isDemo = isDemo,
        onSelectTab = onSelectTab,
        onRename = onRename,
        onSetPrivacyMode = onSetPrivacyMode,
        onAction = onAction,
        onDismissRequest = { showMenu = false },
        modifier = Modifier.align(TopEnd),
      )
    }
    BudgetNavDisplay(
      contributors = contributors,
      appStack = appStack,
      activeStack = activeStack,
      selectedTab = selectedTab,
      modifier = Modifier.weight(1f),
    )

    if (isMobileLandscape()) {
      SideSpacing()
    }
  }
}

// Wide windows have room to keep the drawer's content open beside the screen
@Composable
private fun SidebarNavLayout(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  appStack: NavStack<NavKey>,
  activeStack: NavStack<BudgetNavKey>,
  selectedTab: BudgetTab,
  sidebar: @Composable ColumnScope.() -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier.fillMaxSize()) {
    PermanentDrawerSheet(
      modifier = Modifier.width(SidebarWidth),
      drawerContainerColor = colors.sidebarBackground,
      drawerContentColor = colors.sidebarItemText,
      content = sidebar,
    )
    BudgetNavDisplay(
      contributors = contributors,
      appStack = appStack,
      activeStack = activeStack,
      selectedTab = selectedTab,
      modifier = Modifier.weight(1f),
    )
  }
}

private val SidebarWidth = 300.dp

@Composable
internal fun BudgetNavDisplay(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  appStack: NavStack<NavKey>,
  activeStack: NavStack<BudgetNavKey>,
  selectedTab: BudgetTab,
  modifier: Modifier = Modifier,
) {
  NavDisplay(
    modifier = modifier,
    backStack = activeStack,
    onBack = { activeStack.pop() },
    transitionSpec = {
      if (budgetTabOf(initialState.key) == budgetTabOf(targetState.key)) {
        slideIntoContainer(towards = Start) togetherWith fadeOut()
      } else {
        ContentTransform(targetContentEnter = None, initialContentExit = None)
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
        val scope = BudgetEntryScope(selectedTab, this)
        for (contributor in contributors) {
          with(contributor) { scope.contribute(activeStack, appStack) }
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
    Column(
      modifier = Modifier.verticalScrollWithBar(),
      horizontalAlignment = CenterHorizontally,
      verticalArrangement = spacedBy(4.dp),
    ) {
      for (tab in PrimaryTabs) {
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
        // secondary tabs are opened from the menu, so highlight it while one of those is showing
        selected = selectedTab in SecondaryTabs,
        onClick = onMenuClick,
        colors = colors.navRailItem(),
      )
    }
  }
}

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

@Composable
internal fun BudgetTab.label(): String =
  when (this) {
    BudgetTab.Home -> Strings.homeTitle
    Budget -> Strings.budgetingTitle
    Transactions -> Strings.transactionsTitle
    BudgetTab.Reports -> Strings.reportsTitle
    Schedules -> Strings.listSchedulesTitle
    Rules -> Strings.rulesTitle
    Tags -> Strings.tagsTitle
    BankSync -> Strings.bankSyncTitle
  }

@Stable
internal fun BudgetTab.icon(): ImageVector =
  when (this) {
    BudgetTab.Home -> MaterialIcons.Home
    Budget -> MaterialIcons.AccountBalanceWallet
    Transactions -> MaterialIcons.ReceiptLong
    BudgetTab.Reports -> AktualIcons.Reports
    Schedules -> AktualIcons.Calendar3
    Rules -> AktualIcons.Tuning
    Tags -> AktualIcons.Tag
    BankSync -> MaterialIcons.AccountBalance
  }

// Less frequently used tabs, which the side rail keeps in its menu
private val SecondaryTabs: ImmutableList<BudgetTab> = persistentListOf(BankSync)

private val PrimaryTabs: ImmutableList<BudgetTab> =
  BudgetTab.entries.filterNot { it in SecondaryTabs }.toImmutableList()

// The sidebar has room for every tab. Transactions are reached through its account list instead
internal val SidebarTabs: ImmutableList<BudgetTab> =
  BudgetTab.entries.filterNot { it == Transactions }.toImmutableList()

private val TabSaver: Saver<BudgetTab, Int> =
  Saver(save = { it.ordinal }, restore = { BudgetTab.entries[it] })

internal fun budgetNavKeyStackSaver() =
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
  isDemo: Boolean,
  onDismissRequest: () -> Unit,
  onSelectTab: (BudgetTab) -> Unit,
  onRename: () -> Unit,
  onSetPrivacyMode: (Boolean) -> Unit,
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
) {
  val isPrivacyEnabled = LocalPrivacyEnabled.current
  Box(modifier = modifier) {
    AktualDropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
      for (tab in SecondaryTabs) {
        AktualDropdownMenuItem(
          text = tab.label(),
          leadingIcon = tab.icon(),
          onClick = {
            onDismissRequest()
            onSelectTab(tab)
          },
        )
      }
      AktualDropdownMenuItem(
        text = Strings.budgetNavMenuRenameBudget,
        leadingIcon = MaterialIcons.Edit,
        onClick = {
          onDismissRequest()
          onRename()
        },
      )
      AktualDropdownMenuItem(
        text =
          if (isPrivacyEnabled) {
            Strings.budgetNavMenuPrivacyOff
          } else {
            Strings.budgetNavMenuPrivacyOn
          },
        leadingIcon =
          if (isPrivacyEnabled) MaterialIcons.VisibilityOff else MaterialIcons.Visibility,
        onClick = {
          onDismissRequest()
          onSetPrivacyMode(!isPrivacyEnabled)
        },
      )
      if (isDemo) {
        AktualDropdownMenuItem(
          text = Strings.budgetNavMenuExitDemo,
          leadingIcon = MaterialIcons.Logout,
          onClick = {
            onDismissRequest()
            onAction(ExitDemo)
          },
        )
      } else {
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
      }
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

@TabletPreview
@Composable
private fun PreviewSideNavRail(@PreviewParameter(ColoredParameters::class) colors: Colors) {
  PreviewWithColors(colors) {
    Row(modifier = Modifier.fillMaxSize()) {
      SideNavRail(selectedTab = Transactions, onSelectTab = {}, onMenuClick = {})
      PreviewContent(modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun PreviewContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Center) {
    Text(text = "Content", style = typography.headlineMedium)
  }
}
