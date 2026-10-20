package aktual.budget.reports.ui.report

import aktual.budget.reports.vm.CustomDrilldown
import aktual.budget.reports.vm.CustomReportConfig
import androidx.compose.runtime.Immutable

// Along with the shared [ReportEditorAction]s
@Immutable
internal sealed interface CustomReportAction {
  @JvmInline
  value class Edit(val transform: (CustomReportConfig) -> CustomReportConfig) : CustomReportAction

  @JvmInline value class DrillDown(val drilldown: CustomDrilldown) : CustomReportAction
}
