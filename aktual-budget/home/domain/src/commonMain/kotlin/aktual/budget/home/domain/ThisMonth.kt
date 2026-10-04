package aktual.budget.home.domain

import aktual.budget.budgeting.domain.BudgetMonth
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

data class ThisMonth(val today: LocalDate, val budget: BudgetMonth) {
  val daysLeft: Int
    get() = today.daysUntil(budget.month.lastDay)
}
