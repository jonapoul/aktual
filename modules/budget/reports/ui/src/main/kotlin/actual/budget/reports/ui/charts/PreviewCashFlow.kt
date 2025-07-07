package actual.budget.reports.ui.charts

import actual.budget.model.Amount
import actual.budget.reports.ui.date
import actual.budget.reports.vm.CashFlowData
import actual.budget.reports.vm.CashFlowDatum
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.Month

internal object PreviewCashFlow {
  fun datum(income: Int, expenses: Int, transfers: Int, balance: Int) = CashFlowDatum(
    income = Amount(income.toDouble()),
    expenses = Amount(expenses.toDouble()),
    transfers = Amount(transfers.toDouble()),
    balance = Amount(balance.toDouble()),
  )

  val DATA = CashFlowData(
    items = persistentMapOf(
      date(2024, Month.JULY) to datum(income = 6683, expenses = -4695, transfers = -1779, balance = 4781),
      date(2024, Month.AUGUST) to datum(income = 6071, expenses = -4111, transfers = -729, balance = 6012),
      date(2024, Month.SEPTEMBER) to datum(income = 6041, expenses = -4233, transfers = -779, balance = 7041),
      date(2024, Month.OCTOBER) to datum(income = 6041, expenses = -3602, transfers = -3819, balance = 5662),
      date(2024, Month.NOVEMBER) to datum(income = 9200, expenses = -5191, transfers = -1111, balance = 8560),
      date(2024, Month.DECEMBER) to datum(income = 27, expenses = -4536, transfers = -4508, balance = 2389),
      date(2025, Month.JANUARY) to datum(income = 34551, expenses = -17336, transfers = -3477, balance = 15403),
      date(2025, Month.FEBRUARY) to datum(income = 9913, expenses = -12977, transfers = -834, balance = 11505),
      date(2025, Month.MARCH) to datum(income = 9850, expenses = -7413, transfers = -4146, balance = 9796),
      date(2025, Month.APRIL) to datum(income = 10218, expenses = -6161, transfers = -4990, balance = 8862),
      date(2025, Month.MAY) to datum(income = 9791, expenses = -5751, transfers = -2422, balance = 10480),
      date(2025, Month.JUNE) to datum(income = 143, expenses = -745, transfers = -4041, balance = 5836),
    ),
  )
}
