package actual.budget.reports.ui

import actual.budget.model.CustomReportId
import actual.budget.reports.vm.CalendarDay
import actual.budget.reports.vm.SummaryChartType
import actual.budget.reports.vm.TextData
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface Action {
  data class OpenItem(val id: CustomReportId) : Action
  data class Rename(val id: CustomReportId) : Action
  data class Delete(val id: CustomReportId) : Action
  data class SetSummaryType(val type: SummaryChartType) : Action
  data class SetAllTimeDivisor(val allTime: Boolean) : Action
  data class ClickCalendarDay(val day: CalendarDay) : Action
  data class SaveTextContent(val data: TextData, val newContent: String) : Action
}

@Immutable
internal fun interface ActionListener {
  operator fun invoke(action: Action)
}
