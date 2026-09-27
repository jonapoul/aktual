package aktual.budget.reports.vm.dashboard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class DashboardContent(
  val page: DashboardPage?,
  // Null while loading
  val items: ImmutableList<DashboardItem>?,
)
