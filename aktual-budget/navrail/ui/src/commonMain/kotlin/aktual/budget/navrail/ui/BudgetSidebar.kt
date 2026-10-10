package aktual.budget.navrail.ui

import aktual.budget.model.AccountGroup
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.navrail.vm.DrawerAccount
import aktual.budget.navrail.vm.DrawerAccountSection
import aktual.budget.navrail.vm.DrawerAccounts
import aktual.budget.navrail.vm.DrawerHeaderState
import aktual.core.icons.material.Edit
import aktual.core.icons.material.ExpandMore
import aktual.core.icons.material.Info
import aktual.core.icons.material.Logout
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Settings
import aktual.core.icons.material.SwapHoriz
import aktual.core.icons.material.Visibility
import aktual.core.icons.material.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.model.unaryPlus
import aktual.core.nav.AccountGroupTransactionsNavRoute
import aktual.core.nav.AccountTransactionsNavRoute
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.BudgetTab
import aktual.core.nav.TransactionsNavRoute
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareIconButton
import aktual.core.ui.ColoredParameters
import aktual.core.ui.IconButtonColorProvider
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.bareIconButton
import aktual.core.ui.disabled
import aktual.core.ui.formattedText
import aktual.core.ui.normalIconButton
import aktual.core.ui.verticalScrollWithBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun BudgetDrawerSheet(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  ModalDrawerSheet(
    modifier = modifier,
    drawerContainerColor = colors.sidebarBackground,
    drawerContentColor = colors.sidebarItemText,
    content = content,
  )
}

// transactionsRoot is what the transactions tab is showing, or null if another tab is selected
@Composable
internal fun ColumnScope.BudgetSidebarContent(
  headerState: DrawerHeaderState,
  accounts: DrawerAccounts,
  selectedTab: BudgetTab,
  transactionsRoot: BudgetNavKey?,
  onSelectTab: (BudgetTab) -> Unit,
  onOpenTransactions: (BudgetNavKey.Transactions) -> Unit,
  onRename: () -> Unit,
  onSetPrivacyMode: (Boolean) -> Unit,
  onAction: BudgetNavActionHandler,
) {
  Column(modifier = Modifier.weight(1f).verticalScrollWithBar().padding(12.dp)) {
    DrawerHeader(
      state = headerState,
      onRename = onRename,
      modifier = Modifier.padding(bottom = 12.dp),
    )

    SidebarTabs.fastForEach { tab ->
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
      icon = BudgetTab.Transactions.icon(),
      label = Strings.budgetNavAccountsAll,
      amount = accounts.total,
      selected =
        transactionsRoot != null &&
          transactionsRoot !is AccountTransactionsNavRoute &&
          transactionsRoot !is AccountGroupTransactionsNavRoute,
      onClick = { onOpenTransactions(TransactionsNavRoute) },
    )

    AccountGroup.entries.fastForEach { group ->
      AccountSection(
        group = group,
        section = accounts[group],
        transactionsRoot = transactionsRoot,
        onOpenTransactions = onOpenTransactions,
      )
    }
  }

  HorizontalDivider(
    modifier = Modifier.padding(horizontal = 12.dp),
    color = colors.sidebarItemText.disabled,
  )

  DrawerActions(
    modifier = Modifier.padding(12.dp),
    actions = drawerActions(headerState.isDemo, onSetPrivacyMode, onAction),
  )
}

@Composable
private fun AccountSection(
  group: AccountGroup,
  section: DrawerAccountSection,
  transactionsRoot: BudgetNavKey?,
  onOpenTransactions: (BudgetNavKey.Transactions) -> Unit,
) {
  if (section.accounts.isEmpty()) return

  var isExpanded by rememberSaveable { mutableStateOf(group != Closed) }
  val route = AccountGroupTransactionsNavRoute(group)

  AccountSectionHeader(
    title = group.label(),
    // As upstream, closed accounts have no total
    total = section.total.takeIf { group != Closed },
    isExpanded = isExpanded,
    selected = transactionsRoot == route,
    onToggle = { isExpanded = !isExpanded },
    onClick = { onOpenTransactions(route) },
  )

  AnimatedVisibility(
    visible = isExpanded,
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut(),
  ) {
    Column {
      section.accounts.fastForEach { account ->
        val accountRoute = AccountTransactionsNavRoute(account.id)
        AccountItem(
          account = account,
          selected = transactionsRoot == accountRoute,
          onClick = { onOpenTransactions(accountRoute) },
        )
      }
    }
  }
}

// Tapping the row opens the group's transactions, so collapsing has its own button
@Composable
private fun AccountSectionHeader(
  title: String,
  total: Amount?,
  isExpanded: Boolean,
  selected: Boolean,
  onToggle: () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val color = colors.sidebarText(selected)
  val rotation by animateFloatAsState(if (isExpanded) 0f else COLLAPSED_ROTATION)

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(DrawerItemHeight)
        .sidebarRow(colors, selected, onClick)
        .padding(start = 4.dp, end = 24.dp),
    verticalAlignment = CenterVertically,
  ) {
    IconButton(onClick = onToggle) {
      Icon(
        modifier = Modifier.rotate(rotation),
        imageVector = MaterialIcons.ExpandMore,
        contentDescription =
          if (isExpanded) {
            Strings.budgetNavAccountsCollapse(title)
          } else {
            Strings.budgetNavAccountsExpand(title)
          },
        tint = color,
      )
    }

    Text(
      modifier = Modifier.weight(1f).semantics { heading() },
      text = title,
      style = typography.labelLarge,
      fontWeight = SemiBold,
      color = color,
      maxLines = 1,
      overflow = Ellipsis,
    )

    if (total != null) {
      BalanceText(amount = total, style = typography.labelLarge, color = color)
    }
  }
}

@Composable
private fun AccountItem(
  account: DrawerAccount,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val color = colors.sidebarText(selected)

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(AccountItemHeight)
        .sidebarRow(colors, selected, onClick)
        .padding(start = AccountNameIndent, end = 24.dp),
    horizontalArrangement = spacedBy(12.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = account.name,
      style = typography.bodyMedium,
      color = color,
      maxLines = 1,
      overflow = Ellipsis,
    )

    BalanceText(amount = account.balance, style = typography.bodyMedium, color = color)
  }
}

@Composable
private fun BalanceText(
  amount: Amount,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalTextStyle.current,
  color: Color = Unspecified,
) =
  Text(
    modifier = modifier,
    text = amount.formattedText(),
    style = style.copy(fontFeatureSettings = "tnum"),
    color = color,
    maxLines = 1,
  )

private fun Modifier.sidebarRow(theme: Colors, selected: Boolean, onClick: () -> Unit): Modifier =
  clip(CircleShape)
    .background(if (selected) theme.sidebarItemTextSelected.disabled else Transparent)
    .selectable(selected = selected, onClick = onClick)

@Stable
private fun Colors.sidebarText(selected: Boolean): Color =
  if (selected) sidebarItemTextSelected else sidebarItemText

@Composable
private fun AccountGroup.label(): String =
  when (this) {
    OnBudget -> Strings.budgetNavAccountsOnBudget
    OffBudget -> Strings.budgetNavAccountsOffBudget
    Closed -> Strings.budgetNavAccountsClosed
  }

private const val COLLAPSED_ROTATION = -90f

private val AccountItemHeight = 36.dp

// Lines account names up with the labels of the items above, past their icons
private val AccountNameIndent = 52.dp

@Immutable
private data class DrawerAction(
  val icon: ImageVector,
  val label: String,
  val onClick: () -> Unit,
)

@Composable
private fun drawerActions(
  isDemo: Boolean,
  onSetPrivacyMode: (Boolean) -> Unit,
  onAction: BudgetNavActionHandler,
): ImmutableList<DrawerAction> = buildList {
  if (LocalPrivacyEnabled.current) {
    +DrawerAction(
      icon = MaterialIcons.VisibilityOff,
      label = Strings.budgetNavMenuPrivacyOff,
      onClick = { onSetPrivacyMode(false) },
    )
  } else {
    +DrawerAction(
      icon = MaterialIcons.Visibility,
      label = Strings.budgetNavMenuPrivacyOn,
      onClick = { onSetPrivacyMode(true) },
    )
  }
  if (!isDemo) {
    +DrawerAction(
      icon = MaterialIcons.SwapHoriz,
      label = Strings.budgetNavMenuSwitchBudget,
      onClick = { onAction(SwitchFile) },
    )
  }
  +DrawerAction(
    icon = MaterialIcons.Settings,
    label = Strings.budgetNavMenuSettings,
    onClick = { onAction(Settings) },
  )
  +DrawerAction(
    icon = MaterialIcons.Info,
    label = Strings.budgetNavMenuAbout,
    onClick = { onAction(About) },
  )
  if (isDemo) {
    +DrawerAction(
      icon = MaterialIcons.Logout,
      label = Strings.budgetNavMenuExitDemo,
      onClick = { onAction(ExitDemo) },
    )
  } else {
    +DrawerAction(
      icon = MaterialIcons.Logout,
      label = Strings.budgetNavMenuLogOut,
      onClick = { onAction(LogOut) },
    )
  }
}
  .toImmutableList()

@Composable
private fun DrawerActions(actions: ImmutableList<DrawerAction>, modifier: Modifier = Modifier) {
  Row(modifier = modifier, horizontalArrangement = spacedBy(8.dp)) {
    actions.fastForEach { action ->
      NormalIconButton(
        modifier = Modifier.weight(1f),
        imageVector = action.icon,
        contentDescription = action.label,
        colors = DrawerButtonColors,
        onClick = action.onClick,
      )
    }
  }
}

private val DrawerButtonColors = IconButtonColorProvider { theme, isPressed ->
  theme
    .normalIconButton(isPressed)
    .copy(
      containerColor =
        if (isPressed) theme.sidebarItemTextSelected.disabled else theme.sidebarItemBackgroundHover,
      contentColor = if (isPressed) theme.sidebarItemTextSelected else theme.sidebarItemText,
      disabledContainerColor = theme.sidebarItemBackgroundHover.disabled,
      disabledContentColor = theme.sidebarItemText.disabled,
    )
}

@Composable
private fun DrawerHeader(
  state: DrawerHeaderState,
  onRename: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(colors.sidebarItemBackgroundHover)
        .padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
    verticalAlignment = CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = state.budgetName.orEmpty(),
        style = typography.titleMedium,
        fontWeight = Bold,
        color = colors.sidebarBudgetName,
        maxLines = 1,
        overflow = Ellipsis,
      )
      Text(
        text = state.serverHost ?: Strings.budgetNavDemo,
        style = typography.bodySmall,
        color = colors.sidebarTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )
    }

    BareIconButton(
      imageVector = MaterialIcons.Edit,
      contentDescription = Strings.budgetNavMenuRenameBudget,
      colors = RenameButtonColors,
      onClick = onRename,
    )
  }
}

private val RenameButtonColors = IconButtonColorProvider { theme, isPressed ->
  theme
    .bareIconButton(isPressed)
    .copy(
      containerColor = if (isPressed) theme.sidebarItemTextSelected.disabled else Transparent,
      contentColor = if (isPressed) theme.sidebarItemTextSelected else theme.sidebarBudgetName,
    )
}

// Material's default is 56dp
private val DrawerItemHeight = 44.dp

@Composable
private fun DrawerItem(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
  amount: Amount? = null,
) {
  NavigationDrawerItem(
    modifier = modifier.height(DrawerItemHeight),
    icon = { Icon(icon, contentDescription = null) },
    label = { Text(text = label, maxLines = 1, overflow = Ellipsis) },
    badge = amount?.let { { BalanceText(it) } },
    selected = selected,
    onClick = onClick,
    colors = colors.drawerItem(),
  )
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
    selectedBadgeColor = sidebarItemTextSelected,
    unselectedBadgeColor = sidebarItemText,
  )

@PortraitPreview
@Composable
private fun PreviewBudgetDrawerSheet(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    BudgetDrawerSheet {
      BudgetSidebarContent(
        headerState =
          DrawerHeaderState(
            budgetName = "My Budget",
            serverHost = "actual.example.com",
          ),
        accounts = PREVIEW_ACCOUNTS,
        selectedTab = Transactions,
        transactionsRoot = AccountTransactionsNavRoute(AccountId("Joint Account")),
        onSelectTab = {},
        onOpenTransactions = {},
        onRename = {},
        onSetPrivacyMode = {},
        onAction = {},
      )
    }
  }

private val PREVIEW_ACCOUNTS =
  DrawerAccounts(
    onBudget =
      section(
        account("Current Account", 2_431.18),
        account("Joint Account", 1_204.50),
        account("Credit Card", -642.37),
      ),
    offBudget = section(account("Stocks ISA", 14_220.91), account("Car Loan", -6_300.00)),
    closed = section(account("Old Savings", 0.0)),
  )

private fun section(vararg accounts: DrawerAccount) =
  DrawerAccountSection(
    accounts = persistentListOf(*accounts),
    total = accounts.fold(Amount.Zero) { sum, account -> sum + account.balance },
  )

private fun account(name: String, balance: Double) =
  DrawerAccount(id = AccountId(name), name = name, balance = Amount(balance))
