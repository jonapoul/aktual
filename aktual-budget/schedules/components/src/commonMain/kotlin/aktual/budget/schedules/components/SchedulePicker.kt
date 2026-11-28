package aktual.budget.schedules.components

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.Operator.Is
import aktual.budget.model.Operator.IsApprox
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.ScheduleStatus
import aktual.budget.schedules.domain.amountPrefix
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTextField
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.textSizes
import aktual.core.ui.AnimatedFlowRow
import aktual.core.ui.BottomSheetListItem
import aktual.core.ui.BottomSheetMessage
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import aktual.core.ui.radioButton
import aktual.core.ui.scrollbar
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
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

// As upstream's ScheduleLink modal, typing filters the schedules by their name, payee or account.
// The chips narrow them to the picked statuses, or show them all when none are picked. "None" runs
// [onUnlink], so it's only offered with one while it's linked. Completed schedules are only listed
// while their chip is picked
@Composable
fun SchedulePicker(
  selected: ScheduleId?,
  options: ImmutableList<Schedule>,
  completed: ImmutableList<Schedule>,
  onSelect: (ScheduleId) -> Unit,
  onUnlink: (() -> Unit)?,
) {
  var statuses by remember { mutableStateOf<ImmutableSet<ScheduleStatus>>(persistentSetOf()) }
  SchedulePickerContent(
    selected = selected,
    options = options,
    completed = completed,
    query = rememberTextFieldState(),
    statuses = statuses,
    onToggleStatus = { status ->
      statuses = (if (status in statuses) statuses - status else statuses + status).toImmutableSet()
    },
    onSelect = onSelect,
    onUnlink = onUnlink,
  )
}

@Composable
private fun SchedulePickerContent(
  selected: ScheduleId?,
  options: ImmutableList<Schedule>,
  completed: ImmutableList<Schedule>,
  query: TextFieldState,
  statuses: ImmutableSet<ScheduleStatus>,
  onToggleStatus: (ScheduleStatus) -> Unit,
  onSelect: (ScheduleId) -> Unit,
  onUnlink: (() -> Unit)?,
) = Column {
  val name = query.text.toString().trim()
  val all = remember(options, completed) { (options + completed).toImmutableList() }
  val shown = if (Completed in statuses) all else options
  val matches = remember(shown, name, statuses) { shown.matching(name, statuses) }
  val isFiltering = name.isNotEmpty() || statuses.isNotEmpty()

  AktualTextField(
    modifier = Modifier.fillMaxWidth().padding(SearchPadding),
    state = query,
    placeholderText = Strings.transactionScheduleSearch,
    singleLine = true,
    clearable = true,
  )

  StatusChips(options = all, selected = statuses, onToggle = onToggleStatus)

  val listState = rememberLazyListState()
  LazyColumn(modifier = Modifier.scrollbar(listState), state = listState) {
    if (onUnlink != null && selected != null && !isFiltering) {
      item(key = "none") {
        BottomSheetListItem(
          modifier = Modifier.animateItem(),
          label = Strings.transactionNone,
          isSelected = false,
          onClick = onUnlink,
        )
      }
    }

    if (all.isEmpty()) {
      item(key = "empty") {
        BottomSheetMessage(
          text = Strings.transactionScheduleEmpty,
          modifier = Modifier.animateItem(),
        )
      }
    } else if (matches.isEmpty() && isFiltering) {
      item(key = "no-results") {
        BottomSheetMessage(text = Strings.searchNoResults, modifier = Modifier.animateItem())
      }
    }

    items(matches, key = { it.id.toString() }) { schedule ->
      ScheduleItem(
        modifier = Modifier.animateItem(),
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

  AnimatedFlowRow(modifier = Modifier.fillMaxWidth().padding(SearchPadding)) {
    statuses.fastForEach { status ->
      item(key = status) {
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
}

// Its name, payee and account, with its amount, next date and status on the end
@Composable
private fun ScheduleItem(
  schedule: Schedule,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val background by animateColorAsState(if (isSelected) colors.accentSubtle else Transparent)
  ListItem(
    modifier =
      modifier
        .background(background)
        .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick),
    headlineContent = {
      Text(
        text = schedule.name ?: Strings.listSchedulesUnnamedSchedule,
        color =
          when {
            isSelected -> colors.accentSubtleText
            schedule.name == null || schedule.isCompleted -> colors.textTertiary
            else -> colors.text
          },
        fontWeight = if (isSelected) Medium else null,
        maxLines = 1,
        overflow = Ellipsis,
      )
    },
    supportingContent = {
      Column {
        Text(
          text = schedule.payeeName,
          color = colors.textTertiary,
          maxLines = 1,
          overflow = Ellipsis,
        )
        Text(
          text = schedule.accountName,
          color = colors.textTertiary,
          maxLines = 1,
          overflow = Ellipsis,
        )
      }
    },
    trailingContent = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = CenterVertically,
      ) {
        ScheduleAmount(schedule)
        RadioButton(selected = isSelected, onClick = null, colors = colors.radioButton())
      }
    },
    colors = ListItemDefaults.colors(containerColor = Transparent),
  )
}

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
    Text(
      text = schedule.nextDate.formatted(),
      fontSize = BalanceSize,
      color = colors.textTertiary,
      maxLines = 1,
    )
    ScheduleStatusBadge(schedule.status)
  }

@Preview
@Composable
private fun PreviewSchedulePicker(
  @PreviewParameter(SchedulePickerProvider::class) params: ColorParams<SchedulePickerPreview>,
) =
  PreviewWithColorParams(params) {
    SchedulePickerContent(
      selected = selected,
      options = options,
      completed = completed,
      query = rememberTextFieldState(query),
      statuses = statuses,
      onToggleStatus = {},
      onSelect = {},
      onUnlink = {},
    )
  }

@Immutable
private data class SchedulePickerPreview(
  val options: ImmutableList<Schedule>,
  val selected: ScheduleId? = null,
  val statuses: ImmutableSet<ScheduleStatus> = persistentSetOf(),
  val completed: ImmutableList<Schedule> = PREVIEW_COMPLETED,
  val query: String = "",
)

private class SchedulePickerProvider :
  ColorParamsProvider<SchedulePickerPreview>(
    SchedulePickerPreview(PREVIEW_SCHEDULES, selected = PREVIEW_SCHEDULES[1].id),
    SchedulePickerPreview(PREVIEW_SCHEDULES, statuses = persistentSetOf(Due)),
    SchedulePickerPreview(
      PREVIEW_SCHEDULES,
      statuses = persistentSetOf(Completed),
    ),
    SchedulePickerPreview(PREVIEW_SCHEDULES, query = "Insurance"),
    SchedulePickerPreview(persistentListOf(), completed = persistentListOf()),
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

private val PREVIEW_COMPLETED =
  persistentListOf(
    PREVIEW_SCHEDULE.copy(
      id = ScheduleId("loan"),
      name = "Car loan",
      payeeName = "Lender",
      amount = Amount(-25000),
      nextDate = LocalDate(2026, 6, 1),
      isCompleted = true,
      status = Completed,
    ),
  )

private val BalanceSize = textSizes.medium
private val SearchPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
