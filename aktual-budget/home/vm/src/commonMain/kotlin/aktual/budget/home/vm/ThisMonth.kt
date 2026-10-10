package aktual.budget.home.vm

import aktual.budget.budgeting.domain.BudgetMonth
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

internal data class ThisMonth(val today: LocalDate, val budget: BudgetMonth) {
  val daysLeft: Int
    get() = today.daysUntil(budget.month.lastDay)
}
