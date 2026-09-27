package aktual.budget.reports.vm.search

import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.budget.reports.vm.dashboard.DashboardPage
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface SearchReportsState {
  data object NoQuery : SearchReportsState

  data object NoResults : SearchReportsState

  data class Results(val query: String, val groups: ImmutableList<SearchReportsGroup>) :
    SearchReportsState
}

@Immutable
data class SearchReportsGroup(
  val page: DashboardPage,
  val items: ImmutableList<SearchReportsItem>,
)

@Immutable
data class SearchReportsItem(
  val id: WidgetId,
  val type: WidgetType,
  val name: String?,
  val content: String?,
)
