package aktual.budget.reports.vm

import aktual.budget.db.TransactionsQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// TODO: Complete
@Suppress("unused", "ExpressionBodySyntax", "UnusedParameter")
internal class ChartDataLoader(private val queries: TransactionsQueries) {
  fun cashFlow(meta: CashFlowReportMeta): Flow<CashFlowData> {
    return flowOf()
  }
}
