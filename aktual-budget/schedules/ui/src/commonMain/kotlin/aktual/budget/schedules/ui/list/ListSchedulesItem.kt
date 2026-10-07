package aktual.budget.schedules.ui.list

import aktual.budget.schedules.domain.Schedule
import aktual.budget.schedules.domain.ScheduleStatus
import aktual.budget.schedules.domain.amountPrefix
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
import aktual.core.ui.rememberHighlighted
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
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.rememberShimmer
import com.valentinilk.shimmer.shimmer

/** Keep in sync with [ShimmerListSchedulesItem] */
@Composable
internal fun ListSchedulesItem(
  schedule: Schedule,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  query: String = "",
) {
  val amountPrefix = schedule.amountOp.amountPrefix()
  val amountStr = amountPrefix + schedule.amount.formattedString(includeSign = true)
  val textColor = if (schedule.isCompleted) colors.pageTextSubdued else colors.pageText

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Hairline, colors.tableBorder, RowShape)
        .clickable(onClick = onClick)
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
        text =
          schedule.name?.let { rememberHighlighted(it, query) }
            ?: AnnotatedString(Strings.listSchedulesUnnamedSchedule),
        style = typography.bodyMedium,
        fontWeight = SemiBold,
        color = if (schedule.name != null) textColor else colors.pageTextSubdued,
        maxLines = 1,
        overflow = Ellipsis,
      )

      FlowRow(horizontalArrangement = Arrangement.spacedBy(ListSchedulesDS.itemMetaGroupSpacing)) {
        LabelValue(
          label = Strings.listSchedulesLabelPayee,
          value = schedule.payeeName,
          valueColor = textColor,
          query = query,
        )
        LabelValue(
          label = Strings.listSchedulesLabelAccount,
          value = schedule.accountName,
          valueColor = textColor,
          query = query,
        )

        LabelValue(
          label = Strings.listSchedulesLabelAmount,
          value = amountStr,
          valueColor =
            when {
              schedule.isCompleted -> textColor
              schedule.amount.isPositive() -> colors.budgetNumberPositive
              else -> colors.budgetNumberNegative
            },
        )

        LabelValue(
          label = Strings.listSchedulesLabelNext,
          value = schedule.nextDate.formatted(),
          valueColor = textColor,
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
  query: String = "",
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(ListSchedulesDS.itemContentSpacing),
    verticalAlignment = CenterVertically,
  ) {
    Text(label, style = typography.bodySmall, color = colors.pageTextSubdued)
    Text(rememberHighlighted(value, query), style = typography.bodySmall, color = valueColor)
  }
}

@Composable
internal fun ScheduleStatusBadge(
  status: ScheduleStatus,
  modifier: Modifier = Modifier,
) {
  val (bgColor, textColor) =
    when (status) {
      Missed -> colors.errorBackground to colors.errorTextDarker
      Due -> colors.warningBackground to colors.warningTextDark
      Upcoming -> colors.upcomingBackground to colors.upcomingText
      Paid -> colors.noticeBackgroundLight to colors.noticeText
      Completed -> colors.tableRowHeaderBackground to colors.tableHeaderText
      Scheduled -> colors.tableRowHeaderBackground to colors.tableRowHeaderText
    }
  val label =
    when (status) {
      Missed -> Strings.listSchedulesStatusMissed
      Due -> Strings.listSchedulesStatusDue
      Upcoming -> Strings.listSchedulesStatusUpcoming
      Paid -> Strings.listSchedulesStatusPaid
      Completed -> Strings.listSchedulesStatusCompleted
      Scheduled -> Strings.listSchedulesStatusScheduled
    }
  Box(
    modifier = modifier.background(bgColor, CardShape).padding(ListSchedulesDS.statusBadgePadding),
  ) {
    Text(text = label, style = typography.labelSmall, color = textColor, maxLines = 1)
  }
}

/** Keep in sync with [ListSchedulesItem] */
@Composable
internal fun ShimmerListSchedulesItem(modifier: Modifier = Modifier) {
  val shimmer = rememberShimmer(Window)
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(colors.tableBackground, RowShape)
        .border(Hairline, colors.tableBorder, RowShape)
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
            .background(colors.pageText, CardShape),
      )

      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.85f)
            .height(ListSchedulesDS.shimmerItemTextHeightSmall)
            .background(colors.pageText, CardShape),
      )

      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.75f)
            .height(ListSchedulesDS.shimmerItemTextHeightSmall)
            .background(colors.pageText, CardShape),
      )
    }

    Box(
      modifier =
        Modifier.height(ListSchedulesDS.shimmerItemTextHeight)
          .width(40.dp)
          .background(colors.pageText, CardShape),
    )
  }
}

@Preview
@Composable
private fun PreviewScheduleStatus(
  @PreviewParameter(ScheduleStatusProvider::class) params: ColoredParams<ScheduleStatus>,
) = PreviewWithColoredParams(params) { ScheduleStatusBadge(this) }

private class ScheduleStatusProvider :
  ColoredParameterProvider<ScheduleStatus>(ScheduleStatus.entries)

@Preview
@Composable
private fun PreviewListItem(
  @PreviewParameter(SchedulesProvider::class) params: ColoredParams<Schedule>,
) = PreviewWithColoredParams(params) { ListSchedulesItem(schedule = this, onClick = {}) }

private class SchedulesProvider :
  ColoredParameterProvider<Schedule>(
    ListSchedulesPreview.scheduleA,
    ListSchedulesPreview.scheduleB,
    ListSchedulesPreview.scheduleCompleted,
  )

@Preview
@Composable
private fun PreviewLoadingListItem(@PreviewParameter(ColoredParameters::class) colors: Colors) =
  PreviewWithColors(colors) { ShimmerListSchedulesItem() }
