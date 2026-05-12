package aktual.budget.reports.vm

import aktual.budget.db.BudgetDatabase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// TODO: Complete
@Inject
@Suppress("unused", "ExpressionBodySyntax")
internal class ChartDataLoader(database: BudgetDatabase) {
  private val queries = database.transactionsQueries

  fun cashFlow(meta: CashFlowReportMeta): Flow<CashFlowData> {
    return flowOf()
  }
}
