package aktual.budget.budgeting.ui

import aktual.budget.budgeting.vm.AddKind
import aktual.budget.budgeting.vm.CategoryDialog
import aktual.budget.budgeting.vm.CategoryTarget
import aktual.budget.budgeting.vm.DialogStatus
import aktual.budget.budgeting.vm.GroupChoice
import aktual.budget.budgeting.vm.Transfer
import aktual.budget.budgeting.vm.TransferCategory
import aktual.budget.budgeting.vm.TransferGroup
import aktual.budget.model.CategoryGroupId
import aktual.budget.model.CategoryId
import aktual.core.l10n.Strings
import aktual.core.ui.AktualAlertDialog
import aktual.core.ui.AktualAlertDialogContent
import aktual.core.ui.AktualExposedDropDownMenu
import aktual.core.ui.AktualProgressIndicator
import aktual.core.ui.AktualSlidingToggleButton
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.DialogTextButton
import aktual.core.ui.OnAction
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.keyboardFocusRequester
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.drop

@Composable
internal fun CategoryEditDialog(dialog: CategoryDialog, onAction: OnAction<CategoryEditAction>) {
  AktualAlertDialog(onDismissRequest = { onAction(Dismiss) }) {
    CategoryEditDialogContent(dialog, onAction)
  }
}

@Composable
private fun CategoryEditDialogContent(
  dialog: CategoryDialog,
  onAction: OnAction<CategoryEditAction>,
) {
  when (dialog) {
    is Add -> AddDialog(dialog, onAction)
    is Rename -> RenameDialog(dialog, onAction)
    is Delete -> DeleteDialog(dialog, onAction)
  }
}

@Composable
private fun AddDialog(dialog: CategoryDialog.Add, onAction: OnAction<CategoryEditAction>) {
  var kind by rememberSaveable { mutableStateOf(AddKind.NewCategory) }
  var group by remember {
    mutableStateOf(
      dialog.groups.firstOrNull { !it.isIncome }?.id ?: dialog.groups.firstOrNull()?.id,
    )
  }
  val name = rememberTextFieldState()
  NotifyEdits(name, onAction)

  val isSaving = dialog.status == Saving
  val canCreate = name.text.isNotBlank() && !isSaving && (kind == NewGroup || group != null)
  val create: () -> Unit = {
    val text = name.text.toString()
    when (kind) {
      NewCategory -> group?.let { onAction(CategoryEditAction.CreateCategory(it, text)) }
      NewGroup -> onAction(CategoryEditAction.CreateGroup(text))
    }
  }

  AktualAlertDialogContent(
    title =
      when (kind) {
        NewCategory -> Strings.budgetingAddCategory
        NewGroup -> Strings.budgetingAddGroup
      },
    buttons = {
      DialogTextButton(
        text = Strings.budgetingEditCancel,
        onClick = { onAction(Dismiss) },
      )
      DialogTextButton(
        kind = Primary,
        text = Strings.budgetingAddCreate,
        isEnabled = canCreate,
        isLoading = isSaving,
        onClick = create,
      )
    },
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      AktualSlidingToggleButton(
        selected = kind,
        options = AddKind.entries.toImmutableList(),
        onSelect = { selected ->
          kind = selected
          onAction(Edited)
        },
        string = { option ->
          when (option) {
            NewCategory -> Strings.budgetingAddKindCategory
            NewGroup -> Strings.budgetingAddKindGroup
          }
        },
      )

      NameField(
        state = name,
        status = dialog.status,
        duplicateText =
          when (kind) {
            NewCategory -> Strings.budgetingNameDuplicateCategory
            NewGroup -> Strings.budgetingNameDuplicateGroup
          },
        onDone = { if (canCreate) create() },
      )

      if (kind == NewCategory) {
        GroupPicker(
          value = group,
          groups = dialog.groups,
          onValueChange = { picked ->
            group = picked
            onAction(Edited)
          },
        )
      }
    }
  }
}

@Composable
private fun GroupPicker(
  value: CategoryGroupId?,
  groups: ImmutableList<GroupChoice>,
  onValueChange: (CategoryGroupId) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(text = Strings.budgetingAddInGroup, style = typography.labelLarge)
    AktualExposedDropDownMenu(
      modifier = Modifier.fillMaxWidth(),
      value = value,
      onValueChange = { id -> id?.let(onValueChange) },
      options = groups.map { it.id }.toImmutableList(),
      string = { id -> groups.firstOrNull { it.id == id }?.name.orEmpty() },
    )
  }
}

@Composable
private fun RenameDialog(dialog: CategoryDialog.Rename, onAction: OnAction<CategoryEditAction>) {
  val initial = dialog.target.name
  val name =
    rememberTextFieldState(initialText = initial, initialSelection = TextRange(0, initial.length))
  NotifyEdits(name, onAction)

  val isSaving = dialog.status == Saving
  val canSave = name.text.isNotBlank() && !isSaving
  val save = { onAction(CategoryEditAction.Rename(name.text.toString())) }
  val isCategory = dialog.target is Category

  AktualAlertDialogContent(
    title = if (isCategory) Strings.budgetingCategoryRename else Strings.budgetingGroupRename,
    buttons = {
      DialogTextButton(
        text = Strings.budgetingEditCancel,
        onClick = { onAction(Dismiss) },
      )
      DialogTextButton(
        kind = Primary,
        text = Strings.budgetingNameSave,
        isEnabled = canSave,
        isLoading = isSaving,
        onClick = save,
      )
    },
  ) {
    NameField(
      state = name,
      status = dialog.status,
      duplicateText =
        if (isCategory) {
          Strings.budgetingNameDuplicateCategory
        } else {
          Strings.budgetingNameDuplicateGroup
        },
      onDone = { if (canSave) save() },
    )
  }
}

@Composable
private fun NameField(
  state: TextFieldState,
  status: DialogStatus,
  duplicateText: String,
  onDone: () -> Unit,
) {
  val error =
    when (status) {
      Duplicate -> duplicateText
      Failed -> Strings.budgetingCategoryWriteFailed
      Editing,
      Saving -> null
    }
  AktualTextField(
    modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
    state = state,
    placeholderText = Strings.budgetingName,
    singleLine = true,
    isEnabled = status != Saving,
    isError = error != null,
    supportingText = error?.let { text -> { Text(text = text, color = colors.errorText) } },
    onKeyboardAction = { onDone() },
  )
}

// Tells the view model about typing after the first value, so it can clear an error
@Composable
private fun NotifyEdits(state: TextFieldState, onAction: OnAction<CategoryEditAction>) {
  LaunchedEffect(state) {
    snapshotFlow { state.text.toString() }.drop(1).collect { onAction(Edited) }
  }
}

@Composable
private fun DeleteDialog(dialog: CategoryDialog.Delete, onAction: OnAction<CategoryEditAction>) {
  var transferTo by remember(dialog.target) { mutableStateOf<CategoryId?>(null) }
  val transfer = dialog.transfer
  val isSaving = dialog.status == Saving
  val canDelete =
    !isSaving &&
      when (transfer) {
        Checking -> false
        NotNeeded -> true
        is Needed -> transferTo != null
      }
  val isCategory = dialog.target is Category

  AktualAlertDialogContent(
    title = Strings.budgetingDeleteTitle(dialog.target.name),
    highlight = colors.errorText,
    buttons = {
      DialogTextButton(
        text = Strings.budgetingEditCancel,
        onClick = { onAction(Dismiss) },
      )
      DialogTextButton(
        kind = Destructive,
        text = Strings.budgetingDeleteConfirm,
        isEnabled = canDelete,
        isLoading = isSaving,
        onClick = { onAction(CategoryEditAction.Delete(transferTo)) },
      )
    },
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      when (transfer) {
        Checking -> AktualProgressIndicator()

        NotNeeded ->
          Text(
            if (isCategory) {
              Strings.budgetingDeleteCategoryUnused
            } else {
              Strings.budgetingDeleteGroupUnused
            },
          )

        is Needed -> {
          Text(
            if (isCategory) {
              Strings.budgetingDeleteCategoryTransfer
            } else {
              Strings.budgetingDeleteGroupTransfer
            },
          )
          TransferPicker(
            value = transferTo,
            groups = transfer.groups,
            onValueChange = { transferTo = it },
          )
        }
      }

      if (dialog.status == Failed) {
        Text(text = Strings.budgetingCategoryWriteFailed, color = colors.errorText)
      }
    }
  }
}

@Composable
private fun TransferPicker(
  value: CategoryId?,
  groups: ImmutableList<TransferGroup>,
  onValueChange: (CategoryId?) -> Unit,
) {
  val choose = Strings.budgetingDeleteTransferChoose
  val labels =
    groups
      .flatMap { group -> group.categories.map { it.id to "${it.name} (${group.name})" } }
      .toMap()
  AktualExposedDropDownMenu(
    modifier = Modifier.fillMaxWidth(),
    value = value,
    onValueChange = onValueChange,
    options = labels.keys.toImmutableList(),
    string = { id -> labels[id] ?: choose },
  )
}

private val PREVIEW_GROUP = CategoryGroupId("usual")
private const val PREVIEW_GROUP_NAME = "Usual expenses"

private val PREVIEW_GROUPS =
  persistentListOf(
    GroupChoice(PREVIEW_GROUP, PREVIEW_GROUP_NAME, isIncome = false),
    GroupChoice(CategoryGroupId("income"), "Income", isIncome = true),
  )

private class CategoryDialogProvider :
  ColorParamsProvider<CategoryDialog>(
    CategoryDialog.Add(PREVIEW_GROUPS),
    CategoryDialog.Add(PREVIEW_GROUPS, Duplicate),
    CategoryDialog.Rename(CategoryTarget.Group(PREVIEW_GROUP, PREVIEW_GROUP_NAME)),
    CategoryDialog.Delete(
      target = CategoryTarget.Category(CategoryId("food"), "Food"),
      transfer = NotNeeded,
    ),
    CategoryDialog.Delete(
      target = CategoryTarget.Group(PREVIEW_GROUP, PREVIEW_GROUP_NAME),
      transfer =
        Transfer.Needed(
          persistentListOf(
            TransferGroup(
              "Bills",
              persistentListOf(TransferCategory(CategoryId("rent"), "Rent")),
            ),
          ),
        ),
    ),
  )

@Preview
@Composable
private fun PreviewCategoryEditDialog(
  @PreviewParameter(CategoryDialogProvider::class) params: ColorParams<CategoryDialog>,
) = PreviewWithColorParams(params) { CategoryEditDialogContent(dialog = this, onAction = {}) }
