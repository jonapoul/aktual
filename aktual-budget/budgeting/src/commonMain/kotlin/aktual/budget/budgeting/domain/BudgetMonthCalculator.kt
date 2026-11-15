package aktual.budget.budgeting.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

interface BudgetMonthCalculator {
  fun observe(month: YearMonth): Flow<BudgetMonth>

  // One walk from the budget start covers every month in the range
  fun observeRange(months: ClosedRange<YearMonth>): Flow<ImmutableList<BudgetMonth>>

  // packages/loot-core/src/server/budget/app.ts get-budget-bounds
  fun observeBounds(): Flow<ClosedRange<YearMonth>>
}
