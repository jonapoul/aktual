package actual.budget.reports.vm

import actual.budget.db.dao.DashboardDao
import actual.budget.db.dao.DashboardDao.Companion.DEFAULT_HEIGHT
import actual.budget.db.dao.DashboardDao.Companion.DEFAULT_WIDTH
import actual.budget.db.dao.DashboardDao.Companion.MAX_WIDTH
import actual.budget.model.BudgetId
import actual.budget.model.WidgetId
import actual.budget.model.WidgetType
import actual.core.di.AssistedFactoryKey
import actual.core.di.BudgetGraphHolder
import actual.core.di.ViewModelAssistedFactory
import actual.core.di.ViewModelScope
import actual.core.di.throwIfWrongBudget
import actual.core.model.Empty
import actual.core.model.UuidGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import logcat.logcat

@AssistedInject
class ChooseReportTypeViewModel(
  private val uuidGenerator: UuidGenerator,
  budgetComponents: BudgetGraphHolder,
  @Assisted budgetId: BudgetId,
) : ViewModel() {
  // data sources
  private val budgetGraph = budgetComponents.require()
  private val dao = DashboardDao(budgetGraph.database)

  // state
  private var job: Job? = null
  private val shouldNavigateChannel = Channel<ShouldNavigateEvent>()
  val shouldNavigateEvent: Flow<ShouldNavigateEvent> = shouldNavigateChannel.receiveAsFlow()

  init {
    budgetGraph.throwIfWrongBudget(budgetId)
  }

  override fun onCleared() {
    super.onCleared()
    job?.cancel()
  }

  fun createReport(type: WidgetType) {
    if (type == WidgetType.Custom) {
      logcat.w { "Can't create a custom report yet" }
      return
    }

    job?.cancel()
    job = viewModelScope.launch {
      val widgetId = WidgetId(uuidGenerator())
      val (x, y) = newWidgetPosition()
      dao.insert(widgetId, type, x, y, buildEmptyMetadata(type))
      shouldNavigateChannel.send(ShouldNavigateEvent(widgetId))
    }
  }

  private data class Coords(val x: Long = 0, val y: Long = 0)

  private suspend fun newWidgetPosition(): Coords {
    val positions = dao.getPositionAndSize()
    if (positions.isEmpty()) return Coords()

    val highest = positions.maxWith(compareBy({ it.y }, { it.x }))
    val x = highest.x ?: 0L
    val y = highest.y ?: 0L
    val w = highest.width ?: 0L

    return if (x + w >= MAX_WIDTH) {
      // start of the next row down
      Coords(y = y + DEFAULT_HEIGHT)
    } else {
      // next column over on the same row
      Coords(x = x + DEFAULT_WIDTH, y = y)
    }
  }

  private fun buildEmptyMetadata(type: WidgetType): JsonObject = when (type) {
    // TODO: implement properly
    else -> JsonObject.Empty
  }

  data class ShouldNavigateEvent(
    val id: WidgetId,
  )

  @AssistedFactory
  @AssistedFactoryKey(Factory::class)
  @ContributesIntoMap(ViewModelScope::class)
  fun interface Factory : ViewModelAssistedFactory {
    fun create(
      @Assisted budgetId: BudgetId,
    ): ChooseReportTypeViewModel
  }
}
