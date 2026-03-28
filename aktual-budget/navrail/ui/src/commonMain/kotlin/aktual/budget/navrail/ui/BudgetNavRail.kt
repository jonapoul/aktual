package aktual.budget.navrail.ui

import aktual.app.nav.BackNavigator
import aktual.app.nav.BudgetNavEntryContributor
import aktual.app.nav.BudgetNavKey
import aktual.app.nav.ReportsListNavRoute
import aktual.app.nav.TransactionsNavRoute
import aktual.app.nav.debugPop
import aktual.budget.model.BudgetId
import aktual.budget.navrail.vm.BudgetNavRailViewModel
import aktual.core.icons.material.BarChart
import aktual.core.icons.material.LinearScale
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.model.Token
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.BottomNavBarSpacing
import aktual.core.ui.BottomStatusBarSpacing
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorScheme
import aktual.core.ui.TabletPreview
import aktual.core.ui.ThemeParameters
import aktual.core.ui.disabled
import aktual.core.ui.isCompactWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.serialization.json.Json

@Composable
fun BudgetNavRail(
  back: BackNavigator,
  token: Token,
  budgetId: BudgetId,
  modifier: Modifier = Modifier,
  viewModel: BudgetNavRailViewModel = metroViewModel(token, budgetId),
) {
  val contributors = viewModel.budgetNavEntryContributors
  var selectedTab by
    rememberSaveable(stateSaver = TabSaver) { mutableStateOf(BudgetTab.Transactions) }

  val transactionsStack =
    rememberSaveable(saver = budgetNavKeyStackSaver()) {
      mutableStateListOf(TransactionsNavRoute(token, budgetId))
    }

  val reportsStack =
    rememberSaveable(saver = budgetNavKeyStackSaver()) {
      mutableStateListOf(ReportsListNavRoute(token, budgetId))
    }

  val tabStacks =
    remember(transactionsStack, reportsStack) {
      mapOf(BudgetTab.Transactions to transactionsStack, BudgetTab.Reports to reportsStack)
    }

  val activeStack: SnapshotStateList<BudgetNavKey> = tabStacks.getValue(selectedTab)

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
      onBack = { if (!activeStack.debugPop()) back() },
      modifier = modifier,
    )
  } else {
    SideNavLayout(
      contributors = contributors,
      activeStack = activeStack,
      selectedTab = selectedTab,
      onSelectTab = onSelectTab,
      onBack = { if (!activeStack.debugPop()) back() },
      modifier = modifier,
    )
  }
}

@Composable
private fun BottomNavLayout(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  activeStack: SnapshotStateList<BudgetNavKey>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxSize()) {
    BudgetNavDisplay(
      contributors = contributors,
      activeStack = activeStack,
      onBack = onBack,
      modifier = Modifier.weight(1f),
    )
    BottomNavBar(selectedTab = selectedTab, onSelectTab = onSelectTab)
    BottomStatusBarSpacing()
    BottomNavBarSpacing()
  }
}

@Composable
private fun SideNavLayout(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  activeStack: SnapshotStateList<BudgetNavKey>,
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier.fillMaxSize()) {
    SideNavRail(selectedTab = selectedTab, onSelectTab = onSelectTab)
    BudgetNavDisplay(
      contributors = contributors,
      activeStack = activeStack,
      onBack = onBack,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun BudgetNavDisplay(
  contributors: ImmutableSet<BudgetNavEntryContributor>,
  activeStack: SnapshotStateList<BudgetNavKey>,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  NavDisplay(
    backStack = activeStack,
    modifier = modifier,
    onBack = { onBack() },
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
private fun BottomNavBar(
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  NavigationBar(
    modifier = modifier,
    containerColor = theme.sidebarBackground,
    contentColor = theme.sidebarItemText,
  ) {
    for (tab in BudgetTab.entries) {
      NavigationBarItem(
        icon = { Icon(tab.icon(), contentDescription = tab.label()) },
        label = { Text(tab.label()) },
        selected = selectedTab == tab,
        onClick = { onSelectTab(tab) },
        colors = theme.navBarItem(),
      )
    }
  }
}

@Composable
private fun SideNavRail(
  selectedTab: BudgetTab,
  onSelectTab: (BudgetTab) -> Unit,
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
        label = { Text(tab.label()) },
        selected = selectedTab == tab,
        onClick = { onSelectTab(tab) },
        colors = theme.navRailItem(),
      )
    }
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
private fun metroViewModel(token: Token, budgetId: BudgetId) =
  assistedMetroViewModel<BudgetNavRailViewModel, BudgetNavRailViewModel.Factory> {
    create(token, budgetId)
  }

private val TabSaver =
  Saver<BudgetTab, Int>(save = { it.ordinal }, restore = { BudgetTab.entries[it] })

private fun budgetNavKeyStackSaver() =
  Saver<SnapshotStateList<BudgetNavKey>, String>(
    save = { stack -> Json.encodeToString(stack.toList()) },
    restore = { json ->
      mutableStateListOf<BudgetNavKey>().apply { addAll(Json.decodeFromString(json)) }
    },
  )

private enum class BudgetTab {
  Transactions,
  Reports,
}

@Composable
private fun BudgetTab.label(): String =
  when (this) {
    BudgetTab.Transactions -> Strings.transactionsTitle
    BudgetTab.Reports -> Strings.reportsTitle
  }

@Composable
private fun BudgetTab.icon(): ImageVector =
  when (this) {
    BudgetTab.Transactions -> MaterialIcons.LinearScale
    BudgetTab.Reports -> MaterialIcons.BarChart
  }

@PortraitPreview
@Composable
private fun PreviewBottomNavBar(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithColorScheme(theme) {
    Column(modifier = Modifier.fillMaxSize()) {
      PreviewContent(modifier = Modifier.weight(1f))
      BottomNavBar(selectedTab = BudgetTab.Transactions, onSelectTab = {})
    }
  }

@TabletPreview
@Composable
private fun PreviewSideNavRail(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithColorScheme(theme) {
    Row(modifier = Modifier.fillMaxSize()) {
      SideNavRail(selectedTab = BudgetTab.Transactions, onSelectTab = {})
      PreviewContent(modifier = Modifier.weight(1f))
    }
  }

@Composable
private fun PreviewContent(modifier: Modifier = Modifier) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(text = "Content", style = AktualTypography.headlineMedium)
  }
}
