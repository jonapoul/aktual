package aktual.budget.reports.ui.choosetype

import aktual.budget.model.WidgetType
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface ChooseReportTypeAction {
  data class Create(val type: WidgetType) : ChooseReportTypeAction

  data object ShowDisabledDialog : ChooseReportTypeAction

  data object DismissDialog : ChooseReportTypeAction
}
