package aktual.budget.reports.ui

import aktual.budget.model.DashboardPageId
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.CalendarDay
import aktual.budget.reports.vm.SummaryChartType
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

  @JvmInline value class SaveTextContent(val content: String) : Action

  @JvmInline value class SetUnsavedText(val hasUnsavedText: Boolean) : Action

  data object CreateNewReport : Action

  data object OpenSearch : Action

  @JvmInline value class SelectPage(val id: DashboardPageId) : Action

  @JvmInline value class CreatePage(val name: String) : Action

  data class RenamePage(val id: DashboardPageId, val name: String) : Action

  @JvmInline value class DeletePage(val id: DashboardPageId) : Action
}

@Immutable
internal fun interface ActionListener {
  operator fun invoke(action: Action)
}
