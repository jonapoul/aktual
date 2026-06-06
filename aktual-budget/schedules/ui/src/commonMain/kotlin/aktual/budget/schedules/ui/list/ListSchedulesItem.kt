package aktual.budget.schedules.ui.list

import aktual.budget.model.Operator
import aktual.budget.schedules.vm.Schedule
import aktual.budget.schedules.vm.ScheduleStatus
import aktual.core.l10n.Strings
import aktual.core.theme.Colors
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.CardShape
import aktual.core.ui.ColoredParameterProvider
import aktual.core.ui.ColoredParameters
import aktual.core.ui.ColoredParams
import aktual.core.ui.PreviewWithColoredParams
import aktual.core.ui.PreviewWithColors
import aktual.core.ui.RowShape
import aktual.core.ui.formatted
import aktual.core.ui.formattedString
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.ShimmerBounds
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

/** Keep in sync with [ShimmerListSchedulesItem] */
@Composable
internal fun ListSchedulesItem(
  schedule: Schedule,
  onAction: ListSchedulesActionHandler,
  modifier: Modifier = Modifier,
) {
  val amountPrefix =
    when (schedule.amountOp) {
      Operator.IsApprox,
      Operator.IsBetween -> "~"

      Operator.GreaterThan,
      Operator.GreaterThanOrEquals,
      Operator.Is,
      Operator.LessThan,
      Operator.LessThanOrEquals -> ""
    }
  val amountStr = amountPrefix + schedule.amount.formattedString(includeSign = true)

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Dp.Hairline, colors.tableBorder, RowShape)
        .clickable { onAction(Open(schedule.id)) }
        .padding(ListSchedulesDS.itemCardPadding),
    horizontalArrangement =
      Arrangement.spacedBy(ListSchedulesDS.itemHorizontalSpacing, Alignment.Start),
    verticalAlignment = Alignment.Top,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(ListSchedulesDS.itemContentSpacing),
    ) {
      Text(
        text = schedule.name ?: Strings.listSchedulesUnnamedSchedule,
        style = typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = if (schedule.name != null) colors.pageText else colors.pageTextSubdued,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      FlowRow(horizontalArrangement = Arrangement.spacedBy(ListSchedulesDS.itemMetaGroupSpacing)) {
        LabelValue(
          label = Strings.listSchedulesLabelPayee,
          value = schedule.payeeName,
        )
        LabelValue(
          label = Strings.listSchedulesLabelAccount,
          value = schedule.accountName,
        )

        LabelValue(
          label = Strings.listSchedulesLabelAmount,
          value = amountStr,
          valueColor =
            if (schedule.amount.isPositive()) {
              colors.budgetNumberPositive
            } else {
              colors.budgetNumberNegative
            },
        )

        LabelValue(
          label = Strings.listSchedulesLabelNext,
          value = schedule.nextDate.formatted(),
        )
      }
    }

    ScheduleStatusBadge(schedule.status)
  }
}

@Composable
private fun LabelValue(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  valueColor: Color = colors.pageText,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(ListSchedulesDS.itemContentSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, style = typography.bodySmall, color = colors.pageTextSubdued)
    Text(value, style = typography.bodySmall, color = valueColor)
  }
}

@Composable
private fun ScheduleStatusBadge(
  status: ScheduleStatus,
  modifier: Modifier = Modifier,
) {
  val (bgColor, textColor) =
    when (status) {
      ScheduleStatus.Missed -> colors.errorBackground to colors.errorTextDarker
      ScheduleStatus.Due -> colors.warningBackground to colors.warningTextDark
      ScheduleStatus.Upcoming -> colors.upcomingBackground to colors.upcomingText
      ScheduleStatus.Paid -> colors.noticeBackgroundLight to colors.noticeText
      ScheduleStatus.Completed -> colors.tableRowHeaderBackground to colors.tableHeaderText
      ScheduleStatus.Scheduled -> colors.tableRowHeaderBackground to colors.tableRowHeaderText
    }
  val label =
    when (status) {
      ScheduleStatus.Missed -> Strings.listSchedulesStatusMissed
      ScheduleStatus.Due -> Strings.listSchedulesStatusDue
      ScheduleStatus.Upcoming -> Strings.listSchedulesStatusUpcoming
      ScheduleStatus.Paid -> Strings.listSchedulesStatusPaid
      ScheduleStatus.Completed -> Strings.listSchedulesStatusCompleted
      ScheduleStatus.Scheduled -> Strings.listSchedulesStatusScheduled
    }
  Box(
    modifier = modifier.background(bgColor, CardShape).padding(ListSchedulesDS.statusBadgePadding)
  ) {
    Text(text = label, style = typography.labelSmall, color = textColor, maxLines = 1)
  }
}

/** Keep in sync with [ListSchedulesItem] */
@Composable
internal fun ShimmerListSchedulesItem(modifier: Modifier = Modifier) {
  val shimmer = rememberShimmer(ShimmerBounds.Window)
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Dp.Hairline, colors.tableBorder, RowShape)
        .padding(ListSchedulesDS.itemCardPadding)
        .shimmer(shimmer),
    horizontalArrangement =
      Arrangement.spacedBy(ListSchedulesDS.itemHorizontalSpacing, Alignment.Start),
    verticalAlignment = Alignment.Top,
  ) {
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(ListSchedulesDS.itemContentSpacing),
    ) {
      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.45f)
            .height(ListSchedulesDS.shimmerItemTextHeight)
            .background(colors.pageText, CardShape)
      )

      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.85f)
            .height(ListSchedulesDS.shimmerItemTextHeightSmall)
            .background(colors.pageText, CardShape)
      )

      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.75f)
            .height(ListSchedulesDS.shimmerItemTextHeightSmall)
            .background(colors.pageText, CardShape)
      )
    }

    Box(
      modifier =
        Modifier.height(ListSchedulesDS.shimmerItemTextHeight)
          .width(40.dp)
          .background(colors.pageText, CardShape)
    )
  }
}

@Preview
@Composable
private fun PreviewScheduleStatus(
  @PreviewParameter(ScheduleStatusProvider::class) params: ColoredParams<ScheduleStatus>
) = PreviewWithColoredParams(params) { ScheduleStatusBadge(this) }

private class ScheduleStatusProvider :
  ColoredParameterProvider<ScheduleStatus>(ScheduleStatus.entries)

@Preview
@Composable
private fun PreviewListItem(
  @PreviewParameter(SchedulesProvider::class) params: ColoredParams<Schedule>
) = PreviewWithColoredParams(params) { ListSchedulesItem(schedule = this, onAction = {}) }

private class SchedulesProvider :
  ColoredParameterProvider<Schedule>(ListSchedulesPreview.scheduleA, ListSchedulesPreview.scheduleB)

@Preview
@Composable
private fun PreviewLoadingListItem(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ShimmerListSchedulesItem() }
