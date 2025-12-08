package aktual.budget.reports.vm

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface DashboardState {
  data object Loading : DashboardState
  data object Empty : DashboardState
  data class Loaded(val items: ImmutableList<ReportDashboardItem>) : DashboardState
}
