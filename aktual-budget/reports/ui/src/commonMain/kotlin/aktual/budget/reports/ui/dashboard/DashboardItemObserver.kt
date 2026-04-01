package aktual.budget.reports.ui.dashboard

import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.dashboard.DashboardItem
import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.Flow

@Immutable
internal fun interface DashboardItemObserver {
  operator fun invoke(item: DashboardItem): Flow<ChartData>
}
