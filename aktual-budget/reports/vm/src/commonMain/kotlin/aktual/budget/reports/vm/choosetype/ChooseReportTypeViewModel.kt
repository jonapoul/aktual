package aktual.budget.reports.vm.choosetype

import aktual.budget.db.dao.DashboardDao
import aktual.budget.model.WidgetId
import aktual.budget.model.WidgetType
import aktual.core.model.Empty
import aktual.core.model.UuidGenerator
import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import logcat.logcat

@Stable
@ViewModelKey
@ContributesIntoMap(BudgetScope::class)
class ChooseReportTypeViewModel(
  private val uuidGenerator: UuidGenerator,
  private val dashboardDao: DashboardDao,
) : ViewModel() {
  private var job: Job? = null
  private val shouldNavigateChannel = Channel<ShouldNavigateEvent>()
  private val mutableDialog = MutableStateFlow<ChooseReportTypeDialog?>(null)

  val shouldNavigateEvent: Flow<ShouldNavigateEvent> = shouldNavigateChannel.receiveAsFlow()
  val dialog: StateFlow<ChooseReportTypeDialog?> = mutableDialog.asStateFlow()

  override fun onCleared() {
    super.onCleared()
    job?.cancel()
  }

  fun hideDialogs() {
    mutableDialog.update { null }
  }

  fun show(dialog: ChooseReportTypeDialog) {
    mutableDialog.update { dialog }
  }

  fun createReport(type: WidgetType) {
    if (type == WidgetType.Custom) {
      logcat.w { "Can't create a custom report yet" }
      return
    }

    job?.cancel()
    job = viewModelScope.launch {
      val widgetId = uuidGenerator(::WidgetId)
      val (x, y) = newWidgetPosition()
      dashboardDao.insert(widgetId, type, x, y, buildEmptyMetadata(type))
      shouldNavigateChannel.send(ShouldNavigateEvent(widgetId))
    }
  }

  private data class Coords(val x: Long = 0, val y: Long = 0)

  private suspend fun newWidgetPosition(): Coords {
    val positions = dashboardDao.getPositionAndSize()
    if (positions.isEmpty()) return Coords()

    val highest = positions.maxWith(compareBy({ it.y }, { it.x }))
    val x = highest.x ?: 0L
    val y = highest.y ?: 0L
    val w = highest.width ?: 0L

    return if (x + w >= DashboardDao.MAX_WIDTH) {
      // start of the next row down
      Coords(y = y + DashboardDao.DEFAULT_HEIGHT)
    } else {
      // next column over on the same row
      Coords(x = x + DashboardDao.DEFAULT_WIDTH, y = y)
    }
  }

  @Suppress("ElseCaseInsteadOfExhaustiveWhen")
  private fun buildEmptyMetadata(type: WidgetType): JsonObject =
    when (type) {
      // TODO: implement properly
      else -> JsonObject.Empty
    }

  data class ShouldNavigateEvent(val id: WidgetId)
}
