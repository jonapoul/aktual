package aktual.budget.reports.components

import aktual.budget.reports.vm.CalendarData
import aktual.budget.reports.vm.CustomDrilldown
import aktual.budget.reports.vm.NetWorthMode
import aktual.budget.reports.vm.SummaryChartType
import aktual.budget.reports.vm.TextAlign
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

@Immutable
sealed interface ChartAction {
  @JvmInline value class SetNetWorthMode(val mode: NetWorthMode) : ChartAction

  @JvmInline value class SetSummaryType(val type: SummaryChartType) : ChartAction

  @JvmInline value class SetAllTimeDivisor(val allTime: Boolean) : ChartAction

  data class ClickCalendarDay(val data: CalendarData, val date: LocalDate) : ChartAction

  @JvmInline value class DrillDown(val drilldown: CustomDrilldown) : ChartAction

  data class SaveText(val content: String, val align: TextAlign) : ChartAction

  @JvmInline value class SetUnsavedText(val hasUnsavedText: Boolean) : ChartAction
}

@Immutable
fun interface ChartActionListener {
  operator fun invoke(action: ChartAction)
}
