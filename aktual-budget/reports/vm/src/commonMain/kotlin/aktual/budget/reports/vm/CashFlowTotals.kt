package aktual.budget.reports.vm

import aktual.budget.db.CashFlowByMonth
import aktual.budget.db.ReportTransactions
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

// The cashFlowStartingBalance and cashFlowByMonth queries run over transactions that matched a
// report's conditions, which run from before [start] so the starting balance is filtered too
internal fun cashFlowTotals(
  rows: List<ReportTransactions>,
  start: LocalDate,
): Pair<Long, List<CashFlowByMonth>> {
  val onBudget = rows.filter { it.off_budget == false }
  val (before, during) = onBudget.partition { it.date < start }
  val byMonth =
    during
      .filter { it.amount != 0L }
      .groupBy { row ->
        CashFlowByMonth(
          month = row.date.year * YEAR_MONTH_FACTOR + row.date.month.number,
          is_transfer = row.transfer_acct != null,
          is_income = row.amount > 0,
          total = 0L,
        )
      }
      .map { (key, group) -> key.copy(total = group.sumOf { it.amount }) }
  return before.sumOf { it.amount } to byMonth
}

private const val YEAR_MONTH_FACTOR = 100L
