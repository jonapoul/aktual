package aktual.budget.reports.vm.dashboard

import aktual.budget.BudgetLocalPreferences
import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.DashboardPageId
import aktual.budget.model.DbMetadata
import androidx.compose.runtime.Immutable
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import logcat.logcat

@Immutable data class DashboardPage(val id: DashboardPageId, val name: String)

@Inject
internal class DashboardPages(dao: DashboardDao, private val prefs: BudgetLocalPreferences) {
  val all: Flow<List<DashboardPage>> =
    dao.observePages().map { rows -> rows.map { DashboardPage(it.id, it.name.orEmpty()) } }

  // Falls back to the first page if nothing's been picked yet, or the picked one was deleted
  val selected: Flow<DashboardPage?> =
    combine(all, prefs.observe(SelectedPageKey)) { pages, id ->
        pages.firstOrNull { it.id.value == id } ?: pages.firstOrNull()
      }
      .distinctUntilChanged()

  fun select(id: DashboardPageId) {
    logcat.d { "Selecting dashboard page $id" }
    prefs.update { meta -> meta.set(SelectedPageKey, id.value) }
  }

  private companion object {
    val SelectedPageKey = DbMetadata.StringKey("reports.dashboardPage")
  }
}
