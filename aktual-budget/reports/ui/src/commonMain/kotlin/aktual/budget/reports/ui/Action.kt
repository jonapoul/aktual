package aktual.budget.reports.ui

import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.CalendarDay
import aktual.budget.reports.vm.SummaryChartType
import aktual.budget.reports.vm.TextData
import aktual.budget.reports.vm.dashboard.DashboardItem
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface Action {
  data object NavBack : Action

  @JvmInline value class OpenItem(val id: WidgetId) : Action

  data class Rename(val item: DashboardItem, val name: String) : Action

  @JvmInline value class Delete(val id: WidgetId) : Action

  @JvmInline value class SetSummaryType(val type: SummaryChartType) : Action

  @JvmInline value class SetAllTimeDivisor(val allTime: Boolean) : Action

  @JvmInline value class ClickCalendarDay(val day: CalendarDay) : Action

  data class SaveTextContent(val data: TextData, val newContent: String) : Action

  data object CreateNewReport : Action
}

@Immutable
internal fun interface ActionListener {
  operator fun invoke(action: Action)
}
