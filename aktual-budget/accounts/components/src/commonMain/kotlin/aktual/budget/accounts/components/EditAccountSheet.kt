package aktual.budget.accounts.components

import aktual.budget.accounts.vm.edit.EditAccountState
import aktual.budget.accounts.vm.edit.EditAccountViewModel
import aktual.budget.accounts.vm.edit.Editing
import aktual.budget.accounts.vm.edit.Failure
import aktual.budget.accounts.vm.edit.Loading
import aktual.budget.accounts.vm.manage.AccountGroupItem
import aktual.budget.model.AccountGroupId
import aktual.budget.model.AccountId
import aktual.budget.model.Amount.Companion.Zero
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTextField
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.HostedSheet
import aktual.core.ui.LocalSnackbars
import aktual.core.ui.NormalTextButton
import aktual.core.ui.PortraitPreview
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.PrimaryTextButtonWithLoading
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import kotlinx.collections.immutable.persistentListOf

// Shows its own snackbar once saved, with an undo
@Composable
fun EditAccountSheet(id: AccountId, onDismiss: () -> Unit) {
  val viewModel =
    assistedMetroViewModel<EditAccountViewModel, EditAccountViewModel.Factory>(
      viewModelStoreOwner = rememberSheetViewModelStoreOwner(),
    ) {
      create(id)
    }
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbars = LocalSnackbars.current

  HostedSheet(onDismiss = onDismiss) {
    val sheet = this
    LaunchedEffect(viewModel) {
      viewModel.events.collect { event ->
        when (event) {
          is Saved -> sheet.dismiss { snackbars.show { showSaved(event, viewModel::undo) } }
        }
      }
    }

    EditAccountContent(
      state = state,
      onName = viewModel::setName,
      onNotes = viewModel::setNotes,
      onGroup = viewModel::setGroup,
      onCancel = { sheet.close() },
      onSave = viewModel::save,
    )
  }
}

@Composable
private fun EditAccountContent(
  state: EditAccountState,
  onName: (String) -> Unit,
  onNotes: (String) -> Unit,
  onGroup: (AccountGroupId?) -> Unit,
  onCancel: () -> Unit,
  onSave: () -> Unit,
) {
  val editing = state as? Editing
  SheetPage(
    title = Strings.accountsEditTitle,
    buttons = {
      NormalTextButton(
        modifier = Modifier.weight(1f),
        text = Strings.accountsManageCancel,
        onClick = onCancel,
      )
      PrimaryTextButtonWithLoading(
        modifier = Modifier.weight(1f),
        text = Strings.accountsEditSave,
        isLoading = editing?.isSaving == true,
        isEnabled = editing?.canSave == true,
        onClick = onSave,
      )
    },
  ) {
    when (state) {
      Loading -> SheetLoading()
      is Failure -> ErrorText("${Strings.accountsEditFailed}: ${state.message}")
      is Editing -> EditAccountFields(state, onName, onNotes, onGroup)
    }
  }
}

@Composable
private fun EditAccountFields(
  state: Editing,
  onName: (String) -> Unit,
  onNotes: (String) -> Unit,
  onGroup: (AccountGroupId?) -> Unit,
) {
  // Seeded once, the view model holds the edits from then on
  val nameState = rememberTextFieldState(state.name)
  val notesState = rememberTextFieldState(state.notes)
  LaunchedEffect(Unit) { snapshotFlow { nameState.text.toString() }.collect(onName) }
  LaunchedEffect(Unit) { snapshotFlow { notesState.text.toString() }.collect(onNotes) }

  Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Column {
      FieldLabel(Strings.accountsEditName)
      AktualTextField(
        modifier = Modifier.fillMaxWidth(),
        state = nameState,
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = Sentences),
        placeholderText = null,
        isError = state.duplicateName != null,
        supportingText = state.duplicateName?.let { { Text(Strings.accountsEditNameDuplicate) } },
      )
    }

    Column {
      FieldLabel(Strings.accountsEditGroup)
      AccountGroupPicker(value = state.group, groups = state.groups, onValueChange = onGroup)
    }

    Column {
      FieldLabel(Strings.accountsEditNotes)
      AktualTextField(
        modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
        state = notesState,
        keyboardOptions = KeyboardOptions(capitalization = Sentences),
        placeholderText = null,
      )
    }

    state.saveError?.let { error -> ErrorText("${Strings.accountsEditSaveFailed}: $error") }
  }
}

@PortraitPreview
@Composable
private fun PreviewEditAccount(
  @PreviewParameter(EditAccountStateProvider::class) params: ColorParams<EditAccountState>,
) =
  PreviewWithColorParams(params) {
    EditAccountContent(
      state = this,
      onName = {},
      onNotes = {},
      onGroup = {},
      onCancel = {},
      onSave = {},
    )
  }

private val everyday = AccountGroupItem(AccountGroupId("everyday"), "Everyday")
private val savings = AccountGroupItem(AccountGroupId("savings"), "Savings")

private class EditAccountStateProvider :
  ColorParamsProvider<EditAccountState>(
    Editing(
      name = "Current account",
      notes = "Joint with Sam",
      group = everyday.id,
      groups = persistentListOf(everyday, savings),
      offBudget = false,
      balance = Zero,
      transactionCount = 412,
      duplicateName = null,
      canSave = true,
      isSaving = false,
      saveError = null,
    ),
    Editing(
      name = "Credit card",
      notes = "",
      group = null,
      groups = persistentListOf(everyday),
      offBudget = false,
      balance = Zero,
      transactionCount = 0,
      duplicateName = "Credit card",
      canSave = false,
      isSaving = false,
      saveError = "Database is locked",
    ),
    Loading,
  )
