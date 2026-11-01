package aktual.budget.reports.ui.report

import androidx.compose.runtime.Immutable

// Shared by the reports that edit a draft of their settings, previewed by the chart until saved
@Immutable
internal sealed interface ReportEditorAction : CustomReportAction {
  data object Back : ReportEditorAction

  data object StartEditing : ReportEditorAction

  data object StopEditing : ReportEditorAction

  data object Revert : ReportEditorAction

  data object Save : ReportEditorAction
}
