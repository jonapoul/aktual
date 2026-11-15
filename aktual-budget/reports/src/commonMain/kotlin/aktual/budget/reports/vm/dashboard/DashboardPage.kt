package aktual.budget.reports.vm.dashboard

import aktual.budget.model.DashboardPageId
import androidx.compose.runtime.Immutable

@Immutable data class DashboardPage(val id: DashboardPageId, val name: String)
