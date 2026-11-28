package aktual.budget.transactions.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.PayeeId
import aktual.budget.schedules.components.SchedulePicker
import aktual.budget.transactions.domain.MIN_TRANSACTION_DATE
import aktual.budget.transactions.domain.SplitPart
import aktual.budget.transactions.vm.edit.EntityOption
import aktual.budget.transactions.vm.edit.NearbyPayeeOption
import aktual.budget.transactions.vm.edit.TransactionForm
import aktual.budget.transactions.vm.edit.TransactionOptions
import aktual.budget.transactions.vm.edit.matching
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.ViewHide
import aktual.core.icons.ViewShow
import aktual.core.l10n.Strings
import aktual.core.ui.AktualDatePickerDialog
import aktual.core.ui.AktualIconButton
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.BottomSheetIcon
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.BottomSheetMessage
import aktual.core.ui.DialogTextButton
import aktual.core.ui.OnAction
import aktual.core.ui.checkbox
import aktual.core.ui.formatted
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.getSelectedDate
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
  onAction: OnAction<EditTransactionAction>,
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
        nearby = form.options.nearbyPayees,
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
        closed = form.options.closedAccounts,
        onDismiss = onDismiss,
        onSelect = { onAction(SetAccount(it)) },
      )

    Date ->
      DateDialog(
        date = draft.date,
        syncTransferDate = form.syncTransferDate,
        onDismiss = onDismiss,
        onSelect = { onAction(SetDate(it)) },
        onSyncTransferDate = { onAction(SetSyncTransferDate(it)) },
      )

    Notes ->
      NotesSheet(
        notes = draft.notes,
        tags = form.options.tags,
        onDismiss = onDismiss,
        onSave = { onAction(SetNotes(it)) },
        onCreateTag = { onAction(CreateTag(it)) },
      )

    ScheduleLink ->
      AktualModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(Hidden),
      ) {
        SchedulePicker(
          selected = draft.schedule,
          options = form.options.schedules,
          completed = form.options.completedSchedules,
          onSelect = { id ->
            onAction(SetSchedule(id))
            onDismiss()
          },
          onUnlink = {
            onAction(SetSchedule(null))
            onDismiss()
          },
        )
      }

    EditField.Amount,
    null -> Unit
  }
}

// A split's part has its parent's account and date
@Composable
private fun PartSheet(
  field: EditField,
  part: SplitPart,
  form: TransactionForm,
  onAction: OnAction<EditTransactionAction>,
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

    EditField.Amount,
    Account,
    Date,
    ScheduleLink -> Unit
  }

// As upstream's payee picker: "None" is only offered when there's a payee to remove, typing a
// name that no payee has offers to create it, the nearby then suggested payees come first and the
// accounts to transfer to come last
@Composable
private fun PayeeSheet(
  selected: PayeeId?,
  options: ImmutableList<EntityOption<PayeeId>>,
  suggested: ImmutableList<EntityOption<PayeeId>>,
  transfers: ImmutableList<EntityOption<PayeeId>>,
  onDismiss: () -> Unit,
  onSelect: (PayeeId?) -> Unit,
  onCreate: (String) -> Unit,
  nearby: ImmutableList<NearbyPayeeOption> = persistentListOf(),
) =
  AktualModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberBottomSheetState(Hidden),
  ) {
    val query = rememberTextFieldState()
    val name = query.text.toString().trim()
    val nearbyMatches =
      remember(nearby, name) {
        nearby.filter { it.name.contains(name, ignoreCase = true) }.toImmutableList()
      }
    // A nearby payee is only listed with them
    val nearbyIds = remember(nearby) { nearby.map { it.id }.toSet() }
    val matches =
      remember(options, name, nearbyIds) {
        options
          .filter { it.id !in nearbyIds && it.name.contains(name, ignoreCase = true) }
          .toImmutableList()
      }
    val suggestedMatches =
      remember(suggested, name, nearbyIds) {
        suggested
          .filter { it.id !in nearbyIds && it.name.contains(name, ignoreCase = true) }
          .toImmutableList()
      }
    val transferMatches =
      remember(transfers, name) {
        transfers.filter { it.name.contains(name, ignoreCase = true) }.toImmutableList()
      }
    val isNew =
      name.isNotEmpty() &&
        nearbyMatches.none { it.name.equals(name, ignoreCase = true) } &&
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
            leadingContent = { BottomSheetIcon(AktualIcons.Add) },
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

      if (nearbyMatches.isNotEmpty()) {
        item { GroupHeader(Strings.transactionPayeeNearby) }
        nearbyPayeeItems(nearbyMatches, selected, onSelect, onDismiss)
      }

      // The rest only need a header to tell them from the suggestions
      if (suggestedMatches.isNotEmpty()) {
        item { GroupHeader(Strings.transactionPayeeSuggested) }
        payeeItems(suggestedMatches, selected, onSelect, onDismiss)
      }
      if (matches.isNotEmpty() && (suggestedMatches.isNotEmpty() || nearbyMatches.isNotEmpty())) {
        item { GroupHeader(Strings.transactionPayeeAll) }
      }

      payeeItems(matches, selected, onSelect, onDismiss)

      if (transferMatches.isNotEmpty()) {
        item { GroupHeader(Strings.transactionsTransfer) }
      }

      payeeItems(transferMatches, selected, onSelect, onDismiss)
    }
  }

private fun LazyListScope.nearbyPayeeItems(
  payees: ImmutableList<NearbyPayeeOption>,
  selected: PayeeId?,
  onSelect: (PayeeId?) -> Unit,
  onDismiss: () -> Unit,
) =
  items(payees, key = { it.id.toString() }) { payee ->
    BottomSheetListItem(
      label = payee.name,
      isSelected = payee.id == selected,
      trailingContent = {
        Text(
          text = payee.distance.formatted(),
          fontSize = DistanceSize,
          color = colors.textTertiary,
        )
      },
      onClick = {
        onSelect(payee.id)
        onDismiss()
      },
    )
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
    color = colors.textTertiary,
  )

// Closed accounts are listed after the rest once they're shown, which they are from the start if
// the transaction's in one
@Composable
private fun AccountSheet(
  selected: AccountId?,
  options: ImmutableList<EntityOption<AccountId>>,
  closed: ImmutableList<EntityOption<AccountId>>,
  onDismiss: () -> Unit,
  onSelect: (AccountId) -> Unit,
) =
  AktualModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberBottomSheetState(Hidden),
  ) {
    var showClosed by rememberSaveable { mutableStateOf(closed.any { it.id == selected }) }
    val query = rememberTextFieldState()
    val name = query.text.toString().trim()
    val matches = remember(options, name) { options.matching(name) }
    val closedMatches =
      remember(closed, name, showClosed) {
        if (showClosed) closed.matching(name) else persistentListOf()
      }

    SearchRow(
      query = query,
      placeholder = Strings.searchAccounts,
      toggle =
        if (closed.isEmpty()) {
          null
        } else {
          HidingToggle(
            isShown = showClosed,
            showText = Strings.transactionAccountShowClosed,
            hideText = Strings.transactionAccountHideClosed,
            onClick = { showClosed = !showClosed },
          )
        },
    )

    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
      if (name.isNotEmpty() && matches.isEmpty() && closedMatches.isEmpty()) noResults()

      accountItems(matches, selected, isClosed = false, onSelect, onDismiss)

      if (closedMatches.isNotEmpty()) {
        item(key = CLOSED_KEY) {
          GroupHeader(Strings.transactionAccountClosed, Modifier.animateItem())
        }
      }

      accountItems(closedMatches, selected, isClosed = true, onSelect, onDismiss)
    }
  }

private fun ImmutableList<EntityOption<AccountId>>.matching(query: String) =
  if (query.isEmpty()) {
    this
  } else {
    filter { it.name.contains(query, ignoreCase = true) }.toImmutableList()
  }

private fun LazyListScope.accountItems(
  accounts: ImmutableList<EntityOption<AccountId>>,
  selected: AccountId?,
  isClosed: Boolean,
  onSelect: (AccountId) -> Unit,
  onDismiss: () -> Unit,
) =
  items(accounts, key = { it.id.toString() }) { account ->
    BottomSheetListItem(
      modifier = Modifier.animateItem(),
      label = account.name,
      isSelected = account.id == selected,
      labelColor = if (isClosed) colors.textTertiary else Unspecified,
      onClick = {
        onSelect(account.id)
        onDismiss()
      },
    )
  }

@Immutable
private data class HidingToggle(
  val isShown: Boolean,
  val showText: String,
  val hideText: String,
  val onClick: () -> Unit,
)

// The search field, with the eye button to show or hide what's hidden on its end
@Composable
private fun SearchRow(
  query: TextFieldState,
  placeholder: String,
  toggle: HidingToggle? = null,
) =
  Row(
    modifier = Modifier.fillMaxWidth().padding(SearchPadding),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = CenterVertically,
  ) {
    AktualTextField(
      modifier = Modifier.weight(1f),
      state = query,
      placeholderText = placeholder,
      singleLine = true,
      clearable = true,
    )

    if (toggle != null) {
      AktualIconButton(
        kind = Normal,
        imageVector = if (toggle.isShown) AktualIcons.ViewHide else AktualIcons.ViewShow,
        contentDescription = if (toggle.isShown) toggle.hideText else toggle.showText,
        onClick = toggle.onClick,
      )
    }
  }

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

    SearchRow(
      query = query,
      placeholder = Strings.transactionCategorySearch,
      toggle =
        if (options.hasHiddenCategories) {
          HidingToggle(
            isShown = options.showHiddenCategories,
            showText = Strings.transactionCategoryShowHidden,
            hideText = Strings.transactionCategoryHideHidden,
            onClick = onToggleHiding,
          )
        } else {
          null
        },
    )

    val listState = rememberLazyListState()
    LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
      if (name.isNotEmpty() && matches.isEmpty()) noResults()

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
            labelColor = if (category.isHidden) colors.textTertiary else Unspecified,
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

private fun LazyListScope.noResults() =
  item(key = NO_RESULTS_KEY) { BottomSheetMessage(Strings.searchNoResults, Modifier.animateItem()) }

@Composable private fun hiddenName(name: String) = Strings.transactionCategoryHidden(name)

// Qualified, since Amount is also the field
@Composable
private fun CategoryBalance(balance: Amount) =
  Text(
    text = balance.formattedText(),
    fontSize = BalanceSize,
    style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
    color =
      when {
        balance < Zero -> colors.numberNegative
        balance > Zero -> colors.numberPositive
        else -> colors.textTertiary
      },
    maxLines = 1,
  )

// Saving rejects dates before MIN_TRANSACTION_DATE, so they can't be picked. The range still holds
// an older date that's already saved, which the picker would otherwise crash on
@Composable
private fun DateDialog(
  date: LocalDate,
  syncTransferDate: Boolean?,
  onDismiss: () -> Unit,
  onSelect: (LocalDate) -> Unit,
  onSyncTransferDate: (Boolean) -> Unit,
) {
  val first = minOf(MIN_TRANSACTION_DATE.year, date.year)
  val last = maxOf(DatePickerDefaults.YearRange.last, date.year)
  val state =
    rememberDatePickerState(
      initialSelectedDate = date.toJavaLocalDate(),
      yearRange = first..last,
      selectableDates = TransactionDates,
    )
  AktualDatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      DialogTextButton(
        kind = Primary,
        text = Strings.transactionSheetDone,
        isEnabled = state.selectedDateMillis != null,
        onClick = {
          state.getSelectedDate()?.toKotlinLocalDate()?.let(onSelect)
          onDismiss()
        },
      )
    },
    dismissButton = {
      DialogTextButton(text = Strings.transactionSheetCancel, onClick = onDismiss)
    },
    content = {
      DatePicker(state = state)
      if (syncTransferDate != null) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .toggleable(
                value = syncTransferDate,
                role = Role.Checkbox,
                onValueChange = onSyncTransferDate,
              )
              .padding(horizontal = 12.dp, vertical = 4.dp),
          verticalAlignment = CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Checkbox(checked = syncTransferDate, onCheckedChange = null, colors = colors.checkbox())
          Text(text = Strings.transactionSyncTransferDate, color = colors.text)
        }
      }
    },
  )
}

private object TransactionDates : SelectableDates {
  private val min = MIN_TRANSACTION_DATE.atStartOfDayIn(UTC).toEpochMilliseconds()

  override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= min

  override fun isSelectableYear(year: Int) = year >= MIN_TRANSACTION_DATE.year
}

private val GroupHeaderPadding =
  PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
private val GroupHeaderSize = textSizes.smallPlus
private val DistanceSize = textSizes.small
private val BalanceSize = textSizes.medium
private val SearchPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
private const val NONE_KEY = "none"
private const val CLOSED_KEY = "closed"
private const val NO_RESULTS_KEY = "no-results"
