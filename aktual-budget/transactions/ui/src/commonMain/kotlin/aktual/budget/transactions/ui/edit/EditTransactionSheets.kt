package aktual.budget.transactions.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.transactions.vm.edit.CategoryGroupOptions
import aktual.budget.transactions.vm.edit.EntityOption
import aktual.budget.transactions.vm.edit.TransactionEditMode
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.EditorSheet
import aktual.core.ui.ListBottomSheet
import aktual.core.ui.scrollbar
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate

// The picker of whichever field is being edited. The amount has the keypad instead
@Composable
internal fun EditTransactionSheet(
  field: EditField?,
  edit: TransactionEditMode.Edit,
  onAction: EditTransactionActionHandler,
  onDismiss: () -> Unit,
) {
  val draft = edit.draft
  when (field) {
    Payee ->
      PayeeSheet(
        selected = draft.payee,
        options = edit.options.payees,
        onDismiss = onDismiss,
        onSelect = { onAction(SetPayee(it)) },
      )

    Category ->
      CategorySheet(
        selected = draft.category,
        groups = edit.options.categoryGroups,
        onDismiss = onDismiss,
        onSelect = { onAction(SetCategory(it)) },
      )

    Account ->
      AccountSheet(
        selected = draft.account,
        options = edit.options.accounts,
        onDismiss = onDismiss,
        onSelect = { onAction(SetAccount(it)) },
      )

    Date ->
      DateDialog(date = draft.date, onDismiss = onDismiss, onSelect = { onAction(SetDate(it)) })

    Notes ->
      NotesSheet(notes = draft.notes, onDismiss = onDismiss, onSave = { onAction(SetNotes(it)) })

    Amount,
    null -> Unit
  }
}

@Immutable private data class PayeeChoice(val id: PayeeId?, val name: String)

// As upstream's payee picker, "None" is only offered when there's a payee to remove
@Composable
private fun PayeeSheet(
  selected: PayeeId?,
  options: ImmutableList<EntityOption<PayeeId>>,
  onDismiss: () -> Unit,
  onSelect: (PayeeId?) -> Unit,
) {
  val none = Strings.transactionNone
  val hasPayee = selected != null
  val choices =
    remember(options, hasPayee, none) {
      buildList {
        if (hasPayee) add(PayeeChoice(id = null, name = none))
        options.mapTo(this) { PayeeChoice(it.id, it.name) }
      }
        .toImmutableList()
    }
  ListBottomSheet(
    value = choices.firstOrNull { it.id == selected } ?: PayeeChoice(selected, name = ""),
    options = choices,
    onDismiss = onDismiss,
    onSelect = { onSelect(it.id) },
    sheetState = rememberBottomSheetState(Hidden),
    string = { it.name },
    key = { it.id?.toString().orEmpty() },
  )
}

@Composable
private fun AccountSheet(
  selected: AccountId?,
  options: ImmutableList<EntityOption<AccountId>>,
  onDismiss: () -> Unit,
  onSelect: (AccountId) -> Unit,
) =
  ListBottomSheet(
    value =
      options.firstOrNull { it.id == selected } ?: EntityOption(selected ?: AccountId(""), ""),
    options = options,
    onDismiss = onDismiss,
    onSelect = { onSelect(it.id) },
    sheetState = rememberBottomSheetState(Hidden),
    string = { it.name },
    key = { it.id.toString() },
  )

@Composable
private fun CategorySheet(
  selected: CategoryId?,
  groups: ImmutableList<CategoryGroupOptions>,
  onDismiss: () -> Unit,
  onSelect: (CategoryId?) -> Unit,
) =
  AktualModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberBottomSheetState(Hidden),
  ) {
    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
      item {
        BottomSheetListItem(
          label = Strings.transactionNone,
          isSelected = selected == null,
          onClick = {
            onSelect(null)
            onDismiss()
          },
        )
      }

      groups.fastForEach { group ->
        val name = group.name
        if (name != null) {
          item {
            Text(
              modifier = Modifier.fillMaxWidth().padding(GroupHeaderPadding),
              text = name,
              fontSize = GroupHeaderSize,
              fontWeight = SemiBold,
              color = colors.pageTextSubdued,
            )
          }
        }

        items(group.categories, key = { it.id.toString() }) { category ->
          BottomSheetListItem(
            label = category.name,
            isSelected = category.id == selected,
            onClick = {
              onSelect(category.id)
              onDismiss()
            },
          )
        }
      }
    }
  }

@Composable
private fun DateDialog(date: LocalDate, onDismiss: () -> Unit, onSelect: (LocalDate) -> Unit) {
  val state = rememberDatePickerState(date.toJavaLocalDate())
  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        enabled = state.selectedDateMillis != null,
        onClick = {
          state.getSelectedDate()?.toKotlinLocalDate()?.let(onSelect)
          onDismiss()
        },
        content = { Text(Strings.transactionSheetDone) },
      )
    },
    dismissButton = {
      TextButton(onClick = onDismiss, content = { Text(Strings.transactionSheetCancel) })
    },
    content = { DatePicker(state = state) },
  )
}

@Composable
private fun NotesSheet(notes: String?, onDismiss: () -> Unit, onSave: (String) -> Unit) {
  val text = rememberTextFieldState(initialText = notes.orEmpty())
  EditorSheet(
    title = Strings.transactionNotes,
    cancelText = Strings.transactionSheetCancel,
    confirmText = Strings.transactionSheetDone,
    canConfirm = true,
    onDismiss = onDismiss,
    onConfirm = { onSave(text.text.toString()) },
  ) {
    AktualTextField(
      modifier = Modifier.fillMaxWidth(),
      state = text,
      placeholderText = Strings.transactionNotesPlaceholder,
    )
  }
}

private val GroupHeaderPadding =
  PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
private val GroupHeaderSize = 13.sp
