package aktual.budget.reports.vm.pinned

import aktual.budget.model.WidgetId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow

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
