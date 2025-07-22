package actual.budget.reports.vm

import actual.account.model.LoginToken
import actual.budget.db.dao.DashboardDao
import actual.budget.db.dao.DashboardDao.Companion.DEFAULT_HEIGHT
import actual.budget.db.dao.DashboardDao.Companion.DEFAULT_WIDTH
import actual.budget.db.dao.DashboardDao.Companion.MAX_WIDTH
import actual.budget.di.BudgetComponentStateHolder
import actual.budget.di.throwIfWrongBudget
import actual.budget.model.BudgetId
import actual.budget.model.WidgetId
import actual.budget.model.WidgetType
import actual.core.model.UuidGenerator
import alakazam.kotlin.logging.Logger
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

@HiltViewModel(assistedFactory = ChooseReportTypeViewModel.Factory::class)
class ChooseReportTypeViewModel @AssistedInject constructor(
  private val uuidGenerator: UuidGenerator,
  budgetComponents: BudgetComponentStateHolder,
  @Assisted inputs: Inputs,
) : ViewModel() {
  // data sources
  private val component = budgetComponents.require()
  private val dao = DashboardDao(component.database)

  // state
  private var job: Job? = null
  private val shouldNavigateChannel = Channel<ShouldNavigateEvent>()
  val shouldNavigateEvent: Flow<ShouldNavigateEvent> = shouldNavigateChannel.receiveAsFlow()

  init {
    component.throwIfWrongBudget(inputs.budgetId)
  }

  override fun onCleared() {
    super.onCleared()
    job?.cancel()
  }

  fun createReport(type: WidgetType) {
    if (type == WidgetType.Custom) {
      Logger.w("Can't create a custom report yet")
      return
    }

    viewModelScope.launch {
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
    else -> JsonObject(emptyMap())
  }

  data class Inputs(
    val token: LoginToken,
    val budgetId: BudgetId,
  )

  data class ShouldNavigateEvent(
    val id: WidgetId,
  )

  @AssistedFactory
  fun interface Factory {
    fun create(
      @Assisted inputs: Inputs,
    ): ChooseReportTypeViewModel
  }
}
