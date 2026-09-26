package aktual.budget.db.dao

import aktual.budget.db.AgeOfMoneyTransactions
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.CrossoverBalancesByMonth
import aktual.budget.db.CrossoverExpensesByMonth
import aktual.budget.db.NetWorthByMonth
import aktual.budget.db.TransactionDateBounds
import aktual.budget.db.reports.CashFlowByMonth
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.LocalDate

@Inject
class ReportsDao(database: BudgetDatabase, private val contexts: CoroutineContexts) {
  private val queries = database.reportsQueries

  fun observeTransactionDateBounds(): Flow<TransactionDateBounds> =
    queries.transactionDateBounds().asFlow().mapToOne(contexts.default).distinctUntilChanged()

  fun observeCashFlowStartingBalance(start: LocalDate): Flow<Long> =
    queries
      .cashFlowStartingBalance(start)
      .asFlow()
      .mapToOne(contexts.default)
      .distinctUntilChanged()

  fun observeCashFlowByMonth(start: LocalDate, end: LocalDate): Flow<List<CashFlowByMonth>> =
    queries.cashFlowByMonth(start, end).asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observeNetWorthStartingBalance(start: LocalDate): Flow<Long> =
    queries
      .netWorthStartingBalance(start)
      .asFlow()
      .mapToOne(contexts.default)
      .distinctUntilChanged()

  fun observeNetWorthByMonth(start: LocalDate, end: LocalDate): Flow<List<NetWorthByMonth>> =
    queries.netWorthByMonth(start, end).asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observeAgeOfMoneyTransactions(end: LocalDate): Flow<List<AgeOfMoneyTransactions>> =
    queries.ageOfMoneyTransactions(end).asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observeCrossoverExpensesByMonth(
    start: LocalDate,
    end: LocalDate,
  ): Flow<List<CrossoverExpensesByMonth>> =
    queries
      .crossoverExpensesByMonth(start, end)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeCrossoverBalancesByMonth(end: LocalDate): Flow<List<CrossoverBalancesByMonth>> =
    queries
      .crossoverBalancesByMonth(end)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()
}
