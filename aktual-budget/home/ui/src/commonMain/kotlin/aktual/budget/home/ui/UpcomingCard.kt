package aktual.budget.home.ui

import aktual.budget.home.vm.UpcomingCardState
import aktual.budget.home.vm.UpcomingCardState.Loaded
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.AmountOperator
import aktual.budget.model.Operator
import aktual.budget.model.PayeeId
import aktual.budget.model.RuleId
import aktual.budget.model.ScheduleId
import aktual.budget.model.UpcomingLength
import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.ScheduleStatus
import aktual.budget.schedules.domain.amountPrefix
import aktual.core.l10n.Plurals
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.BareTextButton
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.stringLong
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

private val RowMinHeight = 56.dp
private val BadgeWidth = 44.dp

// Shows nothing until there's something upcoming, so the card doesn't flash in and out
@Composable
internal fun UpcomingCard(
  state: UpcomingCardState,
  onAction: HomeActionHandler,
  modifier: Modifier = Modifier,
) {
  if (state == Failed) {
    HomeCard(modifier = modifier) {
      CardError(message = Strings.homeUpcomingFailed, onAction = onAction)
    }
    return
  }

  if (state !is Loaded) return

  HomeCard(modifier = modifier) {
    UpcomingHeader(state.length, onSeeAll = { onAction(OpenSchedules) })

    state.schedules.fastForEach { schedule ->
      UpcomingRow(
        schedule = schedule,
        today = state.today,
        onClick = { onAction(OpenSchedule(schedule.id)) },
      )
    }

    if (state.hiddenCount > 0) {
      MoreRow(count = state.hiddenCount, onClick = { onAction(OpenSchedules) })
    }

    TotalRow(state.total)
  }
}

@Composable
private fun UpcomingHeader(
  length: UpcomingLength,
  onSeeAll: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = CardPadding),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Top,
  ) {
    Text(
      modifier = Modifier.weight(1f).semantics { heading() },
      text = Strings.homeUpcomingTitle(length.windowString()),
      style = typography.titleSmall,
      fontWeight = SemiBold,
      color = colors.pageTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )

    BareTextButton(text = Strings.homeUpcomingSeeAll, onClick = onSeeAll)
  }
}

@Composable
private fun UpcomingLength.windowString(): String =
  when (this) {
    CurrentMonth -> Strings.homeUpcomingWindowCurrentMonth
    OneMonth -> Plurals.homeUpcomingWindowMonths(1, 1)
    is Days -> Plurals.homeUpcomingWindowDays(count, count)
    is Weeks -> Plurals.homeUpcomingWindowWeeks(count, count)
    is Months -> Plurals.homeUpcomingWindowMonths(count, count)
    is Years -> Plurals.homeUpcomingWindowYears(count, count)
  }

@Composable
private fun UpcomingRow(
  schedule: Schedule,
  today: LocalDate,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isOverdue = schedule.status == Missed

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = RowMinHeight)
        .clickable(onClick = onClick)
        .padding(horizontal = CardPadding, vertical = 6.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = CenterVertically,
  ) {
    DateBadge(schedule.nextDate)

    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(
        text = schedule.name?.takeIf { it.isNotBlank() } ?: schedule.payeeName,
        style = typography.bodyLarge,
        color = colors.pageText,
        maxLines = 1,
        overflow = Ellipsis,
      )

      Text(
        text = Strings.homeUpcomingRowDetail(schedule.accountName, schedule.dueString(today)),
        style = typography.bodySmall,
        color = if (isOverdue) colors.numberNegative else colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )
    }

    AmountText(
      amount = schedule.amount,
      prefix = schedule.amountOp.amountPrefix(),
      style = typography.bodyLarge,
      color = if (schedule.amount < Zero) colors.numberNegative else colors.pageText,
    )
  }
}

// The row's due text already says when it is
@Composable
private fun DateBadge(date: LocalDate, modifier: Modifier = Modifier) {
  Column(
    modifier =
      modifier
        .width(BadgeWidth)
        .background(colors.tableRowHeaderBackground, CardShape)
        .padding(vertical = 4.dp)
        .clearAndSetSemantics {},
    horizontalAlignment = CenterHorizontally,
  ) {
    Text(
      text = date.dayOfWeek.stringLong().take(WEEKDAY_LENGTH).uppercase(),
      style = typography.labelSmall,
      color = colors.pageTextSubdued,
      maxLines = 1,
    )

    Text(
      text = date.day.toString(),
      style = typography.titleMedium.tabularFigures(),
      fontWeight = SemiBold,
      color = colors.pageText,
      maxLines = 1,
    )
  }
}

@Composable
private fun Schedule.dueString(today: LocalDate): String {
  val days = today.daysUntil(nextDate)
  return when {
    days < 0 -> Strings.homeUpcomingOverdue
    days == 0 -> Strings.homeUpcomingToday
    days == 1 -> Strings.homeUpcomingTomorrow
    else -> Plurals.homeUpcomingInDays(days, days)
  }
}

@Composable
private fun MoreRow(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 48.dp)
        .clickable(onClick = onClick)
        .padding(horizontal = CardPadding),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      text = Strings.homeUpcomingMore(count),
      style = typography.bodyMedium,
      color = colors.pageTextSubdued,
    )
  }
}

@Composable
private fun TotalRow(total: Amount, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = CardPadding).padding(top = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = Strings.homeUpcomingTotal,
      style = typography.labelLarge,
      fontWeight = SemiBold,
      color = colors.pageTextSubdued,
      maxLines = 1,
      overflow = Ellipsis,
    )

    AmountText(amount = total, style = typography.labelLarge, color = colors.pageTextSubdued)
  }
}

private const val WEEKDAY_LENGTH = 3

@Preview
@Composable
private fun PreviewUpcomingCard(
  @PreviewParameter(UpcomingCardStateProvider::class) params: ColoredParams<UpcomingCardState>
) =
  PreviewWithColoredParams(params) {
    UpcomingCard(
      modifier = Modifier.padding(16.dp),
      state = this,
      onAction = {},
    )
  }

private class UpcomingCardStateProvider :
  ColoredParameterProvider<UpcomingCardState>(
    PREVIEW_UPCOMING,
    PREVIEW_UPCOMING.copy(length = CurrentMonth, hiddenCount = 3),
    Failed,
  )

private val PREVIEW_TODAY = LocalDate(2026, 4, 1)

internal val PREVIEW_UPCOMING =
  Loaded(
    length = UpcomingLength.Days(count = 7),
    today = PREVIEW_TODAY,
    schedules =
      persistentListOf(
        schedule("Council Tax", "Joint Account", days = -3, amount = -142.00, status = Missed),
        schedule(null, "Current Account", days = 0, amount = -9.99, status = Due),
        schedule(
          "Electricity",
          "Joint Account",
          days = 1,
          amount = -80.00,
          amountOp = Operator.IsApprox,
        ),
        schedule("Salary", "Current Account", days = 5, amount = 2_650.00),
      ),
    hiddenCount = 0,
    total = Amount(2_418.01),
  )

private fun schedule(
  name: String?,
  account: String,
  days: Int,
  amount: Double,
  status: ScheduleStatus = Upcoming,
  amountOp: AmountOperator = Operator.Is,
): Schedule {
  val date = PREVIEW_TODAY.plus(value = days, unit = DAY)
  return Schedule(
    id = ScheduleId("$name-$days"),
    name = name,
    ruleId = RuleId("rule"),
    nextDate = date,
    isCompleted = false,
    postsTransaction = false,
    customUpcomingLength = null,
    payeeId = PayeeId("payee"),
    payeeName = "Streaming Co.",
    accountId = AccountId(account),
    accountName = account,
    amount = Amount(amount),
    amountOp = amountOp,
    date = date,
    status = status,
  )
}
