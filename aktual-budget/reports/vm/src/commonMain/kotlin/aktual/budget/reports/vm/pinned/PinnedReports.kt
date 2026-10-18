package aktual.budget.reports.vm.pinned

import aktual.budget.BudgetLocalPreferences
import aktual.budget.db.Dashboard
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.CustomReportId
import aktual.budget.model.DbMetadata
import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.ChartData
import aktual.budget.reports.vm.ChartDataLoader
import aktual.budget.reports.vm.ReportMeta
import aktual.budget.reports.vm.dashboard.DashboardItemDecoder
import aktual.budget.reports.vm.dashboard.DashboardPages
import aktual.budget.reports.vm.dashboard.displayName
import aktual.budget.reports.vm.pinned.PinnedReports.Companion.MAX_PINNED_REPORTS
import aktual.di.BudgetScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import logcat.logcat

// Reports shown on the home page, saved locally like the home card order. Pins whose report has
// since been deleted or can't be shown are skipped, and dropped on the next change
interface PinnedReports {
  val ids: Flow<List<WidgetId>>

  // Charts load separately, so one slow report doesn't hold up the rest
  fun observe(): Flow<ImmutableList<PinnedReport>>

  // Every report on the dashboard, by page
  fun observeChoices(): Flow<ImmutableList<ReportPage>>

  suspend fun pin(id: WidgetId)

  suspend fun unpin(id: WidgetId)

  // Takes the old report's place
  suspend fun replace(old: WidgetId, new: WidgetId)

  companion object {
    const val MAX_PINNED_REPORTS = 3
  }
}

@Inject
@ContributesBinding(BudgetScope::class)
@OptIn(ExperimentalCoroutinesApi::class)
class PinnedReportsImpl
internal constructor(
  private val prefs: BudgetLocalPreferences,
  private val dashboardDao: DashboardDao,
  private val customReportsDao: CustomReportsDao,
  private val pages: DashboardPages,
  private val decoder: DashboardItemDecoder,
  private val chartDataLoader: ChartDataLoader,
) : PinnedReports {
  override val ids: Flow<List<WidgetId>> =
    combine(prefs.observe(DbMetadata.HomeReports), dashboardDao.observeAll()) { saved, widgets ->
        val live = widgets.pinnableIds()
        saved.orEmpty().mapNotNull { WidgetId(it).takeIf { id -> id in live } }
      }
      .distinctUntilChanged()

  override fun observe(): Flow<ImmutableList<PinnedReport>> =
    combine(ids, dashboardDao.observeAll(), customReportsDao.observeNames()) { ids, widgets, names
        ->
        val byId = widgets.associateBy { it.id }
        ids.mapNotNull { id -> byId[id]?.toPinned(names) }
      }
      .distinctUntilChanged()
      .flatMapLatest { pinned ->
        if (pinned.isEmpty()) {
          flowOf(persistentListOf())
        } else {
          val reports = pinned.map { (summary, meta) ->
            chartDataLoader
              .load(meta)
              .map<ChartData, ChartData?> { it }
              .onStart { emit(null) }
              .map { data -> PinnedReport(summary, data) }
          }
          combine(reports) { it.toImmutableList() }
        }
      }

  override fun observeChoices(): Flow<ImmutableList<ReportPage>> =
    combine(pages.all, dashboardDao.observeAll(), customReportsDao.observeNames()) {
      pages,
      widgets,
      names ->
      val byPage = widgets.groupBy { it.dashboard_page_id }
      pages
        .map { page ->
          val reports = byPage[page.id].orEmpty().mapNotNull { it.toPinned(names)?.first }
          ReportPage(page, reports.toImmutableList())
        }
        .filter { it.reports.isNotEmpty() }
        .toImmutableList()
    }

  override suspend fun pin(id: WidgetId) = edit { ids ->
    if (id in ids || ids.size >= MAX_PINNED_REPORTS) {
      logcat.w { "Can't pin $id to home with $ids" }
      ids
    } else {
      ids + id
    }
  }

  override suspend fun unpin(id: WidgetId) = edit { ids -> ids - id }

  override suspend fun replace(old: WidgetId, new: WidgetId) = edit { ids ->
    if (new in ids) ids else ids.map { if (it == old) new else it }
  }

  private suspend fun edit(change: (List<WidgetId>) -> List<WidgetId>) {
    val live = dashboardDao.observeAll().first().pinnableIds()
    prefs.update { meta ->
      val ids =
        meta[DbMetadata.HomeReports].orEmpty().mapNotNull {
          WidgetId(it).takeIf { id -> id in live }
        }
      meta.set(DbMetadata.HomeReports, change(ids).map { it.value })
    }
  }

  // Widgets with no type can't be shown, so they shouldn't take up a slot either
  private fun List<Dashboard>.pinnableIds(): Set<WidgetId> =
    mapNotNullTo(HashSet()) { widget -> widget.id.takeIf { widget.type != null } }

  private fun Dashboard.toPinned(
    names: Map<CustomReportId, String>,
  ): Pair<ReportSummary, ReportMeta>? {
    val type = type ?: return null
    val item = decoder.decode(this) ?: return null
    return ReportSummary(id, type, item.displayName(names)) to item.meta
  }
}
