package aktual.budget.reports.ui.choosetype

import aktual.budget.model.CustomReportId
import aktual.budget.model.WidgetType
import androidx.compose.runtime.Immutable

@Immutable internal sealed interface ChooseReportTypeAction

@JvmInline internal value class Create(val type: WidgetType) : ChooseReportTypeAction

@JvmInline internal value class AddSavedReport(val id: CustomReportId) : ChooseReportTypeAction

internal data object ShowDisabledDialog : ChooseReportTypeAction

internal data object DismissDialog : ChooseReportTypeAction

@Immutable
internal fun interface ChooseReportTypeActionHandler {
  operator fun invoke(action: ChooseReportTypeAction)
}
