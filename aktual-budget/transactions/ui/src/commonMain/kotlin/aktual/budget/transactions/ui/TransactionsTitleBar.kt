package aktual.budget.transactions.ui

import aktual.budget.transactions.vm.LoadedAccount
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.DotsHorizontalTriple
import aktual.core.icons.Pencil1
import aktual.core.icons.Trash
import aktual.core.icons.Tuning
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualDropdownMenu
import aktual.core.ui.AktualDropdownMenuItem
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.HazedTopBarState
import aktual.core.ui.LocalNavDrawerOpener
import aktual.core.ui.NavBackIconButton
import aktual.core.ui.NavDrawerIconButton
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.hazedTopBar
import aktual.core.ui.rememberHazedTopBarState
import aktual.core.ui.stringLong
import aktual.core.ui.transparentTopAppBarColors
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.YearMonth

@Composable
internal fun TransactionsTitleBar(
  hazeState: HazedTopBarState,
  listState: LazyListState,
  loadedAccount: LoadedAccount,
  isRoot: Boolean,
  onAction: ActionListener,
  onOpenViewOptions: () -> Unit,
  otherAccountNames: ImmutableSet<String> = persistentSetOf(),
) {
  val title =
    when (loadedAccount) {
      AllAccounts -> Strings.transactionsTitleAll
      Uncategorised -> Strings.transactionsUncategorisedTitle
      Loading -> Strings.transactionsTitleLoading
      is SpecificAccount -> loadedAccount.account.name ?: Strings.transactionsTitleNone
      is Group ->
        when (loadedAccount.group) {
          OnBudget -> Strings.transactionsTitleOnBudget
          OffBudget -> Strings.transactionsTitleOffBudget
          Closed -> Strings.transactionsTitleClosed
        }
      is SpecificTag -> "#${loadedAccount.tag}"
      is Filtered -> loadedAccount.title
      is SpecificCategory ->
        loadedAccount.month?.let {
          Strings.transactionsCategoryTitle(loadedAccount.name, it.stringLong())
        } ?: loadedAccount.name
    }

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
    title = { Text(text = title, maxLines = 1, overflow = Ellipsis) },
    actions = {
      IconButton(
        onClick = { onAction(CreateTransaction) },
        content = { Icon(AktualIcons.Add, Strings.transactionsCreate) },
      )

      IconButton(
        onClick = onOpenViewOptions,
        content = { Icon(AktualIcons.Tuning, Strings.transactionsViewOptions) },
      )

      if (loadedAccount is SpecificAccount) {
        TransactionsMenu(
          accountName = loadedAccount.account.name.orEmpty(),
          otherAccountNames = otherAccountNames,
          onAction = onAction,
        )
      }
    },
  )
}

@Composable
private fun TransactionsMenu(
  accountName: String,
  otherAccountNames: ImmutableSet<String>,
  onAction: ActionListener,
  modifier: Modifier = Modifier,
) {
  var expanded by remember { mutableStateOf(false) }
  var showRename by remember { mutableStateOf(false) }
  var confirmDelete by remember { mutableStateOf(false) }

  Box(modifier = modifier) {
    IconButton(
      onClick = { expanded = true },
      content = { Icon(AktualIcons.DotsHorizontalTriple, Strings.transactionsMenu) },
    )

    AktualDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
      AktualDropdownMenuItem(
        text = Strings.transactionsRenameAccount,
        leadingIcon = AktualIcons.Pencil1,
        onClick = {
          expanded = false
          showRename = true
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

  if (showRename) {
    RenameAccountDialog(
      currentName = accountName,
      otherNames = otherAccountNames,
      onConfirm = { name ->
        showRename = false
        onAction(Action.RenameAccount(name))
      },
      onDismiss = { showRename = false },
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
      TextButton(onClick = onCancel) { Text(Strings.transactionsDeleteAccountCancel) }
      TextButton(onClick = onConfirm) {
        Text(Strings.transactionsDeleteAccountConfirm, color = colors.errorText)
      }
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
    )
  }

private class TransactionsTitleBarProvider :
  ColorParamsProvider<LoadedAccount>(
    LoadedAccount.AllAccounts,
    LoadedAccount.Uncategorised,
    LoadedAccount.Loading,
    LoadedAccount.SpecificAccount(PREVIEW_ACCOUNT),
    LoadedAccount.Group(OnBudget),
    LoadedAccount.SpecificTag("groceries"),
    LoadedAccount.SpecificCategory("Groceries", PREVIEW_MONTH),
    LoadedAccount.Filtered("Groceries · Mar 26"),
  )

private val PREVIEW_MONTH = YearMonth(2026, 10)
