package aktual.budget.transactions.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.transactions.domain.MIN_TRANSACTION_DATE
import aktual.budget.transactions.domain.SplitPart
import aktual.budget.transactions.vm.edit.EntityOption
import aktual.budget.transactions.vm.edit.TransactionForm
import aktual.budget.transactions.vm.edit.TransactionOptions
import aktual.budget.transactions.vm.edit.matching
import aktual.core.icons.material.Add
import aktual.core.icons.material.MaterialIcons
import aktual.core.icons.material.Visibility
import aktual.core.icons.material.VisibilityOff
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.BottomSheetIcon
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.ListBottomSheet
import aktual.core.ui.NormalIconButton
import aktual.core.ui.formattedText
import aktual.core.ui.scrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
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
        suggested = form.options.suggestedPayees,
        transfers = form.options.transfers,
        onDismiss = onDismiss,
        onSelect = { onAction(SetPayee(it)) },
        onCreate = { onAction(CreatePayee(it)) },
      )

    Category ->
      CategorySheet(
        selected = draft.category,
        options = form.options,
        onDismiss = onDismiss,
        onSelect = { onAction(SetCategory(it)) },
        onToggleHiding = { onAction(ToggleHiddenCategories) },
        onPicking = { onAction(PickingCategory(it)) },
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
      NotesSheet(
        notes = draft.notes,
        tags = form.options.tags,
        onDismiss = onDismiss,
        onSave = { onAction(SetNotes(it)) },
        onCreateTag = { onAction(CreateTag(it)) },
      )

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
        suggested = form.options.suggestedPayees,
        transfers = form.options.transfers,
        onDismiss = onDismiss,
        onSelect = { onAction(SetPartPayee(part.id, it)) },
        onCreate = { onAction(CreatePartPayee(part.id, it)) },
      )

    Category ->
      CategorySheet(
        selected = part.category,
        options = form.options,
        onDismiss = onDismiss,
        onSelect = { onAction(SetPartCategory(part.id, it)) },
        onToggleHiding = { onAction(ToggleHiddenCategories) },
        onPicking = { onAction(PickingCategory(it)) },
      )

    Notes ->
      NotesSheet(
        notes = part.notes,
        tags = form.options.tags,
        onDismiss = onDismiss,
        onSave = { onAction(SetPartNotes(part.id, it)) },
        onCreateTag = { onAction(CreateTag(it)) },
      )

    Amount,
    Account,
    Date -> Unit
  }

// As upstream's payee picker: "None" is only offered when there's a payee to remove, typing a
// name that no payee has offers to create it, the suggested payees come first and the accounts to
// transfer to come last
@Composable
private fun PayeeSheet(
  selected: PayeeId?,
  options: ImmutableList<EntityOption<PayeeId>>,
  suggested: ImmutableList<EntityOption<PayeeId>>,
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
    val suggestedMatches =
      remember(suggested, name) {
        suggested.filter { it.name.contains(name, ignoreCase = true) }.toImmutableList()
      }
    val transferMatches =
      remember(transfers, name) {
        transfers.filter { it.name.contains(name, ignoreCase = true) }.toImmutableList()
      }
    val isNew =
      name.isNotEmpty() &&
        matches.none { it.name.equals(name, ignoreCase = true) } &&
        suggestedMatches.none { it.name.equals(name, ignoreCase = true) } &&
        transferMatches.none { it.name.equals(name, ignoreCase = true) }

    AktualTextField(
      modifier = Modifier.fillMaxWidth().padding(SearchPadding),
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

      // The rest only need a header to tell them from the suggestions
      if (suggestedMatches.isNotEmpty()) {
        item { GroupHeader(Strings.transactionPayeeSuggested) }
        payeeItems(suggestedMatches, selected, onSelect, onDismiss)
        if (matches.isNotEmpty()) {
          item { GroupHeader(Strings.transactionPayeeAll) }
        }
      }

      payeeItems(matches, selected, onSelect, onDismiss)

      if (transferMatches.isNotEmpty()) {
        item { GroupHeader(Strings.transactionsTransfer) }
      }

      payeeItems(transferMatches, selected, onSelect, onDismiss)
    }
  }

private fun LazyListScope.payeeItems(
  payees: ImmutableList<EntityOption<PayeeId>>,
  selected: PayeeId?,
  onSelect: (PayeeId?) -> Unit,
  onDismiss: () -> Unit,
) =
  items(payees, key = { it.id.toString() }) { payee ->
    BottomSheetListItem(
      label = payee.name,
      isSelected = payee.id == selected,
      onClick = {
        onSelect(payee.id)
        onDismiss()
      },
    )
  }

@Composable
private fun GroupHeader(name: String, modifier: Modifier = Modifier) =
  Text(
    modifier = modifier.fillMaxWidth().padding(GroupHeaderPadding),
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
    searchPlaceholder = Strings.searchAccounts,
  )

// As upstream's category picker, typing filters the categories by their name or their group's.
// Groups with no match are left out, and "None" is only offered until something's typed. Each
// category shows its balance in the transaction's month
@Composable
private fun CategorySheet(
  selected: CategoryId?,
  options: TransactionOptions,
  onDismiss: () -> Unit,
  onSelect: (CategoryId?) -> Unit,
  onToggleHiding: () -> Unit,
  onPicking: (Boolean) -> Unit,
) =
  AktualModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberBottomSheetState(Hidden),
  ) {
    val currentOnPicking by rememberUpdatedState(onPicking)
    DisposableEffect(Unit) {
      currentOnPicking(true)
      onDispose { currentOnPicking(false) }
    }

    val query = rememberTextFieldState()
    val name = query.text.toString().trim()
    val groups = options.categoryGroups
    val matches = remember(groups, name) { groups.matching(name) }

    Row(
      modifier = Modifier.fillMaxWidth().padding(SearchPadding),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = CenterVertically,
    ) {
      AktualTextField(
        modifier = Modifier.weight(1f),
        state = query,
        placeholderText = Strings.transactionCategorySearch,
        singleLine = true,
        clearable = true,
      )

      if (options.hasHiddenCategories) {
        val showHidden = options.showHiddenCategories
        NormalIconButton(
          imageVector = if (showHidden) MaterialIcons.VisibilityOff else MaterialIcons.Visibility,
          contentDescription =
            if (showHidden) {
              Strings.transactionCategoryHideHidden
            } else {
              Strings.transactionCategoryShowHidden
            },
          onClick = onToggleHiding,
        )
      }
    }

    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
      if (name.isEmpty()) {
        item(key = NONE_KEY) {
          BottomSheetListItem(
            modifier = Modifier.animateItem(),
            label = Strings.transactionNone,
            isSelected = selected == null,
            onClick = {
              onSelect(null)
              onDismiss()
            },
          )
        }
      }

      matches.fastForEach { group ->
        val groupName = group.name
        if (groupName != null) {
          item(key = group.id.toString()) {
            GroupHeader(
              name = if (group.isHidden) hiddenName(groupName) else groupName,
              modifier = Modifier.animateItem(),
            )
          }
        }

        items(group.categories, key = { it.id.toString() }) { category ->
          BottomSheetListItem(
            modifier = Modifier.animateItem(),
            // A hidden group already says so
            label =
              if (category.isHidden && !group.isHidden) {
                hiddenName(category.name)
              } else {
                category.name
              },
            isSelected = category.id == selected,
            labelColor = if (category.isHidden) colors.pageTextSubdued else Unspecified,
            trailingContent = category.balance?.let { balance -> { CategoryBalance(balance) } },
            onClick = {
              onSelect(category.id)
              onDismiss()
            },
          )
        }
      }
    }
  }

@Composable private fun hiddenName(name: String) = Strings.transactionCategoryHidden(name)

// Qualified, since Amount is also the field
@Composable
private fun CategoryBalance(balance: aktual.budget.model.Amount) =
  Text(
    text = balance.formattedText(),
    fontSize = BalanceSize,
    style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
    color =
      when {
        balance < Zero -> colors.numberNegative
        balance > Zero -> colors.numberPositive
        else -> colors.pageTextSubdued
      },
    maxLines = 1,
  )

// Saving rejects dates before MIN_TRANSACTION_DATE, so they can't be picked. The range still holds
// an older date that's already saved, which the picker would otherwise crash on
@Composable
private fun DateDialog(date: LocalDate, onDismiss: () -> Unit, onSelect: (LocalDate) -> Unit) {
  val first = minOf(MIN_TRANSACTION_DATE.year, date.year)
  val last = maxOf(DatePickerDefaults.YearRange.last, date.year)
  val state =
    rememberDatePickerState(
      initialSelectedDate = date.toJavaLocalDate(),
      yearRange = first..last,
      selectableDates = TransactionDates,
    )
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

private object TransactionDates : SelectableDates {
  private val min = MIN_TRANSACTION_DATE.atStartOfDayIn(UTC).toEpochMilliseconds()

  override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= min

  override fun isSelectableYear(year: Int) = year >= MIN_TRANSACTION_DATE.year
}

private val GroupHeaderPadding =
  PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
private val GroupHeaderSize = 13.sp
private val BalanceSize = 14.sp
private val SearchPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
private const val NONE_KEY = "none"
