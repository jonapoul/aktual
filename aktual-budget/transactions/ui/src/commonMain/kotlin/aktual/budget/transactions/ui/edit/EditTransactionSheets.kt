package aktual.budget.transactions.ui.edit

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.Operator.Is
import aktual.budget.model.Operator.IsApprox
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.schedules.components.ScheduleStatusBadge
import aktual.budget.schedules.components.label
import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.ScheduleStatus
import aktual.budget.schedules.domain.amountPrefix
import aktual.budget.transactions.domain.MIN_TRANSACTION_DATE
import aktual.budget.transactions.domain.SplitPart
import aktual.budget.transactions.vm.edit.EntityOption
import aktual.budget.transactions.vm.edit.TransactionForm
import aktual.budget.transactions.vm.edit.TransactionOptions
import aktual.budget.transactions.vm.edit.matching
import aktual.core.icons.Add
import aktual.core.icons.AktualIcons
import aktual.core.icons.Check
import aktual.core.icons.ViewHide
import aktual.core.icons.ViewShow
import aktual.core.l10n.Strings
import aktual.core.ui.AktualModalBottomSheet
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.BottomSheetIcon
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.ListBottomSheet
import aktual.core.ui.NormalIconButton
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.core.ui.formattedText
import aktual.core.ui.scrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
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

    ScheduleLink ->
      ScheduleSheet(
        selected = draft.schedule,
        options = form.options.schedules,
        onDismiss = onDismiss,
        onSelect = { onAction(SetSchedule(it)) },
      )

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

    EditField.Amount,
    Account,
    Date,
    ScheduleLink -> Unit
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
          imageVector = if (showHidden) AktualIcons.ViewHide else AktualIcons.ViewShow,
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
private fun CategoryBalance(balance: Amount) =
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

// As upstream's ScheduleLink modal, typing filters the schedules by their name, payee or account.
// The chips narrow them to the picked statuses, or show them all when none are picked. "None"
// unlinks it, so it's only offered while it's linked
@Composable
private fun ScheduleSheet(
  selected: ScheduleId?,
  options: ImmutableList<Schedule>,
  onDismiss: () -> Unit,
  onSelect: (ScheduleId?) -> Unit,
) =
  AktualModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberBottomSheetState(Hidden),
  ) {
    var statuses by remember { mutableStateOf<ImmutableSet<ScheduleStatus>>(persistentSetOf()) }
    ScheduleSheetContent(
      selected = selected,
      options = options,
      query = rememberTextFieldState(),
      statuses = statuses,
      onToggleStatus = { status ->
        statuses =
          (if (status in statuses) statuses - status else statuses + status).toImmutableSet()
      },
      onSelect = { id ->
        onSelect(id)
        onDismiss()
      },
    )
  }

@Composable
private fun ScheduleSheetContent(
  selected: ScheduleId?,
  options: ImmutableList<Schedule>,
  query: TextFieldState,
  statuses: ImmutableSet<ScheduleStatus>,
  onToggleStatus: (ScheduleStatus) -> Unit,
  onSelect: (ScheduleId?) -> Unit,
) = Column {
  val name = query.text.toString().trim()
  val matches = remember(options, name, statuses) { options.matching(name, statuses) }

  AktualTextField(
    modifier = Modifier.fillMaxWidth().padding(SearchPadding),
    state = query,
    placeholderText = Strings.transactionScheduleSearch,
    singleLine = true,
    clearable = true,
  )

  StatusChips(options = options, selected = statuses, onToggle = onToggleStatus)

  val listState = rememberLazyListState()
  LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
    if (selected != null && name.isEmpty() && statuses.isEmpty()) {
      item(key = NONE_KEY) {
        BottomSheetListItem(
          label = Strings.transactionNone,
          isSelected = false,
          onClick = { onSelect(null) },
        )
      }
    }

    if (options.isEmpty()) {
      item { GroupHeader(Strings.transactionScheduleEmpty) }
    }

    items(matches, key = { it.id.toString() }) { schedule ->
      ScheduleItem(
        schedule = schedule,
        isSelected = schedule.id == selected,
        onClick = { onSelect(schedule.id) },
      )
    }
  }
}

private fun List<Schedule>.matching(
  query: String,
  statuses: Set<ScheduleStatus>,
): ImmutableList<Schedule> = filter { schedule ->
  (statuses.isEmpty() || schedule.status in statuses) &&
    (query.isEmpty() ||
      schedule.name?.contains(query, ignoreCase = true) == true ||
      schedule.payeeName.contains(query, ignoreCase = true) ||
      schedule.accountName.contains(query, ignoreCase = true))
}
  .toImmutableList()

// Only the statuses some schedule has, and none at all when they'd all have the same one
@Composable
private fun StatusChips(
  options: ImmutableList<Schedule>,
  selected: ImmutableSet<ScheduleStatus>,
  onToggle: (ScheduleStatus) -> Unit,
) {
  val statuses = remember(options) { options.mapTo(mutableSetOf()) { it.status }.sorted() }
  if (statuses.size < 2) return

  FlowRow(
    modifier = Modifier.fillMaxWidth().padding(SearchPadding),
    horizontalArrangement = Arrangement.spacedBy(ChipDefaults.Spacing * 2),
    verticalArrangement = Arrangement.spacedBy(ChipDefaults.Spacing * 2),
  ) {
    statuses.fastForEach { status ->
      val isSelected = status in selected
      Chip(
        modifier = Modifier.semantics { this.selected = isSelected },
        text = status.label(),
        colors = ChipDefaults.selectableColors(isSelected),
        onClick = { onToggle(status) },
      )
    }
  }
}

// Its name, then its payee and account, with its amount and next date on the end
@Composable
private fun ScheduleItem(schedule: Schedule, isSelected: Boolean, onClick: () -> Unit) =
  ListItem(
    modifier = Modifier.clickable(onClick = onClick),
    headlineContent = {
      Text(
        text = schedule.name ?: Strings.listSchedulesUnnamedSchedule,
        color = if (schedule.name == null) colors.pageTextSubdued else colors.pageText,
        maxLines = 1,
        overflow = Ellipsis,
      )
    },
    supportingContent = {
      Text(
        text = "${schedule.payeeName} · ${schedule.accountName}",
        color = colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )
    },
    trailingContent = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = CenterVertically,
      ) {
        if (isSelected) BottomSheetIcon(AktualIcons.Check)
        ScheduleAmount(schedule)
      }
    },
    colors = ListItemDefaults.colors(containerColor = Transparent),
  )

@Composable
private fun ScheduleAmount(schedule: Schedule) =
  Column(horizontalAlignment = End) {
    Text(
      text = schedule.amountOp.amountPrefix() + schedule.amount.formattedString(includeSign = true),
      fontSize = BalanceSize,
      style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
      color = if (schedule.amount > Zero) colors.numberPositive else colors.numberNegative,
      maxLines = 1,
    )
    Row(
      horizontalArrangement = Arrangement.spacedBy(ChipDefaults.Spacing),
      verticalAlignment = CenterVertically,
    ) {
      ScheduleStatusBadge(schedule.status)
      Text(
        text = schedule.nextDate.formatted(),
        fontSize = BalanceSize,
        color = colors.pageTextSubdued,
        maxLines = 1,
      )
    }
  }

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
private val GroupHeaderSize = textSizes.smallPlus
private val BalanceSize = textSizes.medium
private val SearchPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
private const val NONE_KEY = "none"

@Preview
@Composable
private fun PreviewScheduleSheet(
  @PreviewParameter(ScheduleSheetProvider::class) params: ColorParams<ScheduleSheetPreview>,
) =
  PreviewWithColorParams(params) {
    ScheduleSheetContent(
      selected = selected,
      options = options,
      query = rememberTextFieldState(),
      statuses = statuses,
      onToggleStatus = {},
      onSelect = {},
    )
  }

@Immutable
private data class ScheduleSheetPreview(
  val options: ImmutableList<Schedule>,
  val selected: ScheduleId? = null,
  val statuses: ImmutableSet<ScheduleStatus> = persistentSetOf(),
)

private class ScheduleSheetProvider :
  ColorParamsProvider<ScheduleSheetPreview>(
    ScheduleSheetPreview(PREVIEW_SCHEDULES, selected = PREVIEW_SCHEDULES[1].id),
    ScheduleSheetPreview(PREVIEW_SCHEDULES, statuses = persistentSetOf(ScheduleStatus.Due)),
    ScheduleSheetPreview(persistentListOf()),
  )

private val PREVIEW_SCHEDULE =
  Schedule(
    id = ScheduleId("rent"),
    name = "Rent",
    ruleId = RuleId("rent"),
    nextDate = LocalDate(2026, 11, 1),
    isCompleted = false,
    postsTransaction = false,
    customUpcomingLength = null,
    payeeId = PayeeId("landlord"),
    payeeName = "Landlord",
    accountId = AccountId("current"),
    accountName = "Current",
    amount = Amount(-120000),
    amountOp = Is,
    date = LocalDate(2026, 11, 1),
    status = Upcoming,
  )

private val PREVIEW_SCHEDULES =
  persistentListOf(
    PREVIEW_SCHEDULE,
    PREVIEW_SCHEDULE.copy(
      id = ScheduleId("energy"),
      name = null,
      payeeName = "Energy Co.",
      amount = Amount(-8500),
      amountOp = IsApprox,
      nextDate = LocalDate(2026, 10, 31),
      status = Due,
    ),
    PREVIEW_SCHEDULE.copy(
      id = ScheduleId("salary"),
      name = "Salary",
      payeeName = "Employer",
      amount = Amount(250000),
      nextDate = LocalDate(2026, 10, 28),
      status = Paid,
    ),
    PREVIEW_SCHEDULE.copy(
      id = ScheduleId("gym"),
      name = "Gym",
      payeeName = "Gym Ltd.",
      amount = Amount(-3000),
      nextDate = LocalDate(2026, 12, 5),
      status = Scheduled,
    ),
  )
