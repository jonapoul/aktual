package aktual.budget.accounts.ui.manage

import aktual.budget.accounts.components.ErrorText
import aktual.budget.accounts.components.FieldLabel
import aktual.budget.accounts.components.SheetLoading
import aktual.budget.accounts.components.SheetPage
import aktual.budget.accounts.vm.close.CategoryChoice
import aktual.budget.accounts.vm.close.CategoryChoiceGroup
import aktual.budget.accounts.vm.close.CloseAccountEvent
import aktual.budget.accounts.vm.close.CloseAccountState
import aktual.budget.accounts.vm.close.Failure
import aktual.budget.accounts.vm.close.Loading
import aktual.budget.accounts.vm.close.Ready
import aktual.budget.accounts.vm.close.TransferTarget
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.core.icons.AktualIcons
import aktual.core.icons.AlertTriangle
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareTextButton
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.HostedSheet
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PrimaryTextButtonWithLoading
import aktual.core.ui.formattedString
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun CloseAccountSheet(
  id: AccountId,
  onDismiss: () -> Unit,
  onDeleteInstead: () -> Unit,
  onClose: (CloseAccountEvent.Closed) -> Unit,
) {
  val viewModel = closeAccountViewModel(id)
  val state by viewModel.state.collectAsStateWithLifecycle()
  val latestOnClose by rememberUpdatedState(onClose)

  HostedSheet(onDismiss = onDismiss) {
    val sheet = this
    LaunchedEffect(viewModel) {
      viewModel.events.collect { event ->
        if (event is Closed) sheet.dismiss { latestOnClose(event) }
      }
    }

    CloseAccountContent(
      state = state,
      onTransferTo = viewModel::setTransferTo,
      onCategory = viewModel::setCategory,
      onCancel = { sheet.close() },
      onClose = viewModel::close,
      onDeleteInstead = { sheet.close(onDeleteInstead) },
    )
  }
}

@Composable
private fun CloseAccountContent(
  state: CloseAccountState,
  onTransferTo: (AccountId?) -> Unit,
  onCategory: (CategoryId?) -> Unit,
  onCancel: () -> Unit,
  onClose: () -> Unit,
  onDeleteInstead: () -> Unit,
) {
  val ready = state as? Ready
  SheetPage(
    title = Strings.accountsCloseTitle(ready?.name.orEmpty()),
    buttons = {
      NormalTextButton(
        modifier = Modifier.weight(1f),
        text = Strings.accountsManageCancel,
        onClick = onCancel,
      )
      PrimaryTextButtonWithLoading(
        modifier = Modifier.weight(1f),
        text =
          if (ready?.case == NoTransactions) {
            Strings.accountsCloseDeleteEmpty
          } else {
            Strings.accountsCloseConfirm
          },
        isLoading = ready?.isWorking == true,
        isEnabled = ready?.canClose == true,
        onClick = onClose,
      )
    },
  ) {
    when (state) {
      Loading -> SheetLoading()
      is Failure -> ErrorText("${Strings.accountsCloseFailed}: ${state.message}")
      is Ready -> CloseAccountFields(state, onTransferTo, onCategory, onDeleteInstead)
    }
  }
}

@Composable
private fun CloseAccountFields(
  state: Ready,
  onTransferTo: (AccountId?) -> Unit,
  onCategory: (CategoryId?) -> Unit,
  onDeleteInstead: () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    when (state.case) {
      HasBalance -> {
        Message(Strings.accountsCloseBalance(state.balance.formattedString()))
        Column {
          FieldLabel(Strings.accountsCloseTransferTo)
          TransferPicker(state.transferTo, state.targets, onTransferTo)
        }
        if (state.needsCategory) {
          Column {
            FieldLabel(Strings.accountsCloseCategory)
            CategoryPicker(state.category, state.categories, onCategory)
            Text(
              text = Strings.accountsCloseCategoryHint,
              style = typography.bodySmall,
              color = colors.pageTextSubdued,
            )
          }
        }
      }

      ZeroBalance -> Message(Strings.accountsCloseZero)
      NoTransactions -> Message(Strings.accountsCloseEmpty)
    }

    if (state.isLinked) LinkedWarning()
    state.error?.let { error -> ErrorText("${Strings.accountsCloseWorkFailed}: $error") }

    if (state.case != NoTransactions) {
      BareTextButton(
        modifier = Modifier.fillMaxWidth(),
        text = Strings.accountsCloseDeleteInstead,
        isEnabled = !state.isWorking,
        onClick = onDeleteInstead,
      )
    }
  }
}

@Composable private fun Message(text: String) = Text(text = text, style = typography.bodyMedium)

@Composable
private fun LinkedWarning() {
  Row(
    verticalAlignment = CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Icon(
      modifier = Modifier.size(18.dp),
      imageVector = AktualIcons.AlertTriangle,
      contentDescription = null,
      tint = colors.warningText,
    )
    Text(
      text = Strings.accountsCloseLinked,
      style = typography.bodyMedium,
      color = colors.warningText,
    )
  }
}

@Composable
private fun TransferPicker(
  value: AccountId?,
  targets: ImmutableList<TransferTarget>,
  onValueChange: (AccountId?) -> Unit,
) {
  val choose = Strings.accountsCloseChooseAccount
  AktualExposedDropDownMenu(
    modifier = Modifier.fillMaxWidth(),
    value = value,
    onValueChange = onValueChange,
    options = targets.map { it.id }.toImmutableList(),
    string = { id -> targets.firstOrNull { it.id == id }?.name ?: choose },
  )
}

@Composable
private fun CategoryPicker(
  value: CategoryId?,
  groups: ImmutableList<CategoryChoiceGroup>,
  onValueChange: (CategoryId?) -> Unit,
) {
  val choose = Strings.accountsCloseChooseCategory
  val labels =
    groups
      .flatMap { group -> group.categories.map { it.id to "${group.name} · ${it.name}" } }
      .toMap()
  AktualExposedDropDownMenu(
    modifier = Modifier.fillMaxWidth(),
    value = value,
    onValueChange = onValueChange,
    options = labels.keys.toImmutableList(),
    string = { id -> labels[id] ?: choose },
  )
}

@Composable
internal fun DeleteAccountDialog(
  id: AccountId,
  onDismiss: () -> Unit,
  onDelete: (CloseAccountEvent.Deleted) -> Unit,
) {
  val viewModel = closeAccountViewModel(id)
  val state by viewModel.state.collectAsStateWithLifecycle()
  val latestOnDelete by rememberUpdatedState(onDelete)
  val latestOnDismiss by rememberUpdatedState(onDismiss)

  LaunchedEffect(viewModel) {
    viewModel.events.collect { event ->
      if (event is Deleted) {
        latestOnDelete(event)
        latestOnDismiss()
      }
    }
  }

  val ready = state as? Ready
  AktualAlertDialog(
    title = Strings.accountsDeleteTitle(ready?.name.orEmpty()),
    highlight = colors.errorText,
    onDismissRequest = onDismiss,
    buttons = {
      NormalTextButton(text = Strings.accountsManageCancel, onClick = onDismiss)
      PrimaryTextButtonWithLoading(
        text = Strings.accountsDeleteConfirm,
        isLoading = ready?.isWorking == true,
        isEnabled = ready != null && !ready.isWorking,
        onClick = viewModel::delete,
      )
    },
  ) {
    when (val current = state) {
      Loading -> SheetLoading()
      is Failure -> ErrorText("${Strings.accountsCloseFailed}: ${current.message}")
      is Ready -> DeleteAccountMessage(current)
    }
  }
}

@Composable
private fun DeleteAccountMessage(state: Ready) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    val count = state.transactionCount.toInt()
    Text(Plurals.accountsDeleteMessage(count, count))
    if (state.transferCount > 0) {
      Text("• ${Plurals.accountsDeleteTransfers(state.transferCount, state.transferCount)}")
    }
    if (state.isLinked) Text("• ${Strings.accountsDeleteUnlink}")
    Text(text = Strings.accountsDeleteKeepHint, color = colors.pageTextSubdued)
    state.error?.let { error -> ErrorText(error) }
  }
}

@Preview
@Composable
private fun PreviewCloseAccount(
  @PreviewParameter(CloseAccountStateProvider::class) params: ColorParams<CloseAccountState>,
) =
  PreviewWithColorParams(params) {
    Column {
      CloseAccountContent(
        state = this@PreviewWithColorParams,
        onTransferTo = {},
        onCategory = {},
        onCancel = {},
        onClose = {},
        onDeleteInstead = {},
      )
    }
  }

private val previewReady =
  Ready(
    name = "Credit card",
    balance = Amount(-245.10),
    transactionCount = 88,
    transferCount = 3,
    isLinked = true,
    case = HasBalance,
    targets =
      persistentListOf(
        TransferTarget(AccountId("a"), "Current account", offBudget = false),
        TransferTarget(AccountId("b"), "Pension", offBudget = true),
      ),
    transferTo = AccountId("b"),
    needsCategory = true,
    categories =
      persistentListOf(
        CategoryChoiceGroup(
          "Bills",
          persistentListOf(CategoryChoice(CategoryId("c"), "Rent")),
        ),
      ),
    category = null,
    categoryName = null,
    canClose = false,
    isWorking = false,
    error = null,
  )

private class CloseAccountStateProvider :
  ColorParamsProvider<CloseAccountState>(
    previewReady,
    previewReady.copy(case = ZeroBalance, isLinked = false, canClose = true),
    previewReady.copy(case = NoTransactions, isLinked = false, canClose = true),
    Loading,
  )
