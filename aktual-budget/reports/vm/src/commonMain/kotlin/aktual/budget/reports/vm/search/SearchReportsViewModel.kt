package aktual.budget.reports.vm.search

import aktual.budget.db.Dashboard
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.CustomReportId
import aktual.budget.reports.vm.CustomReportMeta
import aktual.budget.reports.vm.FormulaReportMeta
import aktual.budget.reports.vm.MarkdownReportMeta
import aktual.budget.reports.vm.UnsupportedReportMeta
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.dashboard.DashboardPage
import aktual.budget.reports.vm.dashboard.DashboardPages
import aktual.budget.reports.vm.dashboard.name
import aktual.budget.reports.vm.search.SearchReportsState.Results
import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Stable
@AssistedInject
class SearchReportsViewModel
internal constructor(
  @Assisted private val savedState: SavedStateHandle,
  dashboardDao: DashboardDao,
  customReportsDao: CustomReportsDao,
  pages: DashboardPages,
  private val decoder: DashboardItemDecoder,
) : ViewModel() {
  @AssistedFactory
  @ViewModelAssistedFactoryKey(SearchReportsViewModel::class)
  @ContributesIntoMap(BudgetScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    override fun create(extras: CreationExtras): SearchReportsViewModel =
      create(extras.createSavedStateHandle())

    fun create(@Assisted savedState: SavedStateHandle): SearchReportsViewModel
  }

  val query: StateFlow<String> = savedState.getStateFlow(KEY_QUERY, initialValue = "")

  private val groups: Flow<List<SearchReportsGroup>> =
    combine(pages.all, dashboardDao.observeAll(), customReportsDao.observeNames(), ::mapGroups)

  val state: StateFlow<SearchReportsState> =
    combine(query, groups, ::search).stateIn(viewModelScope, Eagerly, initialValue = NoQuery)

  fun setQuery(query: String) {
    savedState[KEY_QUERY] = query
  }

  private fun mapGroups(
    pages: List<DashboardPage>,
    widgets: List<Dashboard>,
    customReportNames: Map<CustomReportId, String>,
  ): List<SearchReportsGroup> {
    val byPage = widgets.groupBy { it.dashboard_page_id }
    return pages.map { page ->
      val items =
        byPage[page.id].orEmpty().mapNotNull { widget ->
          val type = widget.type ?: return@mapNotNull null
          val item = decoder.decode(widget) ?: return@mapNotNull null
          val meta = item.meta
          SearchReportsItem(
            id = item.id,
            type = type,
            name =
              @Suppress("ElseCaseInsteadOfExhaustiveWhen")
              when (meta) {
                is CustomReportMeta -> customReportNames[meta.id]
                is UnsupportedReportMeta -> (meta.raw["name"] as? JsonPrimitive)?.contentOrNull
                else -> item.name
              },
            content =
              @Suppress("ElseCaseInsteadOfExhaustiveWhen")
              when (meta) {
                is MarkdownReportMeta -> meta.content.markdownToPlainText()
                is FormulaReportMeta -> meta.formula?.collapseWhitespace()
                else -> null
              },
          )
        }
      SearchReportsGroup(page, items.toImmutableList())
    }
  }

  private companion object {
    const val KEY_QUERY = "query"
  }
}

internal fun search(query: String, groups: List<SearchReportsGroup>): SearchReportsState {
  val trimmed = query.trim()
  if (trimmed.isEmpty()) return NoQuery

  val matching = groups.mapNotNull { group ->
    val items =
      group.items
        .filter { it.matches(trimmed) }
        .map { item -> item.copy(content = item.content?.let { snippet(it, trimmed) }) }
    if (items.isEmpty()) null else group.copy(items = items.toImmutableList())
  }

  return if (matching.isEmpty()) {
    NoResults
  } else {
    Results(trimmed, matching.toImmutableList())
  }
}

private fun SearchReportsItem.matches(query: String): Boolean {
  val typeQuery = query.filterNot { it.isWhitespace() || it == '-' }
  return name?.contains(query, ignoreCase = true) == true ||
    content?.contains(query, ignoreCase = true) == true ||
    (typeQuery.isNotEmpty() && type.name.contains(typeQuery, ignoreCase = true))
}
