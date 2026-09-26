package aktual.budget.navrail.ui

import aktual.budget.navrail.vm.BudgetNavRailViewModel
import aktual.budget.navrail.vm.DrawerHeaderState
import aktual.core.icons.AktualIcons
import aktual.core.icons.Calendar3
import aktual.core.icons.Reports
import aktual.core.icons.Tag
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
import aktual.core.ui.ColoredParameters
import aktual.core.ui.LocalNavDrawerOpener
import aktual.core.ui.LocalRootOverlay
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.SideSpacing
import aktual.core.ui.TabletPreview
import aktual.core.ui.disabled
import aktual.core.ui.isCompactWidth
import aktual.core.ui.isMobileLandscape
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

@Composable
internal fun BudgetNavRail(
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
  viewModel: BudgetNavRailViewModel = metroViewModel(),
) {
  val contributors = viewModel.budgetNavEntryContributors
  val headerState by viewModel.headerState.collectAsState()

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

  // NavDisplay only handles back when the stack has more than one entry
  BackHandler(enabled = selectedTab != Transactions && activeStack.size == 1) {
    selectedTab = Transactions
  }

  if (isCompactWidth()) {
    DrawerNavLayout(
      contributors = contributors,
      headerState = headerState,
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
private fun stackWithDefault(default: BudgetNavKey): NavStack<BudgetNavKey> =
  rememberSaveable(saver = budgetNavKeyStackSaver()) {
    NavStackImpl(appCloser = null, stack = mutableStateListOf(default))
  }

@Composable
private fun DrawerNavLayout(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  headerState: DrawerHeaderState,
  activeStack: NavStack<BudgetNavKey>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
) {
  val drawerState = rememberDrawerState(initialValue = Closed)
  val scope = rememberCoroutineScope()
  val openDrawer: () -> Unit =
    remember(drawerState, scope) { { scope.launch { drawerState.open() } } }
  val closeDrawer: () -> Unit = { scope.launch { drawerState.close() } }

  BackHandler(enabled = drawerState.isOpen) { closeDrawer() }

  val drawer by
    rememberUpdatedState<@Composable () -> Unit> {
      ModalNavigationDrawer(
        modifier =
          Modifier.passTouchesThrough(!drawerState.isOpen && !drawerState.isAnimationRunning),
        drawerState = drawerState,
        // Only allow swiping to close, so opening doesn't clash with horizontal gestures in the
        // content
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
          BudgetDrawerSheet(
            headerState = headerState,
            selectedTab = selectedTab,
            onSelectTab = { tab ->
              closeDrawer()
              onSelectTab(tab)
            },
            onAction = { action ->
              closeDrawer()
              onAction(action)
            },
          )
        },
        content = {},
      )
    }

  // Draw the drawer from the app root so it sits above the bottom status bar
  val rootOverlay = LocalRootOverlay.current
  DisposableEffect(rootOverlay) {
    val content: @Composable () -> Unit = { drawer() }
    rootOverlay.content = content
    onDispose { if (rootOverlay.content === content) rootOverlay.content = null }
  }

  CompositionLocalProvider(LocalNavDrawerOpener provides openDrawer) {
    BudgetNavDisplay(
      contributors = contributors,
      activeStack = activeStack,
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
private fun BudgetDrawerSheet(
  headerState: DrawerHeaderState,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
) {
  ModalDrawerSheet(
    modifier = modifier,
    drawerContainerColor = colors.sidebarBackground,
    drawerContentColor = colors.sidebarItemText,
  ) {
    Column(modifier = Modifier.verticalScrollWithBar().padding(12.dp)) {
      DrawerHeader(
        state = headerState,
        modifier = Modifier.padding(bottom = 12.dp),
      )

      for (tab in BudgetTab.entries) {
        DrawerItem(
          icon = tab.icon(),
          label = tab.label(),
          selected = tab == selectedTab,
          onClick = { onSelectTab(tab) },
        )
      }

      HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = colors.sidebarItemText.disabled,
      )

      DrawerItem(
        icon = MaterialIcons.SwapHoriz,
        label = Strings.budgetNavMenuSwitchBudget,
        onClick = { onAction(SwitchFile) },
      )
      DrawerItem(
        icon = MaterialIcons.Logout,
        label = Strings.budgetNavMenuLogOut,
        onClick = { onAction(LogOut) },
      )
      DrawerItem(
        icon = MaterialIcons.Settings,
        label = Strings.budgetNavMenuSettings,
        onClick = { onAction(Settings) },
      )
      DrawerItem(
        icon = MaterialIcons.Info,
        label = Strings.budgetNavMenuAbout,
        onClick = { onAction(About) },
      )
    }
  }
}

@Composable
private fun DrawerHeader(
  state: DrawerHeaderState,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(colors.sidebarItemBackgroundHover)
        .padding(16.dp)
  ) {
    Text(
      text = state.budgetName.orEmpty(),
      style = typography.titleMedium,
      fontWeight = Bold,
      color = colors.sidebarBudgetName,
      maxLines = 1,
      overflow = Ellipsis,
    )
    Text(
      text = state.serverHost,
      style = typography.bodySmall,
      color = colors.sidebarTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )
  }
}

@Composable
private fun DrawerItem(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
) {
  NavigationDrawerItem(
    modifier = modifier,
    icon = { Icon(icon, contentDescription = null) },
    label = { Text(text = label) },
    selected = selected,
    onClick = onClick,
    colors = colors.drawerItem(),
  )
}

@Composable
private fun SideNavLayout(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  activeStack: NavStack<BudgetNavKey>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onAction: BudgetNavActionHandler,
  modifier: Modifier = Modifier,
) {
  var showMenu by remember { mutableStateOf(false) }
  Row(modifier = modifier.fillMaxSize()) {
    Box(modifier = Modifier.zIndex(1f), contentAlignment = TopStart) {
      SideNavRail(selectedTab, onSelectTab, onMenuClick = { showMenu = true })
      BudgetMenu(
        expanded = showMenu,
        onAction = onAction,
        onDismissRequest = { showMenu = false },
        modifier = Modifier.align(TopEnd),
      )
    }
    BudgetNavDisplay(
      contributors = contributors,
      activeStack = activeStack,
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
  modifier: Modifier = Modifier,
) {
  NavDisplay(
    modifier = modifier,
    backStack = activeStack,
    onBack = { activeStack.pop() },
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
    for (tab in BudgetTab.entries) {
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

@Composable
private fun Colors.drawerItem(): NavigationDrawerItemColors =
  NavigationDrawerItemDefaults.colors(
    selectedContainerColor = sidebarItemTextSelected.disabled,
    unselectedContainerColor = Transparent,
    selectedIconColor = sidebarItemTextSelected,
    unselectedIconColor = sidebarItemText,
    selectedTextColor = sidebarItemTextSelected,
    unselectedTextColor = sidebarItemText,
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

@Composable
private fun BudgetTab.label(): String =
  when (this) {
    Transactions -> Strings.transactionsTitle
    BudgetTab.Reports -> Strings.reportsTitle
    Schedules -> Strings.listSchedulesTitle
    Rules -> Strings.rulesTitle
    Tags -> Strings.tagsTitle
  }

@Stable
private fun BudgetTab.icon(): ImageVector =
  when (this) {
    Transactions -> MaterialIcons.LinearScale
    BudgetTab.Reports -> AktualIcons.Reports
    Schedules -> AktualIcons.Calendar3
    Rules -> AktualIcons.Tuning
    Tags -> AktualIcons.Tag
  }

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
private fun PreviewBudgetDrawerSheet(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    BudgetDrawerSheet(
      headerState =
        DrawerHeaderState(
          budgetName = "My Budget",
          serverHost = "actual.example.com",
        ),
      selectedTab = Transactions,
      onSelectTab = {},
      onAction = {},
    )
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
