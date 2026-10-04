package aktual.budget.db.dao

import aktual.budget.db.AgeOfMoneyTransactions
import aktual.budget.db.BudgetDatabase
import aktual.budget.db.CashFlowByMonth
import aktual.budget.db.CrossoverBalancesByMonth
import aktual.budget.db.CrossoverExpensesByMonth
import aktual.budget.db.CrossoverStartingBalances
import aktual.budget.db.ForecastDailyTotals
import aktual.budget.db.ForecastPostedScheduleTransactions
import aktual.budget.db.ForecastSchedules
import aktual.budget.db.ForecastStartingBalances
import aktual.budget.db.ForecastTrackingBudgetTotals
import aktual.budget.db.ForecastTransferPayees
import aktual.budget.db.MonteCarloAccountBalances
import aktual.budget.db.NetWorthByMonth
import aktual.budget.db.SankeyCategoryTotals
import aktual.budget.db.SankeyTransfers
import aktual.budget.db.TransactionDateBounds
import aktual.budget.model.AccountId
import alakazam.kotlin.CoroutineContexts
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth

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

  fun observeCrossoverStartingBalances(start: LocalDate): Flow<List<CrossoverStartingBalances>> =
    queries
      .crossoverStartingBalances(start)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeCrossoverBalancesByMonth(
    start: LocalDate,
    end: LocalDate,
  ): Flow<List<CrossoverBalancesByMonth>> =
    queries
      .crossoverBalancesByMonth(start, end)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeSankeyCategoryTotals(
    start: LocalDate,
    end: LocalDate,
  ): Flow<List<SankeyCategoryTotals>> =
    queries
      .sankeyCategoryTotals(start, end)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeSankeyTransfers(start: LocalDate, end: LocalDate): Flow<List<SankeyTransfers>> =
    queries.sankeyTransfers(start, end).asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observeForecastAccounts(): Flow<List<AccountId>> =
    queries.forecastAccounts().asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observeForecastStartingBalances(start: LocalDate): Flow<List<ForecastStartingBalances>> =
    queries
      .forecastStartingBalances(start)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeForecastDailyTotals(
    start: LocalDate,
    end: LocalDate,
  ): Flow<List<ForecastDailyTotals>> =
    queries
      .forecastDailyTotals(start, end)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeForecastSchedules(): Flow<List<ForecastSchedules>> =
    queries.forecastSchedules().asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observePostedScheduleTransactions(
    start: LocalDate
  ): Flow<List<ForecastPostedScheduleTransactions>> =
    queries
      .forecastPostedScheduleTransactions(start)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeForecastTransferPayees(): Flow<List<ForecastTransferPayees>> =
    queries.forecastTransferPayees().asFlow().mapToList(contexts.default).distinctUntilChanged()

  fun observeForecastOnBudgetBalance(): Flow<Long> =
    queries.forecastOnBudgetBalance().asFlow().mapToOne(contexts.default).distinctUntilChanged()

  fun observeForecastTrackingBudgetTotals(
    start: YearMonth,
    end: YearMonth,
  ): Flow<List<ForecastTrackingBudgetTotals>> =
    queries
      .forecastTrackingBudgetTotals(start, end)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()

  fun observeMonteCarloAccountBalances(
    accounts: Collection<AccountId>
  ): Flow<List<MonteCarloAccountBalances>> =
    queries
      .monteCarloAccountBalances(accounts)
      .asFlow()
      .mapToList(contexts.default)
      .distinctUntilChanged()
}
