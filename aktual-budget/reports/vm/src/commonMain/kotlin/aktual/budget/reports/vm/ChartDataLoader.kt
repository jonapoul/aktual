package aktual.budget.reports.vm

import aktual.budget.db.dao.ReportsDao
import aktual.budget.db.reports.CashFlowByMonth
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.WidgetType
import aktual.core.Calendar
import dev.zacsweers.metro.Inject
import kotlin.math.roundToLong
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit.Companion.MONTH
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange
import kotlinx.datetime.minus
import kotlinx.datetime.minusMonth
import kotlinx.datetime.yearMonth
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Inject
@OptIn(ExperimentalCoroutinesApi::class)
internal class ChartDataLoader(private val dao: ReportsDao, private val calendar: Calendar) {
  fun text(meta: MarkdownReportMeta): Flow<ChartData> = flowOf(TextData(meta.content))

  // packages/desktop-client/src/components/reports/spreadsheets/cash-flow-spreadsheet.tsx
  // cashFlowByDate()
  fun cashFlow(meta: CashFlowReportMeta): Flow<ChartData> {
    if (meta.conditions.hasFilters()) return unsupported(meta, Filters)

    return dao.observeTransactionDateBounds().flatMapLatest { bounds ->
      val today = calendar.today()
      val range =
        resolveTimeRange(
          timeFrame = meta.timeFrame,
          default = TimeFrame(start = today.yearMonth, end = today.yearMonth, mode = SlidingWindow),
          today = today,
          latestTransaction = bounds.latest,
        )
      val start = range.start
      val end = minOf(range.endInclusive, today.yearMonth)

      combine(
        dao.observeCashFlowStartingBalance(start.firstDay),
        dao.observeCashFlowByMonth(start.firstDay, end.lastDay),
      ) { startingBalance, rows ->
        val byMonth = rows.groupBy { it.month }
        var balance = Amount(startingBalance)
        val items =
          (start..end).associateWith { month ->
            val monthRows = byMonth[month.toLong()].orEmpty()
            val income = monthRows.total { !it.is_transfer && it.is_income }
            val expenses = monthRows.total { !it.is_transfer && !it.is_income }
            val transfers = monthRows.total { it.is_transfer }
            balance += income + expenses + transfers
            CashFlowDatum(income, expenses, transfers, balance)
          }
        CashFlowData(title = meta.name, items = items.toImmutableMap())
      }
    }
  }

  // packages/desktop-client/src/components/reports/spreadsheets/net-worth-spreadsheet.ts
  // createSpreadsheet()
  fun netWorth(meta: NetWorthReportMeta): Flow<ChartData> {
    if (meta.conditions.hasFilters()) return unsupported(meta, Filters)

    return dao.observeTransactionDateBounds().flatMapLatest { bounds ->
      val today = calendar.today()
      val range =
        resolveTimeRange(
          timeFrame = meta.timeFrame,
          default = null,
          today = today,
          latestTransaction = bounds.latest,
        )

      // Go back one month to show the change into the first month, unless there's no data before it
      val earliest = bounds.earliest
      val start =
        if (earliest != null && earliest >= range.start.firstDay) {
          range.start
        } else {
          range.start.minusMonth()
        }
      val end = range.endInclusive

      combine(
        dao.observeNetWorthStartingBalance(start.firstDay),
        dao.observeNetWorthByMonth(start.firstDay, end.lastDay),
      ) { startingBalance, rows ->
        val byMonth = rows.associate { it.month to it.total }
        var balance = Amount(startingBalance)
        val items =
          (start..end).associateWith { month ->
            balance += Amount(byMonth[month.toLong()] ?: 0L)
            balance
          }
        NetWorthData(title = meta.name, items = items.toImmutableMap())
      }
    }
  }

  // packages/desktop-client/src/components/reports/spreadsheets/age-of-money-spreadsheet.ts
  // createAgeOfMoneySpreadsheet()
  fun ageOfMoney(meta: AgeOfMoneyReportMeta): Flow<ChartData> {
    if (meta.conditions.hasFilters()) return unsupported(meta, Filters)

    val granularity = meta.granularity?.takeIf { it != Unknown } ?: AgeOfMoneyGranularity.Monthly
    return dao.observeTransactionDateBounds().flatMapLatest { bounds ->
      val today = calendar.today()
      val range =
        resolveTimeRange(
          timeFrame = meta.timeFrame,
          default = null,
          today = today,
          latestTransaction = bounds.latest,
        )
      val start = range.start
      val end = range.endInclusive

      // FIFO needs all the history, not just the displayed range
      dao.observeAgeOfMoneyTransactions(minOf(end.lastDay, today)).map { rows ->
        val transactions = rows.map { AgeOfMoneyTransaction(it.date, it.amount) }
        val (ages, insufficientData) = calculateAges(transactions)
        val displayed = ages.filter { it.date >= start.firstDay }
        val items = calculateGraphData(displayed, start, end, granularity, today)
        AgeOfMoneyData(
          title = meta.name,
          start = start,
          end = end,
          granularity = granularity,
          items = items,
          currentAge = averageAge(displayed.map { it.age }),
          trend = calculateTrend(items.values.toList()),
          insufficientData = insufficientData,
        )
      }
    }
  }

  // packages/desktop-client/src/components/reports/reports/CrossoverCard.tsx
  fun crossover(meta: CrossoverReportMeta): Flow<ChartData> =
    dao.observeTransactionDateBounds().flatMapLatest { bounds ->
      val today = calendar.today()
      val range = crossoverRange(meta.timeFrame, today, bounds.earliest)
      val months = (range.start..range.endInclusive).toList()
      val categoryIds = meta.expenseCategoryIds?.toSet()
      val accountIds = meta.incomeAccountIds?.toSet()
      val showHidden = meta.showHiddenCategories == true
      val params =
        CrossoverParams(
          safeWithdrawalRate = meta.safeWithdrawalRate ?: DEFAULT_SAFE_WITHDRAWAL_RATE,
          estimatedReturn = meta.estimatedReturn,
          expectedContribution = meta.expectedContribution?.roundToLong(),
          projectionType = meta.projectionType ?: Hampel,
          expenseAdjustmentFactor = meta.expenseAdjustmentFactor ?: 1.0,
        )

      if (accountIds?.isEmpty() == true) {
        return@flatMapLatest flowOf(CrossoverData(meta.name, persistentMapOf(), null, null))
      }

      combine(
        dao.observeCrossoverExpensesByMonth(range.start.firstDay, range.endInclusive.lastDay),
        dao.observeCrossoverStartingBalances(range.start.firstDay),
        dao.observeCrossoverBalancesByMonth(range.start.firstDay, range.endInclusive.lastDay),
      ) { expenseRows, startingRows, balanceRows ->
        val expensesByMonth =
          expenseRows
            .filter { row ->
              val included = categoryIds?.contains(row.category) ?: (row.is_income != true)
              included && (showHidden || !row.hidden)
            }
            .groupingBy { it.month }
            .fold(0L) { total, row -> total - row.total }

        val changesByMonth =
          balanceRows
            .filter { row -> accountIds?.contains(row.account) ?: (row.tombstone != true) }
            .groupingBy { it.month }
            .fold(0L) { total, row -> total + row.total }

        var balance =
          startingRows
            .filter { row -> accountIds?.contains(row.account) ?: (row.tombstone != true) }
            .sumOf { it.total }
        val balances = months.map { month ->
          balance += changesByMonth[month.toLong()] ?: 0L
          balance
        }

        calculateCrossover(
          title = meta.name,
          months = months,
          expenses = months.map { expensesByMonth[it.toLong()] ?: 0L },
          balances = balances,
          params = params,
          today = today,
        )
      }
    }

  // Only whole months up to last month are used, clamped to the months with data
  private fun crossoverRange(
    timeFrame: TimeFrame?,
    today: LocalDate,
    earliest: LocalDate?,
  ): YearMonthRange {
    val latestMonth = today.yearMonth.minusMonth()
    val earliestMonth = minOf(earliest?.yearMonth ?: latestMonth, latestMonth)
    val default =
      TimeFrame(
        start = today.yearMonth.minus(DEFAULT_CROSSOVER_MONTHS, MONTH),
        end = latestMonth,
        mode = Full,
      )
    val range = resolveTimeRange(timeFrame, default, today, latestMonth.firstDay)
    fun YearMonth.clamp() = coerceIn(earliestMonth, latestMonth)

    val (start, end) =
      when (timeFrame?.mode ?: Full) {
        Full -> earliestMonth to latestMonth
        SlidingWindow,
        Unknown -> range.start.minusMonth().clamp() to range.endInclusive.minusMonth().clamp()
        LastMonth,
        LastYear,
        YearToDate,
        PriorYearToDate,
        CurrentQuarter,
        PreviousQuarter,
        Static -> range.start.clamp() to range.endInclusive.clamp()
      }
    return start..maxOf(start, end)
  }

  fun unsupported(meta: ReportMeta, reason: UnsupportedReason): Flow<ChartData> {
    val (type, name) =
      when (meta) {
        is AgeOfMoneyReportMeta -> WidgetType.AgeOfMoney to meta.name
        is BalanceForecastReportMeta -> WidgetType.BalanceForecast to meta.name
        is BudgetAnalysisReportMeta -> WidgetType.BudgetAnalysis to meta.name
        is CalendarReportMeta -> WidgetType.Calendar to meta.name
        is CashFlowReportMeta -> WidgetType.CashFlow to meta.name
        is CrossoverReportMeta -> WidgetType.Crossover to meta.name
        is CustomReportMeta -> WidgetType.Custom to null
        is FormulaReportMeta -> WidgetType.Formula to meta.name
        is MarkdownReportMeta -> WidgetType.Markdown to null
        is MonteCarloReportMeta -> WidgetType.MonteCarlo to meta.name
        is NetWorthReportMeta -> WidgetType.NetWorth to meta.name
        is SankeyReportMeta -> WidgetType.Sankey to meta.name
        is SpendingReportMeta -> WidgetType.Spending to meta.name
        is SummaryReportMeta -> WidgetType.Summary to meta.name
        is UnsupportedReportMeta -> meta.type to (meta.raw["name"] as? JsonPrimitive)?.contentOrNull
      }
    return flowOf(UnsupportedData(reason, type, name))
  }

  private fun List<Condition>?.hasFilters() = !isNullOrEmpty()

  private fun YearMonth.toLong(): Long = year * YEAR_MONTH_FACTOR + month.ordinal + 1L

  private inline fun List<CashFlowByMonth>.total(predicate: (CashFlowByMonth) -> Boolean): Amount =
    Amount(filter(predicate).sumOf { it.total })

  private companion object {
    const val YEAR_MONTH_FACTOR = 100L
    const val DEFAULT_SAFE_WITHDRAWAL_RATE = 0.04
    const val DEFAULT_CROSSOVER_MONTHS = 120
  }
}
