package aktual.budget.schedules.ui.list

import aktual.budget.model.Operator
import aktual.budget.schedules.vm.Schedule
import aktual.budget.schedules.vm.ScheduleStatus
import aktual.core.l10n.Strings
import aktual.core.theme.LocalTheme
import aktual.core.theme.Theme
import aktual.core.ui.AktualTypography
import aktual.core.ui.CardShape
import aktual.core.ui.LocalDateFormatter
import aktual.core.ui.PreviewWithTheme
import aktual.core.ui.PreviewWithThemedParams
import aktual.core.ui.RowShape
import aktual.core.ui.ThemeParameters
import aktual.core.ui.ThemedParameterProvider
import aktual.core.ui.ThemedParams
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
  theme: Theme = LocalTheme.current,
) {
  val dateFormatter = LocalDateFormatter.current

  @Suppress("ElseCaseInsteadOfExhaustiveWhen")
  val amountPrefix =
    when (schedule.amountOp) {
      is Operator.IsApprox,
      is Operator.IsBetween -> "~"
      else -> ""
    }
  val amountStr = amountPrefix + schedule.amount.formattedString(includeSign = true)

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(theme.tableBackground, RowShape)
        .border(Dp.Hairline, theme.tableBorder, RowShape)
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
        style = AktualTypography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = if (schedule.name != null) theme.pageText else theme.pageTextSubdued,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      FlowRow(horizontalArrangement = Arrangement.spacedBy(ListSchedulesDS.itemMetaGroupSpacing)) {
        LabelValue(
          label = Strings.listSchedulesLabelPayee,
          value = schedule.payeeName,
          theme = theme,
        )
        LabelValue(
          label = Strings.listSchedulesLabelAccount,
          value = schedule.accountName,
          theme = theme,
        )

        LabelValue(label = Strings.listSchedulesLabelAmount, value = amountStr, theme = theme)

        LabelValue(
          label = Strings.listSchedulesLabelNext,
          value = dateFormatter.format(schedule.nextDate),
          theme = theme,
        )
      }
    }

    ScheduleStatusBadge(schedule.status, theme = theme)
  }
}

@Composable
private fun LabelValue(
  label: String,
  value: String,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(ListSchedulesDS.itemContentSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, style = AktualTypography.bodySmall, color = theme.pageTextSubdued)
    Text(value, style = AktualTypography.bodySmall, color = theme.pageText)
  }
}

@Composable
private fun ScheduleStatusBadge(
  status: ScheduleStatus,
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val (bgColor, textColor) =
    when (status) {
      ScheduleStatus.Missed -> theme.errorBackground to theme.errorTextDarker
      ScheduleStatus.Due -> theme.warningBackground to theme.warningTextDark
      ScheduleStatus.Upcoming -> theme.upcomingBackground to theme.upcomingText
      ScheduleStatus.Paid -> theme.noticeBackgroundLight to theme.noticeText
      ScheduleStatus.Completed -> theme.tableRowHeaderBackground to theme.tableHeaderText
      ScheduleStatus.Scheduled -> theme.tableRowHeaderBackground to theme.tableRowHeaderText
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
    Text(text = label, style = AktualTypography.labelSmall, color = textColor, maxLines = 1)
  }
}

/** Keep in sync with [ListSchedulesItem] */
@Composable
internal fun ShimmerListSchedulesItem(
  modifier: Modifier = Modifier,
  theme: Theme = LocalTheme.current,
) {
  val shimmer = rememberShimmer(ShimmerBounds.Window)
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RowShape)
        .background(theme.tableBackground, RowShape)
        .border(Dp.Hairline, theme.tableBorder, RowShape)
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
            .background(theme.pageText, CardShape)
      )

      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.85f)
            .height(ListSchedulesDS.shimmerItemTextHeightSmall)
            .background(theme.pageText, CardShape)
      )

      Box(
        modifier =
          Modifier.fillMaxWidth(fraction = 0.75f)
            .height(ListSchedulesDS.shimmerItemTextHeightSmall)
            .background(theme.pageText, CardShape)
      )
    }

    Box(
      modifier =
        Modifier.height(ListSchedulesDS.shimmerItemTextHeight)
          .width(40.dp)
          .background(theme.pageText, CardShape)
    )
  }
}

@Preview
@Composable
private fun PreviewScheduleStatus(
  @PreviewParameter(ScheduleStatusProvider::class) params: ThemedParams<ScheduleStatus>
) = PreviewWithThemedParams(params) { ScheduleStatusBadge(this) }

private class ScheduleStatusProvider :
  ThemedParameterProvider<ScheduleStatus>(ScheduleStatus.entries)

@Preview
@Composable
private fun PreviewListItem(
  @PreviewParameter(SchedulesProvider::class) params: ThemedParams<Schedule>
) = PreviewWithThemedParams(params) { ListSchedulesItem(schedule = this, onAction = {}) }

private class SchedulesProvider :
  ThemedParameterProvider<Schedule>(ListSchedulesPreview.scheduleA, ListSchedulesPreview.scheduleB)

@Preview
@Composable
private fun PreviewLoadingListItem(@PreviewParameter(ThemeParameters::class) theme: Theme) =
  PreviewWithTheme(theme) { ShimmerListSchedulesItem() }
