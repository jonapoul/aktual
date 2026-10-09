package aktual.budget.budgeting.vm

import aktual.budget.budgeting.domain.BudgetMonth
import aktual.budget.budgeting.domain.BudgetMonthCalculator
import aktual.budget.model.CategoryId
import aktual.core.Calendar
import aktual.di.BudgetScope
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.yearMonth
import logcat.logcat

/**
 * One category over the twelve months up to the one it opened on, to compare months on a phone.
 * Upstream's mobile category page (packages/desktop-client/src/components/mobile/budget/
 * CategoryPage.tsx) only lists the month's transactions, which this links to.
 */
@Stable
@AssistedInject
class BudgetCategoryViewModel(
  @Assisted private val category: CategoryId,
  @Assisted month: YearMonth,
  calculator: BudgetMonthCalculator,
  calendar: Calendar,
) : ViewModel() {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(BudgetScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(category: CategoryId, month: YearMonth): BudgetCategoryViewModel
  }

  private val selected = MutableStateFlow(month)

  val state: StateFlow<BudgetCategoryState> =
    combine(
        calculator
          .observeBounds()
          .map { bounds -> historyRange(month, bounds) }
          .distinctUntilChanged()
          .flatMapLatest { range -> calculator.observeRange(range) },
        selected,
        calendar.observeToday().map { it.yearMonth }.distinctUntilChanged(),
      ) { months, selected, current ->
        months.toState(selected, current)
      }
      .catch { e ->
        logcat.e(e) { "Failed loading $category history" }
        emit(Failed)
      }
      .stateIn(viewModelScope, Eagerly, Loading)

  // Only months in the history can be picked
  fun select(month: YearMonth) {
    val loaded = state.value as? BudgetCategoryState.Loaded ?: return
    if (loaded.history.any { it.month == month }) selected.update { month }
  }

  private fun List<BudgetMonth>.toState(
    selected: YearMonth,
    current: YearMonth,
  ): BudgetCategoryState {
    val latest = lastOrNull() ?: return Failed
    val details = latest.categories.firstOrNull { it.id == category }
    if (details == null) return Failed
    return BudgetCategoryState.Loaded(
      type = if (latest is Tracking) Tracking else Envelope,
      name = details.name,
      group = latest.groups.firstOrNull { it.id == details.group }?.name.orEmpty(),
      isIncome = details.isIncome,
      selected = selected,
      current = current,
      history = mapNotNull { month ->
          month.categories
            .firstOrNull { it.id == category }
            ?.let { details ->
              CategoryHistoryMonth(
                month = month.month,
                budgeted = details.budgeted,
                spent = details.spent,
                balance = details.balance,
                carryover = details.carryover,
              )
            }
        }
          .toImmutableList(),
    )
  }

  private companion object {
    const val HISTORY_MONTHS = 12

    // Twelve months ending at the given one, inside the budget's bounds
    fun historyRange(month: YearMonth, bounds: ClosedRange<YearMonth>): ClosedRange<YearMonth> {
      val end = month.coerceIn(bounds.start, bounds.endInclusive)
      val start = maxOf(bounds.start, end.minus(HISTORY_MONTHS - 1, MONTH))
      return start..end
    }
  }
}
