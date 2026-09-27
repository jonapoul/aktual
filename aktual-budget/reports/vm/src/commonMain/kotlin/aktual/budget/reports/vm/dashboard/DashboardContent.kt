package aktual.budget.reports.vm.dashboard

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class DashboardContent(val page: DashboardPage?, val items: ImmutableList<DashboardItem>)
