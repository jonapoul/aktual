package actual.budget.reports.ui

import actual.budget.model.CustomReportId
import androidx.compose.runtime.Immutable

@Immutable
internal sealed interface Action {
  data class OpenItem(val id: CustomReportId) : Action
  data class Rename(val id: CustomReportId) : Action
  data class Delete(val id: CustomReportId) : Action
}
