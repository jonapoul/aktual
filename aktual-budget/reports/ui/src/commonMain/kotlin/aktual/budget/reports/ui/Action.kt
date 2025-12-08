package aktual.budget.reports.ui

import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.CalendarDay
import aktual.budget.reports.vm.SummaryChartType
import aktual.budget.reports.vm.TextData
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface Action {
  data class OpenItem(val id: WidgetId) : Action
  data class Rename(val id: WidgetId) : Action
  data class Delete(val id: WidgetId) : Action
  data class SetSummaryType(val type: SummaryChartType) : Action
  data class SetAllTimeDivisor(val allTime: Boolean) : Action
  data class ClickCalendarDay(val day: CalendarDay) : Action
  data class SaveTextContent(val data: TextData, val newContent: String) : Action
  data object CreateNewReport : Action
}

@Immutable
internal fun interface ActionListener {
  operator fun invoke(action: Action)
}
