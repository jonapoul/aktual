package aktual.budget.reports.ui.report

import aktual.budget.reports.vm.CustomReportConfig
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface CustomReportAction {
  data object Back : CustomReportAction

  data object StartEditing : CustomReportAction

  data object StopEditing : CustomReportAction

  data object Revert : CustomReportAction

  @JvmInline
  value class Edit(val transform: (CustomReportConfig) -> CustomReportConfig) : CustomReportAction
}
