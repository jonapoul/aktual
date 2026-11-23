package aktual.budget.navrail.ui

import aktual.budget.accounts.components.AccountSyncDot
import aktual.budget.accounts.components.NewTransactionsBadge
import aktual.budget.model.AccountGroup
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.AccountSyncState
import aktual.budget.model.AccountSyncState.Failed
import aktual.budget.model.AccountSyncState.Ok
import aktual.budget.model.Amount
import aktual.budget.model.BudgetTab
import aktual.budget.navrail.vm.DrawerAccount
import aktual.budget.navrail.vm.DrawerAccountBucket
import aktual.budget.navrail.vm.DrawerAccountGroup
import aktual.budget.navrail.vm.DrawerAccountSection
import aktual.budget.navrail.vm.DrawerAccounts
import aktual.budget.navrail.vm.DrawerHeaderState
import aktual.budget.navrail.vm.SidebarAccountsAction
import aktual.budget.navrail.vm.SidebarAccountsAction.Search
import aktual.budget.navrail.vm.SidebarAccountsAction.ToggleSection
import aktual.budget.navrail.vm.SidebarAccountsState
import aktual.budget.navrail.vm.SidebarSection
import aktual.budget.navrail.vm.SidebarSyncStatus
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheveronDown
import aktual.core.icons.CheveronDownUp
import aktual.core.icons.CheveronUpDown
import aktual.core.icons.CloudCheck
import aktual.core.icons.CloudDownload
import aktual.core.icons.CloudUnknown
import aktual.core.icons.CloudWarning
import aktual.core.icons.Code
import aktual.core.icons.Cog
import aktual.core.icons.Delete
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.Download
import aktual.core.icons.InformationOutline
import aktual.core.icons.Pencil1
import aktual.core.icons.SearchAlternate
import aktual.core.icons.ViewHide
import aktual.core.icons.ViewShow
import aktual.core.icons.material.Logout
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.SwapHoriz
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.model.unaryPlus
import aktual.core.nav.AccountGroupTransactionsNavRoute
import aktual.core.nav.AccountTransactionsNavRoute
import aktual.core.nav.BudgetNavKey
import aktual.core.nav.ManageAccountsNavRoute
import aktual.core.nav.TransactionsNavRoute
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BasicIconButton
import aktual.core.ui.ChipSize
import aktual.core.ui.ColorParameters
import aktual.core.ui.IconButtonColorProvider
import aktual.core.ui.LocalPrivacyEnabled
import aktual.core.ui.NoticeBanner
import aktual.core.ui.OnAction
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.bareIconButton
import aktual.core.ui.disabled
import aktual.core.ui.formattedText
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.normalIconButton
import aktual.core.ui.textField
import aktual.core.ui.verticalScrollWithBar
import aktual.theme.Colors
import aktual.theme.LocalColors
import aktual.theme.sidebarColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterEnd
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun BudgetDrawerSheet(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit,
) {
  ProvideSidebarColors {
    ModalDrawerSheet(
      modifier = modifier,
      drawerContainerColor = colors.sidebarBackground,
      drawerContentColor = colors.sidebarItemText,
      content = content,
    )
  }
}

// The built-in light theme draws the sidebar with its own palette, see sidebarColors()
@Composable
internal fun ProvideSidebarColors(content: @Composable () -> Unit) {
  val sidebarColors = colors.sidebarColors()
  CompositionLocalProvider(LocalColors provides sidebarColors, content = content)
}

// transactionsRoot is what the transactions tab is showing, or null if another tab is selected
@Composable
internal fun ColumnScope.BudgetSidebarContent(
  headerState: DrawerHeaderState,
  accounts: SidebarAccountsState,
  selectedTab: BudgetTab,
  transactionsRoot: BudgetNavKey?,
  onSelectTab: (BudgetTab) -> Unit,
  onOpenTransactions: (BudgetNavKey.Transactions) -> Unit,
  onSetPrivacyMode: (Boolean) -> Unit,
  onAccountsAction: OnAction<SidebarAccountsAction>,
  onAction: OnAction<BudgetMenuAction>,
  hasNightlyUpdate: Boolean = false,
) {
  Column(modifier = Modifier.weight(1f).verticalScrollWithBar().padding(12.dp)) {
    DrawerHeader(
      state = headerState,
      onAction = onAction,
      modifier = Modifier.padding(bottom = 12.dp),
    )

    if (hasNightlyUpdate) {
      NoticeBanner(
        modifier = Modifier.padding(bottom = 12.dp),
        text = Strings.budgetNavNightlyBanner,
        icon = AktualIcons.CloudDownload,
        onClick = { onAction(OpenNightlyUpdate) },
      )
    }

    SidebarTabs.fastForEach { tab ->
      DrawerItem(
        icon = tab.icon(),
        label = tab.label(),
        selected = tab == selectedTab,
        onClick = { onSelectTab(tab) },
      )
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = colors.sidebarBorder)

    AccountsHeader(
      state = accounts,
      selected =
        transactionsRoot != null &&
          transactionsRoot !is AccountTransactionsNavRoute &&
          transactionsRoot !is AccountGroupTransactionsNavRoute &&
          transactionsRoot !is ManageAccountsNavRoute,
      isManageSelected = transactionsRoot is ManageAccountsNavRoute,
      onClick = { onOpenTransactions(TransactionsNavRoute) },
      onManage = { onOpenTransactions(ManageAccountsNavRoute) },
      onAction = onAccountsAction,
    )

    val search = accounts.search
    if (search != null) {
      AccountSearchField(
        initialQuery = search,
        onQueryChange = { onAccountsAction(Search(it)) },
        onClose = { onAccountsAction(Search(null)) },
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
      )
    }

    AccountGroup.entries.fastForEach { group ->
      AccountSection(
        group = group,
        section = accounts.accounts[group],
        state = accounts,
        transactionsRoot = transactionsRoot,
        onOpenTransactions = onOpenTransactions,
        onToggle = { onAccountsAction(ToggleSection(it)) },
      )
    }
  }

  HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp), color = colors.sidebarBorder)

  DrawerActions(
    modifier = Modifier.padding(12.dp),
    actions = drawerActions(headerState.isDemo, onSetPrivacyMode, onAction),
  )
}

// packages/desktop-client/src/components/sidebar/redesign/AccountsHeaderRow.tsx
@Composable
private fun AccountsHeader(
  state: SidebarAccountsState,
  selected: Boolean,
  isManageSelected: Boolean,
  onClick: () -> Unit,
  onManage: () -> Unit,
  onAction: OnAction<SidebarAccountsAction>,
  modifier: Modifier = Modifier,
) {
  val color = if (selected) colors.sidebarItemTextSelected else colors.sidebarHeaderText

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .sidebarRow(colors, selected, onClick)
        .padding(start = 16.dp, top = 4.dp, end = 4.dp, bottom = 4.dp),
    verticalAlignment = CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        modifier = Modifier.semantics { heading() },
        text = Strings.budgetNavAccounts,
        style = typography.labelLarge,
        fontWeight = Bold,
        color = color,
        maxLines = 1,
        overflow = Ellipsis,
      )
      BalanceText(
        amount = state.accounts.total,
        style = typography.bodySmall,
        color = if (selected) colors.sidebarItemTextSelected else colors.sidebarTextSubdued,
      )
    }

    val toggleAllLabel =
      if (state.allOpen) {
        Strings.budgetNavAccountsCollapseAll
      } else {
        Strings.budgetNavAccountsExpandAll
      }
    HeaderIconButton(
      icon = if (state.allOpen) AktualIcons.CheveronDownUp else AktualIcons.CheveronUpDown,
      label = toggleAllLabel,
      isEnabled = !state.isSearching,
      onClick = { onAction(ToggleAllSections) },
    )

    HeaderIconButton(
      icon = AktualIcons.SearchAlternate,
      label = Strings.budgetNavAccountsFind,
      isToggledOn = state.search != null,
      onClick = { onAction(Search(if (state.search == null) "" else null)) },
    )

    HeaderIconButton(
      icon = AktualIcons.Pencil1,
      label = Strings.accountsManageTitle,
      isToggledOn = isManageSelected,
      onClick = onManage,
    )
  }
}

// packages/desktop-client/src/components/sidebar/redesign/SidebarIconButton.tsx
@Composable
private fun HeaderIconButton(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isEnabled: Boolean = true,
  isToggledOn: Boolean = false,
) {
  BasicIconButton(
    modifier = modifier,
    imageVector = icon,
    contentDescription = label,
    size = HeaderIconSize,
    isEnabled = isEnabled,
    colors =
      IconButtonColorProvider { theme, isPressed ->
        val isHighlighted = isPressed || isToggledOn
        theme
          .bareIconButton(isPressed)
          .copy(
            containerColor = if (isHighlighted) theme.sidebarControlBackground else Transparent,
            contentColor =
              if (isHighlighted) theme.sidebarItemTextSelected else theme.sidebarTextSubdued,
            disabledContainerColor = Transparent,
            disabledContentColor = theme.sidebarTextSubdued.disabled,
          )
      },
    onClick = onClick,
  )
}

private val HeaderIconSize = 16.dp

// packages/desktop-client/src/components/sidebar/redesign/AccountSearchField.tsx
@Composable
private fun AccountSearchField(
  initialQuery: String,
  onQueryChange: (String) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state = rememberTextFieldState(initialText = initialQuery)
  val currentOnQueryChange by rememberUpdatedState(onQueryChange)
  val focusRequester = keyboardFocusRequester()

  LaunchedEffect(state) {
    snapshotFlow { state.text.toString() }.collect { query -> currentOnQueryChange(query) }
  }

  ProvideTextStyle(typography.bodyMedium) {
    AktualTextField(
      modifier = modifier.fillMaxWidth().focusRequester(focusRequester),
      state = state,
      placeholderText = Strings.budgetNavAccountsFind,
      singleLine = true,
      showBorder = false,
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
      colors =
        colors.textField(
          focusedContainer = colors.sidebarControlBackground,
          border = Transparent,
          text = colors.sidebarItemText,
          textPlaceholder = colors.sidebarTextSubdued,
          icon = colors.sidebarTextSubdued,
        ),
      trailingIcon = {
        BasicIconButton(
          imageVector = AktualIcons.Delete,
          contentDescription = Strings.budgetNavAccountsFindClose,
          size = HeaderIconSize,
          colors =
            IconButtonColorProvider { theme, isPressed ->
              theme.bareIconButton(isPressed).copy(contentColor = theme.sidebarTextSubdued)
            },
          onClick = onClose,
        )
      },
    )
  }
}

@Composable
private fun AccountSection(
  group: AccountGroup,
  section: DrawerAccountSection,
  state: SidebarAccountsState,
  transactionsRoot: BudgetNavKey?,
  onOpenTransactions: (BudgetNavKey.Transactions) -> Unit,
  onToggle: (SidebarSection) -> Unit,
) {
  if (section.accounts.isEmpty()) return

  val sidebarSection = SidebarSection.of(group)
  val isExpanded = state.isOpen(sidebarSection)
  val isClosed = group == Closed
  val route = AccountGroupTransactionsNavRoute(group)

  AccountSectionHeader(
    title = group.label(),
    // As upstream, closed accounts have no total
    total = section.total.takeUnless { isClosed },
    accountCount = section.accountCount,
    failedCount = if (isClosed) 0 else section.failedCount,
    isExpanded = isExpanded,
    isMuted = isClosed,
    selected = transactionsRoot == route,
    onToggle = { onToggle(sidebarSection) },
    onClick = { onOpenTransactions(route) },
  )

  AnimatedVisibility(
    visible = isExpanded,
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut(),
  ) {
    Column {
      section.buckets.fastForEach { bucket ->
        val accountGroup = bucket.group
        if (accountGroup == null) {
          AccountItems(
            accounts = bucket.accounts,
            indent = AccountNameIndent,
            isMuted = isClosed,
            transactionsRoot = transactionsRoot,
            onOpenTransactions = onOpenTransactions,
          )
        } else {
          AccountGroupBucket(
            group = accountGroup,
            bucket = bucket,
            section = SidebarSection.group(accountGroup.id, offBudget = group == OffBudget),
            state = state,
            transactionsRoot = transactionsRoot,
            onOpenTransactions = onOpenTransactions,
            onToggle = onToggle,
          )
        }
      }
    }
  }
}

// packages/desktop-client/src/components/sidebar/redesign/AccountGroupHeader.tsx
@Composable
private fun AccountGroupBucket(
  group: DrawerAccountGroup,
  bucket: DrawerAccountBucket,
  section: SidebarSection,
  state: SidebarAccountsState,
  transactionsRoot: BudgetNavKey?,
  onOpenTransactions: (BudgetNavKey.Transactions) -> Unit,
  onToggle: (SidebarSection) -> Unit,
) {
  val isExpanded = state.isOpen(section)
  val rotation by animateFloatAsState(if (isExpanded) 0f else COLLAPSED_ROTATION)
  val toggleDescription =
    if (isExpanded) {
      Strings.budgetNavAccountsCollapse(group.name)
    } else {
      Strings.budgetNavAccountsExpand(group.name)
    }

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .height(AccountItemHeight)
        .clip(SelectedShape)
        .clickable(onClickLabel = toggleDescription, onClick = { onToggle(section) })
        .padding(start = GroupHeaderIndent, end = 24.dp),
    horizontalArrangement = spacedBy(HeaderGap),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      modifier = Modifier.size(GroupChevronSize).rotate(rotation),
      imageVector = AktualIcons.CheveronDown,
      contentDescription = null,
      tint = colors.sidebarTextSubdued,
    )

    HeaderTitle(
      title = group.name,
      accountCount = bucket.accounts.size.takeUnless { isExpanded },
      failedCount = bucket.failedCount,
      style = typography.labelMedium,
      color = colors.sidebarTextSubdued,
      modifier = Modifier.weight(1f),
    )

    BalanceText(
      amount = bucket.total,
      style = typography.bodySmall,
      color = colors.sidebarTextSubdued,
    )
  }

  AnimatedVisibility(
    visible = isExpanded,
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut(),
  ) {
    Column {
      AccountItems(
        accounts = bucket.accounts,
        indent = GroupedAccountNameIndent,
        isMuted = false,
        transactionsRoot = transactionsRoot,
        onOpenTransactions = onOpenTransactions,
      )
    }
  }
}

@Composable
private fun AccountItems(
  accounts: ImmutableList<DrawerAccount>,
  indent: Dp,
  isMuted: Boolean,
  transactionsRoot: BudgetNavKey?,
  onOpenTransactions: (BudgetNavKey.Transactions) -> Unit,
) {
  accounts.fastForEach { account ->
    val accountRoute = AccountTransactionsNavRoute(account.id)
    AccountItem(
      account = account,
      indent = indent,
      isMuted = isMuted,
      selected = transactionsRoot == accountRoute,
      onClick = { onOpenTransactions(accountRoute) },
    )
  }
}

// Tapping the row opens the group's transactions, so collapsing has its own button
@Composable
private fun AccountSectionHeader(
  title: String,
  total: Amount?,
  accountCount: Int,
  failedCount: Int,
  isExpanded: Boolean,
  isMuted: Boolean,
  selected: Boolean,
  onToggle: () -> Unit,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val color =
    when {
      selected -> colors.sidebarItemTextSelected
      isMuted -> colors.sidebarTextMuted
      else -> colors.sidebarItemText
    }
  val rotation by animateFloatAsState(if (isExpanded) 0f else COLLAPSED_ROTATION)

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(DrawerItemHeight)
        .sidebarRow(colors, selected, onClick)
        .padding(start = SectionHeaderStart, end = 24.dp),
    horizontalArrangement = spacedBy(HeaderGap),
    verticalAlignment = CenterVertically,
  ) {
    val toggleDescription =
      if (isExpanded) {
        Strings.budgetNavAccountsCollapse(title)
      } else {
        Strings.budgetNavAccountsExpand(title)
      }
    BasicIconButton(
      modifier = Modifier.size(SectionChevronSize),
      imageVector = AktualIcons.CheveronDown,
      contentDescription = toggleDescription,
      colors =
        IconButtonColorProvider { theme, isPressed ->
          theme.bareIconButton(isPressed).copy(contentColor = color)
        },
      onClick = onToggle,
    ) {
      Icon(
        modifier = Modifier.rotate(rotation),
        imageVector = AktualIcons.CheveronDown,
        contentDescription = toggleDescription,
      )
    }

    HeaderTitle(
      title = title,
      accountCount = accountCount.takeUnless { isExpanded },
      failedCount = failedCount,
      style = typography.labelLarge,
      color = color,
      modifier = Modifier.weight(1f),
    )

    if (total != null) {
      BalanceText(amount = total, style = typography.labelLarge, color = color)
    }
  }
}

// Fills the row's free space, so the title only shortens when it has to and the balance stays put
@Composable
private fun HeaderTitle(
  title: String,
  accountCount: Int?,
  failedCount: Int,
  style: TextStyle,
  color: Color,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = spacedBy(6.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f, fill = false).semantics { heading() },
      text = title,
      style = style,
      fontWeight = SemiBold,
      color = color,
      maxLines = 1,
      overflow = Ellipsis,
    )

    if (accountCount != null) {
      CountPill(accountCount)
    }
    SyncErrorRollup(failedCount)
  }
}

// packages/desktop-client/src/components/sidebar/redesign/CountPill.tsx
@Composable
private fun CountPill(count: Int, modifier: Modifier = Modifier) {
  Text(
    modifier =
      modifier
        .background(colors.sidebarControlBackground, CircleShape)
        .padding(horizontal = 6.dp, vertical = 1.dp),
    text = count.toString(),
    style = typography.labelSmall.copy(fontFeatureSettings = "tnum"),
    fontWeight = SemiBold,
    color = colors.sidebarTextMuted,
    maxLines = 1,
  )
}

// packages/desktop-client/src/components/sidebar/redesign/SyncErrorRollup.tsx
@Composable
private fun SyncErrorRollup(count: Int, modifier: Modifier = Modifier) {
  if (count == 0) return
  val description = Plurals.budgetNavAccountsFailed(count, count)

  Row(
    modifier =
      modifier
        .background(colors.sidebarBackgroundFailedSubtle, CircleShape)
        .padding(horizontal = 6.dp, vertical = 1.dp)
        .clearAndSetSemantics { contentDescription = description },
    horizontalArrangement = spacedBy(4.dp),
    verticalAlignment = CenterVertically,
  ) {
    Box(
      modifier = Modifier.size(5.dp).background(colors.sidebarItemBackgroundFailed, CircleShape),
    )
    Text(
      text = count.toString(),
      style = typography.labelSmall.copy(fontFeatureSettings = "tnum"),
      fontWeight = Bold,
      color = colors.sidebarTextFailed,
      maxLines = 1,
    )
  }
}

@Composable
private fun AccountItem(
  account: DrawerAccount,
  indent: Dp,
  isMuted: Boolean,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val color =
    when {
      selected -> colors.sidebarItemTextSelected
      isMuted -> colors.sidebarTextMuted
      else -> colors.sidebarItemText
    }

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .height(AccountItemHeight)
        .sidebarRow(colors, selected, onClick)
        .padding(end = 24.dp),
    verticalAlignment = CenterVertically,
  ) {
    Box(modifier = Modifier.width(indent), contentAlignment = CenterEnd) {
      AccountSyncDot(account.syncState, modifier = Modifier.padding(end = 12.dp))
    }

    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = spacedBy(8.dp),
      verticalAlignment = CenterVertically,
    ) {
      Text(
        modifier = Modifier.weight(1f, fill = false),
        text = account.name,
        style = typography.bodyMedium,
        color = color,
        maxLines = 1,
        overflow = Ellipsis,
      )

      NewTransactionsBadge(account.newTransactions)
    }

    BalanceText(
      amount = account.balance,
      style = typography.bodyMedium,
      color = color,
      modifier = Modifier.padding(start = 12.dp),
    )
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
  clip(SelectedShape)
    .background(if (selected) theme.sidebarItemBackgroundSelected else Transparent)
    .selectable(selected = selected, onClick = onClick)

@Composable
private fun AccountGroup.label(): String =
  when (this) {
    OnBudget -> Strings.budgetNavAccountsOnBudget
    OffBudget -> Strings.budgetNavAccountsOffBudget
    Closed -> Strings.budgetNavAccountsClosed
  }

private const val COLLAPSED_ROTATION = -90f

private val AccountItemHeight = 30.dp

private val SectionHeaderStart = 8.dp
// Fits inside DrawerItemHeight so the button stays square
private val SectionChevronSize = 32.dp
private val HeaderGap = 6.dp

// Section titles, account names and group titles share one left edge, with chevrons and sync dots
// hanging to its left. Grouped accounts sit one chevron further in
private val AccountNameIndent = SectionHeaderStart + SectionChevronSize + HeaderGap
private val GroupChevronSize = 16.dp
private val GroupHeaderIndent = AccountNameIndent - HeaderGap - GroupChevronSize
private val GroupedAccountNameIndent = AccountNameIndent + GroupChevronSize

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
  onAction: OnAction<BudgetMenuAction>,
): ImmutableList<DrawerAction> = buildList {
  if (LocalPrivacyEnabled.current) {
    +DrawerAction(
      icon = AktualIcons.ViewHide,
      label = Strings.budgetNavMenuPrivacyOff,
      onClick = { onSetPrivacyMode(false) },
    )
  } else {
    +DrawerAction(
      icon = AktualIcons.ViewShow,
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
    icon = AktualIcons.Cog,
    label = Strings.budgetNavMenuSettings,
    onClick = { onAction(Settings) },
  )
  +DrawerAction(
    icon = AktualIcons.InformationOutline,
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
      BasicIconButton(
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
        if (isPressed) theme.sidebarItemBackgroundSelected else theme.sidebarControlBackground,
      contentColor = if (isPressed) theme.sidebarItemTextSelected else theme.sidebarItemText,
      disabledContainerColor = theme.sidebarControlBackground.disabled,
      disabledContentColor = theme.sidebarItemText.disabled,
    )
}

@Composable
private fun DrawerHeader(
  state: DrawerHeaderState,
  onAction: OnAction<BudgetMenuAction>,
  modifier: Modifier = Modifier,
) {
  var showMenu by remember { mutableStateOf(false) }
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(SelectedShape)
        .background(colors.sidebarControlBackground)
        .padding(start = 16.dp, top = 8.dp, end = 4.dp, bottom = 8.dp),
    verticalAlignment = CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = state.budgetName.orEmpty(),
        style = typography.titleMedium,
        fontWeight = Bold,
        color = colors.sidebarHeaderText,
        maxLines = 1,
        overflow = Ellipsis,
      )
      SyncStatusLine(state.syncStatus)
      state.serverHost?.let { host ->
        Text(
          text = host,
          style = typography.bodySmall,
          color = colors.sidebarTextSubdued,
          maxLines = 1,
          overflow = Ellipsis,
        )
      }
    }

    Box {
      BasicIconButton(
        imageVector = AktualIcons.DotsHorizontalTriple,
        contentDescription = Strings.budgetNavMenuMore,
        colors = HeaderButtonColors,
        onClick = { showMenu = true },
      )

      AktualDropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        AktualDropdownMenuItem(
          text = Strings.budgetNavMenuRenameBudget,
          leadingIcon = AktualIcons.Pencil1,
          onClick = {
            showMenu = false
            onAction(RenameBudget)
          },
        )
        AktualDropdownMenuItem(
          text = Strings.budgetNavMenuExportBudget,
          leadingIcon = AktualIcons.Download,
          onClick = {
            showMenu = false
            onAction(ExportBudget)
          },
        )
        if (state.canUpload) {
          AktualDropdownMenuItem(
            text = Strings.budgetNavMenuUploadBudget,
            leadingIcon = AktualIcons.Upload,
            onClick = {
              showMenu = false
              onAction(UploadBudget)
            },
          )
        }
        if (state.canManageApiKeys) {
          AktualDropdownMenuItem(
            text = Strings.budgetNavMenuApiKeys,
            leadingIcon = AktualIcons.Code,
            onClick = {
              showMenu = false
              onAction(OpenApiKeys)
            },
          )
        }
      }
    }
  }
}

private val HeaderButtonColors = IconButtonColorProvider { theme, isPressed ->
  theme
    .bareIconButton(isPressed)
    .copy(
      containerColor = if (isPressed) theme.sidebarItemBackgroundSelected else Transparent,
      contentColor = if (isPressed) theme.sidebarItemTextSelected else theme.sidebarHeaderText,
    )
}

// packages/desktop-client/src/components/sidebar/redesign/SyncStatusLine.tsx
@Composable
private fun SyncStatusLine(status: SidebarSyncStatus, modifier: Modifier = Modifier) {
  val (icon, iconColor, label) =
    when (status) {
      Demo -> Triple(AktualIcons.CloudUnknown, colors.sidebarTextSubdued, Strings.budgetNavDemo)
      LocalOnly ->
        Triple(AktualIcons.CloudUnknown, colors.sidebarTextSubdued, Strings.budgetNavSyncLocal)
      Syncing ->
        Triple(AktualIcons.CloudCheck, colors.sidebarTextSubdued, Strings.budgetNavSyncSyncing)
      SyncError ->
        Triple(AktualIcons.CloudWarning, colors.sidebarTextFailed, Strings.budgetNavSyncError)
      Synced ->
        Triple(AktualIcons.CloudCheck, colors.sidebarTextPositive, Strings.budgetNavSyncSynced)
    }

  Row(
    modifier = modifier,
    horizontalArrangement = spacedBy(4.dp),
    verticalAlignment = CenterVertically,
  ) {
    Icon(
      modifier = Modifier.size(12.dp),
      imageVector = icon,
      contentDescription = null,
      tint = iconColor,
    )
    Text(
      text = label,
      style = typography.bodySmall,
      color = colors.sidebarTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )
  }
}

// Material's default is 56dp
private val DrawerItemHeight = 36.dp

// Aktual icons are 20dp and Material ones 24dp, so pad them to the same width to line up labels
private val DrawerIconSize = 24.dp

private val SelectedShape = ChipSize.Regular.shape

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
    icon = {
      Box(modifier = Modifier.size(DrawerIconSize), contentAlignment = Center) {
        Icon(icon, contentDescription = null)
      }
    },
    label = { Text(text = label, maxLines = 1, overflow = Ellipsis) },
    badge = amount?.let { { BalanceText(it) } },
    selected = selected,
    onClick = onClick,
    colors = colors.drawerItem(),
    shape = SelectedShape,
  )
}

@Composable
private fun Colors.drawerItem(): NavigationDrawerItemColors =
  NavigationDrawerItemDefaults.colors(
    selectedContainerColor = sidebarItemBackgroundSelected,
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
private fun PreviewBudgetDrawerSheet(@PreviewParameter(ColorParameters::class) colors: Colors) =
  PreviewWithColors(colors) {
    BudgetDrawerSheet {
      BudgetSidebarContent(
        headerState =
          DrawerHeaderState(
            budgetName = "My Budget",
            serverHost = "actual.example.com",
            canManageApiKeys = true,
          ),
        accounts =
          SidebarAccountsState(
            accounts = PREVIEW_ACCOUNTS,
            openSections = persistentMapOf("group-savings-on" to false),
          ),
        selectedTab = Transactions,
        transactionsRoot = AccountTransactionsNavRoute(AccountId("Joint Account")),
        hasNightlyUpdate = true,
        onSelectTab = {},
        onOpenTransactions = {},
        onSetPrivacyMode = {},
        onAccountsAction = {},
        onAction = {},
      )
    }
  }

@PortraitPreview
@Composable
private fun PreviewBudgetDrawerSheetSearching(
  @PreviewParameter(ColorParameters::class) colors: Colors,
) =
  PreviewWithColors(colors) {
    BudgetDrawerSheet {
      BudgetSidebarContent(
        headerState =
          DrawerHeaderState(
            budgetName = "My Budget",
            serverHost = "actual.example.com",
            syncStatus = SyncError,
          ),
        accounts = SidebarAccountsState(accounts = PREVIEW_ACCOUNTS, search = "acc"),
        selectedTab = Home,
        transactionsRoot = null,
        onSelectTab = {},
        onOpenTransactions = {},
        onSetPrivacyMode = {},
        onAccountsAction = {},
        onAction = {},
      )
    }
  }

private val PREVIEW_ACCOUNTS =
  DrawerAccounts(
    onBudget =
      section(
        bucket(
          group = null,
          account("Current Account", 2_431.18, newTransactions = 3),
          account("Credit Card", -642.37, Failed(ReauthRequired), newTransactions = 1),
        ),
        bucket(
          group = "Household",
          account("Joint Account", 1_204.50),
          account("Bills Account", 310.00, Failed(ReauthRequired)),
        ),
        bucket(
          group = "Savings",
          account("Easy Saver", 8_500.00, Ok(lastSync = null)),
          account("Holiday Fund", 1_250.00),
        ),
      ),
    offBudget =
      section(bucket(null, account("Stocks ISA", 14_220.91), account("Car Loan", -6_300.00))),
    closed = section(bucket(null, account("Old Savings", 0.0))),
  )

private fun section(vararg buckets: DrawerAccountBucket) =
  DrawerAccountSection(persistentListOf(*buckets))

private fun bucket(group: String?, vararg accounts: DrawerAccount) =
  DrawerAccountBucket(
    group = group?.let { DrawerAccountGroup(AccountGroupId(it.lowercase()), it) },
    accounts = persistentListOf(*accounts),
  )

private fun account(
  name: String,
  balance: Double,
  syncState: AccountSyncState = NotLinked,
  newTransactions: Int = 0,
) =
  DrawerAccount(
    id = AccountId(name),
    name = name,
    balance = Amount(balance),
    syncState = syncState,
    newTransactions = newTransactions,
  )
