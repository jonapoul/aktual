package aktual.budget.transactions.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.transactions.domain.SplitPart
import aktual.budget.transactions.vm.edit.CategoryGroupOptions
import aktual.budget.transactions.vm.edit.EntityOption
import aktual.budget.transactions.vm.edit.TransactionForm
import aktual.core.icons.material.Add
import aktual.core.icons.material.MaterialIcons
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSheetIcon
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.EditorSheet
import aktual.core.ui.ListBottomSheet
import aktual.core.ui.keyboardFocusRequester
import aktual.core.ui.scrollbar
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
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
  active: ActiveField?,
  form: TransactionForm,
  onAction: EditTransactionActionHandler,
  onDismiss: () -> Unit,
) {
  val partId = active?.part
  if (partId != null) {
    val part = form.parts.firstOrNull { it.part.id == partId }?.part ?: return
    PartSheet(active.field, part, form, onAction, onDismiss)
    return
  }

  val draft = form.draft
  when (active?.field) {
    Payee ->
      PayeeSheet(
        selected = draft.payee,
        options = form.options.payees,
        transfers = form.options.transfers,
        onDismiss = onDismiss,
        onSelect = { onAction(SetPayee(it)) },
        onCreate = { onAction(CreatePayee(it)) },
      )

    Category ->
      CategorySheet(
        selected = draft.category,
        groups = form.options.categoryGroups,
        onDismiss = onDismiss,
        onSelect = { onAction(SetCategory(it)) },
      )

    Account ->
      AccountSheet(
        selected = draft.account,
        options = form.options.accounts,
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

// A split's part has its parent's account and date
@Composable
private fun PartSheet(
  field: EditField,
  part: SplitPart,
  form: TransactionForm,
  onAction: EditTransactionActionHandler,
  onDismiss: () -> Unit,
) =
  when (field) {
    Payee ->
      PayeeSheet(
        selected = part.payee,
        options = form.options.payees,
        transfers = form.options.transfers,
        onDismiss = onDismiss,
        onSelect = { onAction(SetPartPayee(part.id, it)) },
        onCreate = { onAction(CreatePartPayee(part.id, it)) },
      )

    Category ->
      CategorySheet(
        selected = part.category,
        groups = form.options.categoryGroups,
        onDismiss = onDismiss,
        onSelect = { onAction(SetPartCategory(part.id, it)) },
      )

    Notes ->
      NotesSheet(
        notes = part.notes,
        onDismiss = onDismiss,
        onSave = { onAction(SetPartNotes(part.id, it)) },
      )

    Amount,
    Account,
    Date -> Unit
  }

// As upstream's payee picker: "None" is only offered when there's a payee to remove, typing a
// name that no payee has offers to create it, and the accounts to transfer to come last
@Composable
private fun PayeeSheet(
  selected: PayeeId?,
  options: ImmutableList<EntityOption<PayeeId>>,
  transfers: ImmutableList<EntityOption<PayeeId>>,
  onDismiss: () -> Unit,
  onSelect: (PayeeId?) -> Unit,
  onCreate: (String) -> Unit,
) =
  AktualModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberBottomSheetState(Hidden),
  ) {
    val query = rememberTextFieldState()
    val name = query.text.toString().trim()
    val matches =
      remember(options, name) {
        options.filter { it.name.contains(name, ignoreCase = true) }.toImmutableList()
      }
    val transferMatches =
      remember(transfers, name) {
        transfers.filter { it.name.contains(name, ignoreCase = true) }.toImmutableList()
      }
    val isNew =
      name.isNotEmpty() &&
        matches.none { it.name.equals(name, ignoreCase = true) } &&
        transferMatches.none { it.name.equals(name, ignoreCase = true) }

    AktualTextField(
      modifier = Modifier.fillMaxWidth().padding(PayeeSearchPadding),
      state = query,
      placeholderText = Strings.transactionPayeeSearch,
      singleLine = true,
      clearable = true,
      keyboardOptions = KeyboardOptions(capitalization = Words),
    )

    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
      if (isNew) {
        item {
          BottomSheetListItem(
            label = Strings.transactionPayeeCreate(name),
            isSelected = false,
            leadingContent = { BottomSheetIcon(MaterialIcons.Add) },
            onClick = {
              onCreate(name)
              onDismiss()
            },
          )
        }
      }

      if (selected != null && name.isEmpty()) {
        item {
          BottomSheetListItem(
            label = Strings.transactionNone,
            isSelected = false,
            onClick = {
              onSelect(null)
              onDismiss()
            },
          )
        }
      }

      items(matches, key = { it.id.toString() }) { payee ->
        BottomSheetListItem(
          label = payee.name,
          isSelected = payee.id == selected,
          onClick = {
            onSelect(payee.id)
            onDismiss()
          },
        )
      }

      if (transferMatches.isNotEmpty()) {
        item { GroupHeader(Strings.transactionsTransfer) }
      }

      items(transferMatches, key = { it.id.toString() }) { payee ->
        BottomSheetListItem(
          label = payee.name,
          isSelected = payee.id == selected,
          onClick = {
            onSelect(payee.id)
            onDismiss()
          },
        )
      }
    }
  }

@Composable
private fun GroupHeader(name: String) =
  Text(
    modifier = Modifier.fillMaxWidth().padding(GroupHeaderPadding),
    text = name,
    fontSize = GroupHeaderSize,
    fontWeight = SemiBold,
    color = colors.pageTextSubdued,
  )

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
          item { GroupHeader(name) }
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
      modifier = Modifier.fillMaxWidth().focusRequester(keyboardFocusRequester()),
      state = text,
      placeholderText = Strings.transactionNotesPlaceholder,
      keyboardOptions = KeyboardOptions(capitalization = Sentences),
    )
  }
}

private val GroupHeaderPadding =
  PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
private val GroupHeaderSize = 13.sp
private val PayeeSearchPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
