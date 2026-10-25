package aktual.budget.home.vm

import aktual.budget.model.WidgetId
import aktual.budget.reports.vm.pinned.PinnedReport
import aktual.budget.reports.vm.pinned.PinnedReports
import aktual.budget.reports.vm.pinned.ReportPage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

internal class FakePinnedReports(
  reports: List<PinnedReport> = emptyList(),
  choices: ImmutableList<ReportPage> = persistentListOf(),
) : PinnedReports {
  val reports = MutableStateFlow(reports)
  val choices = MutableStateFlow(choices)
  val calls = mutableListOf<String>()

  override val ids: Flow<List<WidgetId>> = this.reports.map { list -> list.map { it.summary.id } }

  override fun observe(): Flow<ImmutableList<PinnedReport>> = reports.map { it.toImmutableList() }

  override fun observeChoices(): Flow<ImmutableList<ReportPage>> = choices

  override suspend fun pin(id: WidgetId) {
    calls += "pin ${id.value}"
  }

  override suspend fun unpin(id: WidgetId) {
    calls += "unpin ${id.value}"
    reports.update { list -> list.filter { it.summary.id != id } }
  }

  override suspend fun replace(old: WidgetId, new: WidgetId) {
    calls += "replace ${old.value} ${new.value}"
  }
}
