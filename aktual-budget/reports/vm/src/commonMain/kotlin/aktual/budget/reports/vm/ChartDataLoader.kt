package aktual.budget.reports.vm

import aktual.budget.budgeting.domain.BudgetMonthCalculator
import aktual.budget.db.CashFlowByMonth
import aktual.budget.db.CustomReportAmounts
import aktual.budget.db.CustomReportTransactions
import aktual.budget.db.ForecastAccounts
import aktual.budget.db.ForecastPayees
import aktual.budget.db.ForecastPostedScheduleTransactions
import aktual.budget.db.ForecastSchedules
import aktual.budget.db.ForecastTrackingBudgetTotals
import aktual.budget.db.SankeyCategoryTotals
import aktual.budget.db.SankeyTransfers
import aktual.budget.db.dao.CustomReportsDao
import aktual.budget.db.dao.FormulaDao
import aktual.budget.db.dao.PreferencesDao
import aktual.budget.db.dao.ReportsDao
import aktual.budget.formula.ArrayValue
import aktual.budget.formula.BooleanValue
import aktual.budget.formula.EmptyValue
import aktual.budget.formula.ErrorReason.InternalError
import aktual.budget.formula.ErrorValue
import aktual.budget.formula.FormulaEvaluator
import aktual.budget.formula.FormulaFormat
import aktual.budget.formula.FormulaValue
import aktual.budget.formula.NumberValue
import aktual.budget.formula.TextValue
import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.BudgetType
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.ConditionSet
import aktual.budget.model.CurrencyConfig
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.NumberFormatConfig
import aktual.budget.model.PayeeId
import aktual.budget.model.SyncedPrefKey
import aktual.budget.model.WidgetType
import aktual.budget.model.parseScheduleAmount
import aktual.budget.rules.domain.TransactionFilter
import aktual.core.Calendar
import aktual.core.model.Percent
import aktual.prefs.CurrencyPreferences
import aktual.prefs.FormatPreferences
import alakazam.kotlin.CoroutineContexts
import dev.zacsweers.metro.Inject
import kotlin.math.roundToLong
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plus
import kotlinx.datetime.plusMonth
import kotlinx.datetime.yearMonth
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import logcat.logcat

@Inject
@Suppress("LargeClass")
@OptIn(ExperimentalCoroutinesApi::class)
internal class ChartDataLoader(
  private val dao: ReportsDao,
  private val customReports: CustomReportsDao,
  private val preferences: PreferencesDao,
  private val formulaDao: FormulaDao,
  private val budgets: BudgetMonthCalculator,
  private val formatPreferences: FormatPreferences,
  private val currencyPreferences: CurrencyPreferences,
  private val calendar: Calendar,
  private val contexts: CoroutineContexts,
) {
  fun load(meta: ReportMeta): Flow<ChartData> =
    when (meta) {
      is AgeOfMoneyReportMeta -> ageOfMoney(meta)
      is BalanceForecastReportMeta -> balanceForecast(meta)
      is BudgetAnalysisReportMeta -> budgetAnalysis(meta)
      is CashFlowReportMeta -> cashFlow(meta)
      is CrossoverReportMeta -> crossover(meta)
      is FormulaReportMeta -> formula(meta)
      is CustomReportMeta -> custom(meta)
      is MarkdownReportMeta -> text(meta)
      is MonteCarloReportMeta -> monteCarlo(meta)
      is NetWorthReportMeta -> netWorth(meta)
      is SankeyReportMeta -> sankey(meta)
      is SummaryReportMeta -> summary(meta)
      is CalendarReportMeta -> calendar(meta)
      is SpendingReportMeta -> spending(meta)
      is UnsupportedReportMeta ->
        unsupported(meta, if (meta.type == Unknown) ReportType else InvalidMeta)
    }

  // packages/desktop-client/src/components/reports/reports/GetCardData.tsx
  fun custom(meta: CustomReportMeta): Flow<ChartData> =
    customReports.observe(meta.id).flatMapLatest { report ->
      val config = report?.toConfig()
      when {
        report == null || report.tombstone == true -> flowOf(unsupportedData(meta, MissingReport))
        config == null -> flowOf(UnsupportedData(InvalidMeta, Custom, report.name))
        else -> custom(report.name.orEmpty(), config)
      }
    }

  // Draws [config] whether or not it's saved, so the editor can preview its draft
  fun custom(title: String, config: CustomReportConfig): Flow<ChartData> {
    if (config.balanceType == Budgeted && !config.canShowBudgeted()) {
      return flowOf(UnsupportedData(CustomBudgeted, Custom, title.ifEmpty { null }))
    }
    return combine(
        dao.observeTransactionDateBounds(),
        preferences.observe(SyncedPrefKey.Global.FirstDayOfWeekIdx),
      ) { bounds, firstDayOfWeek ->
        val today = calendar.today()
        // Upstream treats an unset preference as Sunday
        val firstDay = FirstDayOfWeek.from(firstDayOfWeek) ?: Sunday
        val range =
          resolveCustomReportRange(
            dates = config.dates,
            today = today,
            earliestTransaction = bounds.earliest ?: today,
            latestTransaction = bounds.latest ?: today,
            firstDayOfWeek = firstDay,
          )
        customData(title, config, range, firstDay)
      }
      .flatMapLatest { it }
  }

  private fun customData(
    title: String,
    config: CustomReportConfig,
    range: LocalDateRange,
    firstDayOfWeek: FirstDayOfWeek,
  ): Flow<ChartData> {
    val query = customReportQuery(config.interval, range)
    // Only the lists the report groups by. Categories are always needed, for categoryGroups
    val payees = if (config.groupBy == Payee) customReports.observePayees() else flowOf(emptyList())
    val accounts =
      if (config.groupBy == Account) customReports.observeAccounts() else flowOf(emptyList())

    val filter = TransactionFilter.compile(config.conditions, config.conditionsOp)
    val isTagSplit = config.groupBy == Tag && config.balanceType != Budgeted
    val amounts =
      when {
        config.balanceType == Budgeted -> observeBudgetAmounts(config, range)
        // Split by tag, the transactions are summed with their notes instead
        isTagSplit -> flowOf(emptyList())
        filter.isEmpty -> customReports.observeAmounts(query.start, query.end, query.divisor)
        else ->
          customReports.observeTransactions(query.start, query.end, query.divisor).map { rows ->
            filteredAmounts(rows, filter)
          }
      }
    val tagged =
      if (isTagSplit) {
        combine(
          customReports.observeTransactions(query.start, query.end, query.divisor),
          customReports.observeTags(),
        ) { rows, tags ->
          taggedAmounts(rows, filter) to tags
        }
      } else {
        flowOf(emptyList<TaggedAmounts>() to emptyList())
      }

    return combine(
        amounts,
        customReports.observeCategories(),
        customReports.observeCategoryGroups(),
        payees,
        accounts,
        ::CustomReportInputs,
      )
      .combine(tagged) { inputs, (rows, tags) -> inputs.copy(tagged = rows, tags = tags) }
      .map { inputs ->
        val sheet = customSpreadsheet(config, range, firstDayOfWeek, inputs)
        CustomData(
          title = title,
          config = config,
          range = range,
          intervals = sheet.intervals,
          intervalCount = sheet.intervalCount,
          groups = sheet.groups,
          categoryGroups = sheet.categoryGroups,
          totals = sheet.totals,
          trends = sheet.trends,
          hiddenCategories =
            inputs.categories
              .filter { it.hidden || it.group_hidden == true }
              .map { it.id }
              .toImmutableSet(),
          tagScope = config.tagScope.resolve(inputs.tags).map { it.tag }.toImmutableList(),
        )
      }
      .flowOn(contexts.default)
  }

  // packages/desktop-client/src/components/reports/spreadsheets/fetchSpreadsheetQueryData.ts:
  // budgeted reports read every month of the report's dates from the budget, not transactions
  private fun observeBudgetAmounts(
    config: CustomReportConfig,
    range: LocalDateRange,
  ): Flow<List<CustomReportAmounts>> =
    preferences
      .observe(SyncedPrefKey.Global.BudgetType)
      .flatMapLatest { budgetType ->
        customReports.observeBudgets(
          start = range.start.yearMonth,
          end = range.endInclusive.yearMonth,
          divisor = if (config.interval == Yearly) YEAR_MONTH_FACTOR else 1L,
          isTracking = BudgetType.from(budgetType) == Tracking,
        )
      }
      .map { rows -> budgetAmounts(rows, config.conditions, config.conditionsOp) }

  // packages/desktop-client/src/components/reports/reports/FormulaCard.tsx
  fun formula(meta: FormulaReportMeta): Flow<ChartData> =
    combine(formulaDao.observeChanges(), formulaFormats()) { _, format -> format }
      .mapLatest { format -> evaluateFormulaSafely(meta, format) }
      .flowOn(contexts.default)

  private fun formulaFormats(): Flow<FormulaFormat> =
    combine(
      formatPreferences.numberFormat.asFlow(),
      formatPreferences.hideFraction.asFlow(),
      currencyPreferences.currency.asFlow(),
      currencyPreferences.symbolPosition.asFlow(),
      currencyPreferences.spaceBetweenAmountAndSymbol.asFlow(),
    ) { numberFormat, hideFraction, currency, position, includeSpace ->
      formulaFormat(
        numberFormat = NumberFormatConfig(numberFormat, hideFraction),
        currency = CurrencyConfig(currency, position, includeSpace),
      )
    }

  // A bug in the evaluator should show on the card, not crash the app
  @Suppress("TooGenericExceptionCaught")
  private suspend fun evaluateFormulaSafely(
    meta: FormulaReportMeta,
    format: FormulaFormat,
  ): FormulaData =
    try {
      evaluateFormula(meta, format)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Throwable) {
      logcat.e { "Failed evaluating formula ${meta.formula}: ${e.stackTraceToString()}" }
      val cause = generateSequence(e) { it.cause }.last()
      val subject = "${cause::class.simpleName}: ${cause.message}"
      FormulaData(
        title = meta.name,
        showTitle = meta.showTitle != false,
        output = FormulaOutput.Failure(ErrorValue(Generic, InternalError, subject)),
        color = null,
        fontSizeMode = meta.fontSizeMode?.takeIf { it != Unknown } ?: Dynamic,
        staticFontSize = meta.staticFontSize?.takeIf { it > 0 } ?: DEFAULT_FONT_SIZE,
        warnings = persistentListOf(),
      )
    }

  private suspend fun evaluateFormula(meta: FormulaReportMeta, format: FormulaFormat): FormulaData {
    val evaluator =
      FormulaEvaluator(
        calendar,
        FormulaQueries(meta.queries, formulaDao, budgets, calendar),
        format,
      )
    val result = evaluator.evaluate(meta.formula.orEmpty().ifEmpty { DEFAULT_FORMULA })

    val colorFormula = meta.colorFormula.orEmpty()
    val color =
      if (colorFormula.isEmpty()) {
        null
      } else {
        val variables = FORMULA_THEME_VARIABLES + (RESULT_VARIABLE to result.value.asVariable())
        (evaluator.evaluate(colorFormula, variables).value as? TextValue)?.value?.takeIf {
          it.isNotEmpty()
        }
      }

    return FormulaData(
      title = meta.name,
      showTitle = meta.showTitle != false,
      output = result.value.toOutput(),
      color = color,
      fontSizeMode = meta.fontSizeMode?.takeIf { it != Unknown } ?: Dynamic,
      staticFontSize = meta.staticFontSize?.takeIf { it > 0 } ?: DEFAULT_FONT_SIZE,
      warnings = result.warnings.toImmutableList(),
    )
  }

  private fun FormulaValue.asVariable(): FormulaValue =
    when (this) {
      is NumberValue,
      is TextValue -> this
      is BooleanValue -> TextValue(value.toString())
      is ErrorValue,
      EmptyValue,
      is ArrayValue -> NumberValue(0.0)
    }

  private fun FormulaValue.toOutput(): FormulaOutput =
    when (this) {
      is NumberValue -> FormulaOutput.Money(Amount(value))
      is TextValue -> FormulaOutput.Text(value)
      is BooleanValue -> FormulaOutput.Text(value.toString())
      is ErrorValue -> FormulaOutput.Failure(this)
      EmptyValue -> FormulaOutput.Text("")
      is ArrayValue -> topLeft.toOutput()
    }

  fun text(meta: MarkdownReportMeta): Flow<ChartData> {
    val align: TextAlign = meta.textAlign?.takeIf { it != Unknown } ?: Left
    return flowOf(TextData(meta.content, align))
  }

  // packages/desktop-client/src/components/reports/reports/BudgetAnalysisCard.tsx
  fun budgetAnalysis(meta: BudgetAnalysisReportMeta): Flow<ChartData> {
    val range =
      resolveTimeRange(
        timeFrame = meta.timeFrame,
        default = null,
        today = calendar.today(),
        latestTransaction = null,
      )
    val params =
      BudgetAnalysisParams(
        title = meta.name,
        conditions = meta.conditions.orEmpty(),
        conditionsOp = meta.conditionsOp ?: And,
        showHiddenCategories = meta.showHiddenCategories == true,
        graphType = meta.graphType?.takeIf { it != Unknown } ?: Bar,
        showBalance = meta.showBalance != false,
        balanceOnly = meta.balanceOnly == true,
      )
    return budgets
      .observeRange(range.start.minusMonth()..range.endInclusive)
      .map { months -> calculateBudgetAnalysis(months, params) }
      .flowOn(contexts.default)
  }

  // packages/desktop-client/src/components/reports/spreadsheets/cash-flow-spreadsheet.tsx
  // cashFlowByDate()
  fun cashFlow(meta: CashFlowReportMeta): Flow<ChartData> {
    val filter = reportFilter(meta.conditions, meta.conditionsOp)
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

      cashFlowInputs(filter, start.firstDay, end.lastDay).map { (startingBalance, rows) ->
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
        CashFlowData(
          title = meta.name,
          items = items.toImmutableMap(),
          showBalance = meta.showBalance != false,
        )
      }
    }
  }

  // Totalled in SQL without conditions. With them, the matching transactions are totalled here
  private fun cashFlowInputs(
    filter: TransactionFilter,
    start: LocalDate,
    end: LocalDate,
  ): Flow<Pair<Long, List<CashFlowByMonth>>> =
    if (filter.isEmpty) {
      combine(
        dao.observeCashFlowStartingBalance(start),
        dao.observeCashFlowByMonth(start, end),
        ::Pair,
      )
    } else {
      dao
        .observeFilteredTransactions(filter, ALL_TIME_START, end)
        .map { rows -> cashFlowTotals(rows, start) }
        .flowOn(contexts.default)
    }

  // packages/desktop-client/src/components/reports/spreadsheets/summary-spreadsheet.ts
  // summarySpreadsheet()
  fun summary(meta: SummaryReportMeta): Flow<ChartData> {
    val content = parseSummaryContent(meta.content)
    val filter = TransactionFilter.compile(meta.conditions.orEmpty(), meta.conditionsOp ?: And)
    return dao
      .observeTransactionDateBounds()
      .flatMapLatest { bounds ->
        val today = calendar.today()
        val range =
          resolveTimeRange(
            timeFrame = meta.timeFrame,
            default = null,
            today = today,
            latestTransaction = bounds.latest,
          )
        val params = SummaryParams(meta.name.orEmpty(), range.start, range.endInclusive, content)
        val start = range.start.firstDay
        val end = range.endInclusive.lastDay
        val amounts = customReports.observeTransactions(start, end, divisor = 1L)
        val divisorAmounts =
          if (content.type == Percentage) {
            val divisorFilter =
              TransactionFilter.compile(content.divisorConditions.orEmpty(), content.divisorOp)
            val allTime = content.divisorAllTimeDateRange == true
            customReports
              .observeTransactions(
                start = if (allTime) ALL_TIME_START else start,
                end = if (allTime) ALL_TIME_END else end,
                divisor = 1L,
              )
              .map { rows -> rows.amountsMatching(divisorFilter) }
          } else {
            flowOf(emptyList())
          }
        combine(amounts, divisorAmounts) { rows, divisor ->
          calculateSummary(params, rows.amountsMatching(filter), divisor)
        }
      }
      .flowOn(contexts.default)
  }

  // packages/desktop-client/src/components/reports/spreadsheets/calendar-spreadsheet.ts
  // calendarSpreadsheet()
  fun calendar(meta: CalendarReportMeta): Flow<ChartData> {
    val conditions = ConditionSet(meta.conditions.orEmpty(), meta.conditionsOp ?: And)
    val filter = TransactionFilter.compile(conditions.conditions, conditions.op)
    return combine(dao.observeTransactionDateBounds(), formatPreferences.firstDayOfWeek.asFlow()) {
        bounds,
        firstDayOfWeek ->
        bounds to firstDayOfWeek
      }
      .flatMapLatest { (bounds, firstDayOfWeek) ->
        val today = calendar.today()
        val range =
          resolveTimeRange(
            timeFrame = meta.timeFrame,
            default = TimeFrame(start = today.yearMonth, end = today.yearMonth, mode = Full),
            today = today,
            latestTransaction = bounds.latest,
          )
        customReports
          .observeTransactions(range.start.firstDay, range.endInclusive.lastDay, divisor = 1L)
          .map { rows ->
            val transactions =
              rows
                .filter { filter.matches(it.toFilterRow()) }
                .map { CalendarTransaction(it.date, it.amount) }
            calculateCalendar(meta.name.orEmpty(), range, transactions, firstDayOfWeek, conditions)
          }
      }
      .flowOn(contexts.default)
  }

  // packages/desktop-client/src/components/reports/reports/SpendingCard.tsx
  fun spending(meta: SpendingReportMeta): Flow<ChartData> {
    val filter = reportFilter(meta.conditions, meta.conditionsOp)
    val mode = meta.spendingMode
    return dao
      .observeTransactionDateBounds()
      .flatMapLatest { bounds ->
        val today = calendar.today()
        val months = meta.resolveMonths(today)
        val averageMonths =
          if (mode == Average) {
            meta.averageRange.resolve(months.compare, bounds.earliest?.yearMonth)
          } else {
            emptyList()
          }
        val charted = buildSet {
          add(months.compare)
          when (mode) {
            Budget -> Unit
            Average -> addAll(averageMonths)
            SingleMonth,
            Unknown -> add(months.compareTo)
          }
        }
        val budget = if (mode == Budget) spendingBudget(meta, months.compare) else flowOf(0L)
        val params =
          SpendingParams(
            title = meta.name.orEmpty(),
            dateRangeMode = meta.dateRangeMode,
            months = months,
            mode = mode,
            averageMonths = averageMonths,
            today = today,
          )
        combine(spendingTotals(filter, charted), budget) { totals, budgeted ->
          calculateSpending(params, totals, budgeted)
        }
      }
      .flowOn(contexts.default)
  }

  // One query per run of consecutive months, so a comparison a year back doesn't read the months
  // between
  private fun spendingTotals(
    filter: TransactionFilter,
    months: Set<YearMonth>,
  ): Flow<Map<LocalDate, Long>> {
    val runs = mutableListOf<MutableList<YearMonth>>()
    for (month in months.sorted()) {
      val run = runs.lastOrNull()
      if (run != null && run.last().plusMonth() == month) run += month else runs += mutableListOf(month)
    }
    val flows = runs.map { run ->
      dao.observeFilteredTransactions(filter, run.first().firstDay, run.last().lastDay)
    }
    return combine(flows) { rows -> spendingTotals(rows.toList().flatten()) }
  }

  // spending-spreadsheet.ts getSpendingBudgetFilters(): only category and category group
  // conditions narrow the budgets
  private fun spendingBudget(meta: SpendingReportMeta, month: YearMonth): Flow<Long> {
    val filter = BudgetCategoryFilter(meta.conditions.orEmpty(), meta.conditionsOp ?: And)
    return preferences
      .observe(SyncedPrefKey.Global.BudgetType)
      .flatMapLatest { budgetType ->
        customReports.observeBudgets(
          start = month,
          end = month,
          divisor = 1L,
          isTracking = BudgetType.from(budgetType) == Tracking,
        )
      }
      .map { rows ->
        rows
          .filter { row ->
            filter.matches(row.category, row.category_name, row.category_group, row.category_group_name)
          }
          .sumOf { it.amount ?: 0L }
      }
  }

  private fun List<CustomReportTransactions>.amountsMatching(filter: TransactionFilter) = filter {
    filter.matches(it.toFilterRow())
  }
    .map { it.amount }

  // packages/desktop-client/src/components/reports/spreadsheets/net-worth-spreadsheet.ts
  // createSpreadsheet()
  fun netWorth(meta: NetWorthReportMeta): Flow<ChartData> {
    val filter = reportFilter(meta.conditions, meta.conditionsOp)
    val interval = meta.interval?.takeIf { it != Unknown } ?: Monthly
    return combine(
        dao.observeTransactionDateBounds(),
        preferences.observe(SyncedPrefKey.Global.FirstDayOfWeekIdx),
        ::Pair,
      )
      .flatMapLatest { (bounds, firstDayOfWeek) ->
        val today = calendar.today()
        val range =
          resolveTimeRange(
            timeFrame = meta.timeFrame,
            default = null,
            today = today,
            latestTransaction = bounds.latest,
          )
        val periods =
          NetWorthPeriods.of(
            interval = interval,
            range = range,
            earliest = bounds.earliest,
            today = today,
            // Upstream treats an unset preference as Sunday
            firstDayOfWeek = FirstDayOfWeek.from(firstDayOfWeek) ?: Sunday,
          )
        netWorthInputs(filter, periods).map { rows ->
          netWorthData(
            title = meta.name,
            mode = meta.mode,
            interval = interval,
            periods = periods.keys(),
            rows = rows,
          )
        }
      }
      .flowOn(contexts.default)
  }

  // Monthly totals come from SQL without conditions. Otherwise the transactions are grouped here
  private fun netWorthInputs(
    filter: TransactionFilter,
    periods: NetWorthPeriods,
  ): Flow<List<NetWorthRow>> =
    if (filter.isEmpty && periods.interval == Monthly) {
      dao.observeNetWorthByAccount(periods.start, periods.end).map { rows ->
        rows.map { row ->
          NetWorthRow(
            account = row.account,
            name = row.name,
            period = row.month.takeIf { it != 0L }?.toYearMonth()?.firstDay,
            total = row.total,
          )
        }
      }
    } else {
      dao.observeFilteredTransactions(filter, ALL_TIME_START, periods.end).map { rows ->
        netWorthRows(rows, periods)
      }
    }

  // packages/desktop-client/src/components/reports/spreadsheets/age-of-money-spreadsheet.ts
  // createAgeOfMoneySpreadsheet()
  fun ageOfMoney(meta: AgeOfMoneyReportMeta): Flow<ChartData> {
    val filter = reportFilter(meta.conditions, meta.conditionsOp)
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
      ageOfMoneyInputs(meta, filter, minOf(end.lastDay, today)).map { transactions ->
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

  private fun ageOfMoneyInputs(
    meta: AgeOfMoneyReportMeta,
    filter: TransactionFilter,
    end: LocalDate,
  ): Flow<List<AgeOfMoneyTransaction>> =
    if (filter.isEmpty) {
      dao.observeAgeOfMoneyTransactions(end).map { rows ->
        rows.map { AgeOfMoneyTransaction(it.date, it.amount) }
      }
    } else {
      dao
        .observeFilteredTransactions(filter, ALL_TIME_START, end)
        .map { rows -> ageOfMoneyTransactions(rows, meta.conditions, meta.conditionsOp) }
        .flowOn(contexts.default)
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
    val layers =
      normalizeSankeyLayers(
        mode = meta.mode,
        from = SankeyLayer.parse(meta.layerFrom),
        to = SankeyLayer.parse(meta.layerTo),
      )
    val params =
      SankeyParams(
        topN = meta.topNCategories ?: DEFAULT_TOP_N_CATEGORIES,
        sort = meta.categorySort ?: PerGroup,
        layerFrom = layers.from,
        layerTo = layers.to,
        groupAccounts = meta.groupAccounts == true,
        showPercentages = meta.showPercentages == true,
      )
    val showTransfers = meta.showTransfers == true && !params.groupAccounts
    val filter = reportFilter(meta.conditions, meta.conditionsOp)

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

      if (meta.mode == Budgeted) {
        return@flatMapLatest budgets.observeRange(start..end.plusMonth()).map { months ->
          calculateBudgetSankey(
            title = meta.name,
            start = start,
            end = end,
            budget =
              sankeyBudget(
                months.dropLast(1),
                months.lastOrNull(),
                meta.conditions,
                meta.conditionsOp,
              ),
            categoryOrder = budgetCategoryOrder(months.firstOrNull()),
            params = params,
          )
        }
      }

      sankeyInputs(filter, start.firstDay, end.lastDay, showTransfers).map { (rows, transferRows) ->
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
              },
            ),
          categoryOrder = budgetOrder(rows),
          params = params,
        )
      }
    }
  }

  // Totalled in SQL without conditions. With them, the matching transactions are totalled here
  private fun sankeyInputs(
    filter: TransactionFilter,
    start: LocalDate,
    end: LocalDate,
    showTransfers: Boolean,
  ): Flow<Pair<List<SankeyCategoryTotals>, List<SankeyTransfers>>> {
    if (!filter.isEmpty) {
      return dao
        .observeFilteredTransactions(filter, start, end)
        .map { rows ->
          sankeyCategoryTotals(rows) to if (showTransfers) sankeyTransfers(rows) else emptyList()
        }
        .flowOn(contexts.default)
    }
    val transfers =
      if (showTransfers) dao.observeSankeyTransfers(start, end) else flowOf(emptyList())
    return combine(dao.observeSankeyCategoryTotals(start, end), transfers, ::Pair)
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
          timeFrame = meta.forecastTimeFrame(),
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

      if (isTracking) {
        trackingBudgetForecast(params)
      } else {
        scheduleForecast(params, meta.accounts?.toSet(), meta.conditions, meta.conditionsOp)
      }
    }

  // packages/loot-core/src/server/forecast/app.ts generateForecast(). Without an account filter,
  // schedules that have no account are included too.
  private fun scheduleForecast(
    params: ForecastParams,
    accountFilter: Set<AccountId>?,
    conditions: List<Condition>?,
    op: ConditionOp?,
  ): Flow<ChartData> {
    val start = params.start.firstDay
    val end = params.end.lastDay
    val firstForecastDate = if (end < params.today) start else maxOf(start, params.today)
    val filters = ForecastFilters(conditions, op)

    val scheduleInputs =
      combine(
        dao.observeForecastAccounts(),
        dao.observeForecastSchedules(),
        dao.observePostedScheduleTransactions(firstForecastDate.minus(POSTED_LOOKBACK_DAYS, DAY)),
        dao.observeForecastPayees(),
        ::ScheduleInputs,
      )

    return combine(scheduleInputs, postedForecastTotals(filters.transactions, start, end)) {
      inputs,
      posted ->
      val selected = filters.accounts(inputs.accounts, accountFilter)
      val accounts: Set<AccountId?> =
        when {
          selected.isEmpty() -> emptySet()
          accountFilter == null -> selected + null
          else -> selected
        }

      val postedDates =
        inputs.posted.filter { it.account in selected }.groupBy({ it.schedule }, { it.date })
      val occurrences =
        buildScheduleOccurrences(
          schedules = inputs.schedules.mapNotNull(::forecastSchedule),
          end = end,
          transferAccounts =
            inputs.payees.mapNotNull { p -> p.transfer_acct?.let { p.id to it } }.toMap(),
          postedDates = postedDates,
          transferPayees =
            inputs.payees.mapNotNull { p -> p.transfer_acct?.let { it to p.id } }.toMap(),
        )

      calculateBalanceForecast(
        params = params,
        accounts = accounts,
        startingBalances = posted.starting,
        dailyTotals =
          posted.daily
            .filter { it.account in selected }
            .groupingBy { it.date }
            .fold(0L) { total, row -> total + row.total },
        occurrences = occurrences.filter { filters.matches(it, inputs.accounts, inputs.payees) },
      )
    }
  }

  // Each account's balance before the forecast and its daily totals during it. Filtered forecasts
  // only count the matching transactions, like upstream's getTransactions()
  private fun postedForecastTotals(
    filter: TransactionFilter,
    start: LocalDate,
    end: LocalDate,
  ): Flow<PostedForecastTotals> =
    if (filter.isEmpty) {
      combine(
        dao.observeForecastStartingBalances(start),
        dao.observeForecastDailyTotals(start, end),
      ) { starting, daily ->
        PostedForecastTotals(
          starting = starting.associate { it.account to it.total },
          daily = daily.map { PostedForecastTotal(it.account, it.date, it.total) },
        )
      }
    } else {
      dao.observeFilteredTransactions(filter, ALL_TIME_START, end).map { rows ->
        val (before, during) = rows.partition { it.date < start }
        PostedForecastTotals(
          starting = before.groupingBy { it.account }.fold(0L) { total, row -> total + row.amount },
          daily =
            during
              .groupBy { it.account to it.date }
              .map { (key, txs) ->
                PostedForecastTotal(key.first, key.second, txs.sumOf { it.amount })
              },
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

  fun unsupported(meta: ReportMeta, reason: UnsupportedReason): Flow<ChartData> =
    flowOf(unsupportedData(meta, reason))

  private fun unsupportedData(meta: ReportMeta, reason: UnsupportedReason): UnsupportedData {
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
    return UnsupportedData(reason, type, name, stackTrace)
  }

  private fun YearMonth.toLong(): Long = year * YEAR_MONTH_FACTOR + month.ordinal + 1L

  private fun Long.toYearMonth(): YearMonth =
    YearMonth((this / YEAR_MONTH_FACTOR).toInt(), (this % YEAR_MONTH_FACTOR).toInt())

  private inline fun List<CashFlowByMonth>.total(predicate: (CashFlowByMonth) -> Boolean): Amount =
    Amount(filter(predicate).sumOf { it.total })

  private data class ScheduleInputs(
    val accounts: List<ForecastAccounts>,
    val schedules: List<ForecastSchedules>,
    val posted: List<ForecastPostedScheduleTransactions>,
    val payees: List<ForecastPayees>,
  )

  private data class PostedForecastTotals(
    val starting: Map<AccountId, Long>,
    val daily: List<PostedForecastTotal>,
  )

  private data class PostedForecastTotal(
    val account: AccountId,
    val date: LocalDate,
    val total: Long,
  )

  private companion object {
    const val YEAR_MONTH_FACTOR = 100L
    const val PERCENT_TENTHS = 1000.0
    const val TENTHS = 10.0
    const val DEFAULT_FORMULA = "=SUM(1, 2, 3)"
    const val DEFAULT_FONT_SIZE = 32.0
    const val RESULT_VARIABLE = "RESULT"
  }
}
