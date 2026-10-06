package aktual.budget.reports.vm

import aktual.budget.db.CashFlowByMonth
import aktual.budget.db.ForecastPostedScheduleTransactions
import aktual.budget.db.ForecastSchedules
import aktual.budget.db.ForecastTrackingBudgetTotals
import aktual.budget.db.ForecastTransferPayees
import aktual.budget.db.SankeyCategoryTotals
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.PayeeId
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.WidgetType
import aktual.core.Calendar
import aktual.core.model.Percent
import alakazam.kotlin.CoroutineContexts
import dev.zacsweers.metro.Inject
import kotlin.math.roundToLong
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit.Companion.DAY
import kotlinx.datetime.DateTimeUnit.Companion.MONTH
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

@Inject
@OptIn(ExperimentalCoroutinesApi::class)
internal class ChartDataLoader(
  private val dao: ReportsDao,
  private val preferences: PreferencesDao,
  private val calendar: Calendar,
  private val contexts: CoroutineContexts,
) {
  fun load(meta: ReportMeta): Flow<ChartData> =
    when (meta) {
      is AgeOfMoneyReportMeta -> ageOfMoney(meta)
      is BalanceForecastReportMeta -> balanceForecast(meta)
      is CashFlowReportMeta -> cashFlow(meta)
      is CrossoverReportMeta -> crossover(meta)
      is MarkdownReportMeta -> text(meta)
      is MonteCarloReportMeta -> monteCarlo(meta)
      is NetWorthReportMeta -> netWorth(meta)
      is SankeyReportMeta -> sankey(meta)
      is BudgetAnalysisReportMeta,
      is CalendarReportMeta,
      is CustomReportMeta,
      is FormulaReportMeta,
      is SpendingReportMeta,
      is SummaryReportMeta -> unsupported(meta, ReportType)
      is UnsupportedReportMeta ->
        unsupported(meta, if (meta.type == Unknown) ReportType else InvalidMeta)
    }

  fun text(meta: MarkdownReportMeta): Flow<ChartData> {
    val align: TextAlign = meta.textAlign?.takeIf { it != Unknown } ?: Left
    return flowOf(TextData(meta.content, align))
  }

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

    val granularity = meta.granularity?.takeIf { it != Unknown } ?: Monthly
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

  // packages/desktop-client/src/components/reports/reports/SankeyCard.tsx
  fun sankey(meta: SankeyReportMeta): Flow<ChartData> {
    if (meta.conditions.hasFilters()) return unsupported(meta, Filters)
    if (meta.mode == Budgeted) return unsupported(meta, SankeyBudgeted)

    val params =
      SankeyParams(
        topN = meta.topNCategories ?: DEFAULT_TOP_N_CATEGORIES,
        sort = meta.categorySort ?: PerGroup,
        layerFrom = SankeyLayer.parse(meta.layerFrom) ?: IncomePayee,
        layerTo = SankeyLayer.parse(meta.layerTo) ?: Category,
        groupAccounts = meta.groupAccounts == true,
        showPercentages = meta.showPercentages == true,
      )
    val showTransfers = meta.showTransfers == true && !params.groupAccounts

    return dao.observeTransactionDateBounds().flatMapLatest { bounds ->
      val today = calendar.today()
      val range =
        resolveTimeRange(
          timeFrame = meta.timeFrame,
          default = null,
          today = today,
          latestTransaction = bounds.latest,
        )
      val earliest = bounds.earliest?.yearMonth ?: today.yearMonth
      val latest = bounds.latest?.yearMonth ?: today.yearMonth
      val start = range.start.coerceIn(earliest, latest)
      val end = range.endInclusive.coerceIn(earliest, latest).coerceAtLeast(start)

      val transfers =
        if (showTransfers) {
          dao.observeSankeyTransfers(start.firstDay, end.lastDay)
        } else {
          flowOf(emptyList())
        }

      combine(dao.observeSankeyCategoryTotals(start.firstDay, end.lastDay), transfers) {
        rows,
        transferRows ->
        calculateSankey(
          title = meta.name,
          start = start,
          end = end,
          entries = sankeyEntries(rows),
          transfers =
            aggregateTransferPairs(
              transferRows.mapNotNull { row ->
                SankeyTransfer(
                  id = row.id.value,
                  transferId = row.transfer_id?.value ?: return@mapNotNull null,
                  amount = row.amount,
                  accountId = row.account.value,
                  accountName = row.account_name.orEmpty(),
                )
              }
            ),
          categoryOrder = budgetOrder(rows),
          params = params,
        )
      }
    }
  }

  // Expense totals are split by account, income totals by account and payee
  private fun sankeyEntries(rows: List<SankeyCategoryTotals>): List<SankeyEntry> {
    val entries = LinkedHashMap<Triple<CategoryId, AccountId, PayeeId?>, SankeyEntry>()
    for (row in rows) {
      val isIncome = row.is_income == true
      val payee = row.payee.takeIf { isIncome }
      val key = Triple(row.category, row.account, payee)
      val existing = entries[key]
      entries[key] =
        existing?.copy(total = existing.total + row.total)
          ?: SankeyEntry(
            categoryGroupId = row.category_group.value,
            categoryGroup = row.group_name.orEmpty(),
            categoryId = row.category.value,
            category = row.category_name.orEmpty(),
            isIncome = isIncome,
            total = row.total,
            accountId = row.account.value,
            accountName = row.account_name.orEmpty(),
            payeeId = payee?.value,
            payeeName = row.payee_name.takeIf { isIncome },
          )
    }
    return entries.values.toList()
  }

  private fun budgetOrder(rows: List<SankeyCategoryTotals>): List<String> =
    rows
      .distinctBy { it.category }
      .sortedWith(compareBy({ it.group_sort_order }, { it.category_sort_order }))
      .groupBy { it.category_group }
      .flatMap { (group, categories) -> listOf(group.value) + categories.map { it.category.value } }

  // packages/desktop-client/src/components/reports/reports/BalanceForecastCard.tsx
  fun balanceForecast(meta: BalanceForecastReportMeta): Flow<ChartData> =
    preferences.observe(SyncedPrefKey.Global.BudgetType).flatMapLatest { budgetType ->
      val today = calendar.today()
      val range =
        resolveTimeRange(
          timeFrame = meta.timeFrame,
          default =
            TimeFrame(
              start = today.yearMonth,
              end = today.yearMonth.plus(DEFAULT_FORECAST_MONTHS - 1, MONTH),
              mode = Static,
            ),
          today = today,
          latestTransaction = null,
        )
      val isTracking = meta.source == TrackingBudget && BudgetType.from(budgetType) == Tracking
      val params =
        ForecastParams(
          title = meta.name,
          start = range.start,
          end = range.endInclusive,
          granularity = meta.granularity?.takeIf { it != Unknown && !isTracking } ?: Monthly,
          today = today,
        )

      when {
        isTracking -> trackingBudgetForecast(params)
        meta.conditions.hasFilters() -> unsupported(meta, Filters)
        else -> scheduleForecast(params, meta.accounts?.toSet())
      }
    }

  // packages/loot-core/src/server/forecast/app.ts generateForecast(). Without an account filter,
  // schedules that have no account are included too.
  private fun scheduleForecast(
    params: ForecastParams,
    accountFilter: Set<AccountId>?,
  ): Flow<ChartData> {
    val start = params.start.firstDay
    val end = params.end.lastDay
    val firstForecastDate = if (end < params.today) start else maxOf(start, params.today)

    val scheduleInputs =
      combine(
        dao.observeForecastAccounts(),
        dao.observeForecastSchedules(),
        dao.observePostedScheduleTransactions(firstForecastDate.minus(POSTED_LOOKBACK_DAYS, DAY)),
        dao.observeForecastTransferPayees(),
        ::ScheduleInputs,
      )

    return combine(
      scheduleInputs,
      dao.observeForecastStartingBalances(start),
      dao.observeForecastDailyTotals(start, end),
    ) { inputs, startingRows, dailyRows ->
      val live = inputs.accounts
      val selected = (accountFilter?.let { ids -> live.filter { it in ids } } ?: live).toSet()
      val accounts: Set<AccountId?> =
        when {
          selected.isEmpty() -> emptySet()
          accountFilter == null -> selected + null
          else -> selected
        }

      val postedDates =
        inputs.posted.filter { it.account in selected }.groupBy({ it.schedule }, { it.date })

      calculateBalanceForecast(
        params = params,
        accounts = accounts,
        startingBalances = startingRows.associate { it.account to it.total },
        dailyTotals =
          dailyRows
            .filter { it.account in selected }
            .groupingBy { it.date }
            .fold(0L) { total, row -> total + row.total },
        occurrences =
          buildScheduleOccurrences(
            schedules = inputs.schedules.mapNotNull(::forecastSchedule),
            end = end,
            transferAccounts = inputs.transferPayees.associate { it.id to it.transfer_acct },
            postedDates = postedDates,
          ),
      )
    }
  }

  // packages/loot-core/src/server/forecast/forecast-schedules.ts normalizeSchedule()
  private fun forecastSchedule(row: ForecastSchedules): ForecastSchedule? {
    val amount = parseScheduleAmount(row._amount) ?: return null
    val date = parseScheduleDate(row._date) ?: return null
    val dateCondition = row._conditions?.firstOrNull { it.field == Date }
    return ForecastSchedule(
      id = row.id,
      nextDate = row.next_date,
      date = date,
      account = row._account?.let(::AccountId),
      payee = row._payee,
      amount = amount,
      exactDate = dateCondition?.operator == Is || row.posts_transaction == true,
    )
  }

  private fun trackingBudgetForecast(params: ForecastParams): Flow<ChartData> =
    combine(
      dao.observeForecastOnBudgetBalance(),
      dao.observeForecastTrackingBudgetTotals(params.start, params.end),
    ) { balance, rows ->
      val (income, expenses) = rows.partition { it.is_income == true }
      fun List<ForecastTrackingBudgetTotals>.byMonth() = mapNotNull { row ->
        row.month?.let { it to row.total }
      }
        .toMap()
      calculateTrackingBudgetForecast(
        params = params,
        onBudgetBalance = balance,
        budgetedIncome = income.byMonth(),
        budgetedExpenses = expenses.byMonth(),
      )
    }

  // packages/desktop-client/src/components/reports/reports/monte-carlo/MonteCarloCard.tsx
  fun monteCarlo(meta: MonteCarloReportMeta): Flow<ChartData> {
    val config = meta.toConfig()
    val linked = config.pots.mapNotNull { it.accountId }.toSet()
    val balances =
      if (linked.isEmpty()) {
        flowOf(emptyMap())
      } else {
        dao.observeMonteCarloAccountBalances(linked).map { rows ->
          rows.associate { it.account to it.total }
        }
      }

    return balances
      .map { byAccount ->
        // A linked pot takes its account's live balance, falling back to the stored balance
        val result = runMonteCarlo(config.withLiveBalances(byAccount))
        MonteCarloData(
          title = meta.name,
          successRate = Percent((result.successRate * PERCENT_TENTHS).roundToLong() / TENTHS),
          currentAge = config.currentAge,
          targetAge = config.currentAge + result.horizonYears,
          bands =
            result.percentileBands
              .map { band ->
                MonteCarloBand(
                  age = config.currentAge + band.year,
                  p10 = Amount(band.p10),
                  p25 = Amount(band.p25),
                  p50 = Amount(band.p50),
                  p75 = Amount(band.p75),
                  p90 = Amount(band.p90),
                )
              }
              .toImmutableList(),
          medianEndingBalance = Amount(result.medianEndingBalance),
          medianDepletionAge = result.medianDepletionYear?.let { config.currentAge + it - 1 },
          simulationCount = result.simulationCount,
        )
      }
      .flowOn(contexts.default)
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
    val stackTrace = (meta as? UnsupportedReportMeta)?.stackTrace
    return flowOf(UnsupportedData(reason, type, name, stackTrace))
  }

  private fun List<Condition>?.hasFilters() = !isNullOrEmpty()

  private fun YearMonth.toLong(): Long = year * YEAR_MONTH_FACTOR + month.ordinal + 1L

  private inline fun List<CashFlowByMonth>.total(predicate: (CashFlowByMonth) -> Boolean): Amount =
    Amount(filter(predicate).sumOf { it.total })

  private companion object {
    const val YEAR_MONTH_FACTOR = 100L
    const val DEFAULT_SAFE_WITHDRAWAL_RATE = 0.04
    const val DEFAULT_FORECAST_MONTHS = 12
    const val PERCENT_TENTHS = 1000.0
    const val TENTHS = 10.0
  }
}

private data class ScheduleInputs(
  val accounts: List<AccountId>,
  val schedules: List<ForecastSchedules>,
  val posted: List<ForecastPostedScheduleTransactions>,
  val transferPayees: List<ForecastTransferPayees>,
)
