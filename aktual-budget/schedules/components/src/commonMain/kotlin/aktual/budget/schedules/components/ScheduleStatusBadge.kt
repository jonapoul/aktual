package aktual.budget.schedules.components

import aktual.budget.schedules.domain.ScheduleStatus
import aktual.core.l10n.Strings
import aktual.core.ui.AktualTheme.colors
import aktual.core.ui.AktualTheme.typography
import aktual.core.ui.Chip
import aktual.core.ui.ChipDefaults
import aktual.core.ui.ColorParams
import aktual.core.ui.ColorParamsProvider
import aktual.core.ui.PreviewWithColorParams
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter

@Composable
fun ScheduleStatusBadge(
  status: ScheduleStatus,
  modifier: Modifier = Modifier,
) {
  val (bgColor, textColor) =
    with(colors) {
      when (status) {
        Missed -> errorBackground to errorTextDarker
        Due -> warningBackground to warningTextDark
        Upcoming -> upcomingBackground to upcomingText
        Paid -> noticeBackgroundLight to noticeText
        Completed -> tableRowHeaderBackground to tableHeaderText
        Scheduled -> tableRowHeaderBackground to tableRowHeaderText
      }
    }
  Chip(
    modifier = modifier,
    text = status.label(),
    colors = ChipDefaults.colors(bgColor, textColor),
    size = Small,
    textStyle = typography.labelSmall,
  )
}

@Composable
fun ScheduleStatus.label(): String =
  when (this) {
    Missed -> Strings.listSchedulesStatusMissed
    Due -> Strings.listSchedulesStatusDue
    Upcoming -> Strings.listSchedulesStatusUpcoming
    Paid -> Strings.listSchedulesStatusPaid
    Completed -> Strings.listSchedulesStatusCompleted
    Scheduled -> Strings.listSchedulesStatusScheduled
  }

@Preview
@Composable
private fun PreviewScheduleStatus(
  @PreviewParameter(ScheduleStatusProvider::class) params: ColorParams<ScheduleStatus>,
) = PreviewWithColorParams(params) { ScheduleStatusBadge(this) }

private class ScheduleStatusProvider : ColorParamsProvider<ScheduleStatus>(ScheduleStatus.entries)
