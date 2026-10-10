package aktual.budget.home.vm

import aktual.budget.budgeting.domain.BudgetMonthCalculator
import aktual.core.Calendar
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.yearMonth

@Inject
class ThisMonthLoader(
  private val calculator: BudgetMonthCalculator,
  private val calendar: Calendar,
) {
  fun observe(): Flow<ThisMonth> =
    calendar.observeToday().flatMapLatest { today ->
      calculator.observe(today.yearMonth).map { budget -> ThisMonth(today, budget) }
    }
}
