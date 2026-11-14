package aktual.budget.transactions.ui

import aktual.budget.model.AccountId
import aktual.budget.transactions.vm.AccountChoice
import aktual.budget.transactions.vm.LoadedAccount
import aktual.budget.transactions.vm.ReconcileStart
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.CheckCircle1
import aktual.core.icons.Delete
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.Pencil1
import aktual.core.icons.SearchAlternate
import aktual.core.icons.Trash
import aktual.core.icons.Tuning
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.DialogTextButton
import aktual.core.ui.HazedTopBarState
import aktual.core.ui.LocalNavDrawerOpener
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.stringLong
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.YearMonth

@Composable
internal fun TransactionsTitleBar(
  hazeState: HazedTopBarState,
  listState: LazyListState,
  loadedAccount: LoadedAccount,
  isRoot: Boolean,
  onAction: OnAction<Action>,
  onOpenViewOptions: () -> Unit,
  reconcileStart: ReconcileStart? = null,
  accountChoices: ImmutableList<AccountChoice> = persistentListOf(),
  canSearch: Boolean = false,
  isSearching: Boolean = false,
  query: String = "",
  onSearch: () -> Unit = {},
  onCloseSearch: () -> Unit = {},
) {
  val title = loadedAccount.title()

  TopAppBar(
    modifier = Modifier.hazedTopBar(hazeState, listState),
    colors = colors.transparentTopAppBarColors(),
    navigationIcon = {
      if (isRoot && LocalNavDrawerOpener.current != null) {
        NavDrawerIconButton()
      } else {
        NavBackIconButton { onAction(NavBack) }
      }
    },
    title = {
      if (isSearching) {
        TransactionsSearchField(
          initialQuery = query,
          onQueryChange = { onAction(Action.SetQuery(it)) },
        )
      } else if (loadedAccount is SpecificAccount && accountChoices.size > 1) {
        AccountSwitcher(
          title = title,
          selected = loadedAccount.account.id,
          choices = accountChoices,
          onAction = onAction,
        )
      } else {
        Text(text = title, maxLines = 1, overflow = Ellipsis)
      }
    },
    actions = {
      if (isSearching) {
        AktualIconButton(
          imageVector = AktualIcons.Delete,
          contentDescription = Strings.transactionsSearchClose,
          onClick = onCloseSearch,
        )
      } else {
        TransactionsActions(
          loadedAccount = loadedAccount,
          canSearch = canSearch,
          reconcileStart = reconcileStart,
          onSearch = onSearch,
          onOpenViewOptions = onOpenViewOptions,
          onAction = onAction,
        )
      }
    },
  )
}

@Composable
private fun LoadedAccount.title(): String =
  when (this) {
    AllAccounts -> Strings.transactionsTitleAll
    Uncategorised -> Strings.transactionsUncategorisedTitle
    Loading -> Strings.transactionsTitleLoading
    is SpecificAccount -> account.name ?: Strings.transactionsTitleNone
    is Group ->
      when (group) {
        OnBudget -> Strings.transactionsTitleOnBudget
        OffBudget -> Strings.transactionsTitleOffBudget
        Closed -> Strings.transactionsTitleClosed
      }
    is SpecificTag -> "#$tag"
    is Filtered -> title
    is SpecificCategory ->
      month?.let {
        Strings.transactionsCategoryTitle(name, it.stringLong())
      } ?: name
  }

@Composable
private fun TransactionsActions(
  loadedAccount: LoadedAccount,
  canSearch: Boolean,
  reconcileStart: ReconcileStart?,
  onSearch: () -> Unit,
  onOpenViewOptions: () -> Unit,
  onAction: OnAction<Action>,
) {
  if (canSearch) {
    AktualIconButton(
      imageVector = AktualIcons.SearchAlternate,
      contentDescription = Strings.transactionsSearch,
      onClick = onSearch,
    )
  }

  AktualIconButton(
    imageVector = AktualIcons.Add,
    contentDescription = Strings.transactionsCreate,
    onClick = { onAction(CreateTransaction) },
  )

  AktualIconButton(
    imageVector = AktualIcons.Tuning,
    contentDescription = Strings.transactionsViewOptions,
    onClick = onOpenViewOptions,
  )

  if (loadedAccount is SpecificAccount) {
    TransactionsMenu(
      accountId = loadedAccount.account.id,
      reconcileStart = reconcileStart,
      onAction = onAction,
    )
  }
}

@Composable
private fun TransactionsSearchField(
  initialQuery: String,
  onQueryChange: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val state = rememberTextFieldState(initialText = initialQuery)
  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current
  val currentOnQueryChange by rememberUpdatedState(onQueryChange)

  // Opened from the search button, not reopened with a query already set
  LaunchedEffect(Unit) { if (initialQuery.isEmpty()) focusRequester.requestFocus() }

  LaunchedEffect(state) {
    snapshotFlow { state.text.toString() }.collect { currentOnQueryChange(it) }
  }

  AktualTextField(
    modifier = modifier.focusRequester(focusRequester).fillMaxWidth(),
    state = state,
    singleLine = true,
    placeholderText = Strings.transactionsSearchPlaceholder,
    showBorder = false,
    textStyle = typography.bodyLarge,
    keyboardOptions = KeyboardOptions(imeAction = Search),
    onKeyboardAction = { focusManager.clearFocus() },
  )
}

@Composable
private fun TransactionsMenu(
  accountId: AccountId,
  reconcileStart: ReconcileStart?,
  onAction: OnAction<Action>,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  var showReconcile by remember { mutableStateOf(false) }
  var confirmDelete by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    AktualIconButton(
      imageVector = AktualIcons.DotsHorizontalTriple,
      contentDescription = Strings.transactionsMenu,
      onClick = { expanded = true },
    )

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      if (reconcileStart != null) {
        AktualDropdownMenuItem(
          text = Strings.transactionsReconcile,
          leadingIcon = AktualIcons.CheckCircle1,
          onClick = {
            expanded = false
            showReconcile = true
          },
        )
      }
      AktualDropdownMenuItem(
        text = Strings.transactionsEditAccount,
        leadingIcon = AktualIcons.Pencil1,
        onClick = {
          expanded = false
          onAction(Action.EditAccount(accountId))
        },
      )
      AktualDropdownMenuItem(
        text = Strings.transactionsDeleteAccount,
        leadingIcon = AktualIcons.Trash,
        onClick = {
          expanded = false
          confirmDelete = true
        },
      )
    }
  }

  if (showReconcile && reconcileStart != null) {
    ReconcileDialog(
      start = reconcileStart,
      onConfirm = { target ->
        showReconcile = false
        onAction(Action.StartReconciling(target))
      },
      onDismiss = { showReconcile = false },
    )
  }

  if (confirmDelete) {
    ConfirmDeleteAccountDialog(
      onConfirm = {
        confirmDelete = false
        onAction(DeleteAccount)
      },
      onCancel = { confirmDelete = false },
    )
  }
}

@Composable
private fun ConfirmDeleteAccountDialog(onConfirm: () -> Unit, onCancel: () -> Unit) =
  AktualAlertDialog(
    title = Strings.transactionsDeleteAccountTitle,
    highlight = colors.errorText,
    onDismissRequest = onCancel,
    buttons = {
      DialogTextButton(text = Strings.transactionsDeleteAccountCancel, onClick = onCancel)
      DialogTextButton(
        kind = Destructive,
        text = Strings.transactionsDeleteAccountConfirm,
        onClick = onConfirm,
      )
    },
    content = { Text(Strings.transactionsDeleteAccountMessage) },
  )

@Preview
@Composable
private fun PreviewTransactionsTitleBar(
  @PreviewParameter(TransactionsTitleBarProvider::class) params: ColorParams<LoadedAccount>,
) =
  PreviewWithColorParams(params) {
    TransactionsTitleBar(
      hazeState = rememberHazedTopBarState(),
      listState = rememberLazyListState(),
      loadedAccount = this,
      isRoot = true,
      onAction = {},
      onOpenViewOptions = {},
      canSearch = true,
    )
  }

@Preview
@Composable
private fun PreviewTransactionsTitleBarSearching(
  @PreviewParameter(SearchQueryProvider::class) params: ColorParams<String>,
) =
  PreviewWithColorParams(params) {
    TransactionsTitleBar(
      hazeState = rememberHazedTopBarState(),
      listState = rememberLazyListState(),
      loadedAccount = LoadedAccount.SpecificAccount(PREVIEW_ACCOUNT),
      isRoot = true,
      onAction = {},
      onOpenViewOptions = {},
      canSearch = true,
      isSearching = true,
      query = this,
    )
  }

private class SearchQueryProvider : ColorParamsProvider<String>("groceries")

private class TransactionsTitleBarProvider :
  ColorParamsProvider<LoadedAccount>(
    LoadedAccount.AllAccounts,
    LoadedAccount.Uncategorised,
    LoadedAccount.Loading,
    LoadedAccount.SpecificAccount(PREVIEW_ACCOUNT),
    LoadedAccount.Group(OnBudget),
    LoadedAccount.SpecificTag("groceries"),
    LoadedAccount.SpecificCategory("Groceries", PREVIEW_MONTH),
    LoadedAccount.Filtered("Groceries, Mar 26"),
  )

private val PREVIEW_MONTH = YearMonth(2026, 10)
