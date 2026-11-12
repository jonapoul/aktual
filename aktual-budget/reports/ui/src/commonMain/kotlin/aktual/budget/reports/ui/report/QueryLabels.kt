package aktual.budget.reports.ui.report

import aktual.budget.reports.vm.TimeFrameMode
import aktual.core.l10n.Strings
import androidx.compose.runtime.Composable

@Composable
internal fun TimeFrameMode.label(): String =
  when (this) {
    SlidingWindow -> Strings.reportsDateConfigLive
    Static -> Strings.reportsDateConfigStatic
    Full -> Strings.reportsDateTypeAllTime
    LastMonth -> Strings.reportsDateTypeLastMonth
    LastYear -> Strings.reportsDateTypeLastYear
    YearToDate -> Strings.reportsDateTypeYearToDate
    PriorYearToDate -> Strings.reportsDateTypePriorYearToDate
    CurrentQuarter -> Strings.reportsDateTypeCurrentQuarter
    PreviousQuarter -> Strings.reportsDateTypePreviousQuarter
    Unknown -> Strings.reportsDateTypeUnknown
  }
