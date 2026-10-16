package aktual.budget.reports.vm.choosetype

import aktual.budget.model.CustomReportId
import androidx.compose.runtime.Immutable

@Immutable data class SavedCustomReport(val id: CustomReportId, val name: String)
