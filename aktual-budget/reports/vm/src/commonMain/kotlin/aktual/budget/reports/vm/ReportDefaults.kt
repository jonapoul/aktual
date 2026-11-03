package aktual.budget.reports.vm

import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.minusMonth
import kotlinx.datetime.plus

// Fills in upstream's defaults, so the draft starts out the same as the saved report would show
internal fun ReportMeta.withDefaults(current: YearMonth): ReportMeta =
  when (this) {
    is BudgetAnalysisReportMeta -> withSettings(settings(timeFrame ?: defaultTimeFrame(current)))
    is NetWorthReportMeta ->
      copy(
        conditions = conditions.orEmpty(),
        conditionsOp = conditionsOp ?: And,
        timeFrame = timeFrame ?: defaultTimeFrame(current),
        interval = interval?.takeIf { it != Unknown } ?: Monthly,
        mode = if (mode == Stacked) Stacked else Trend,
      )
    is CashFlowReportMeta ->
      copy(
        conditions = conditions.orEmpty(),
        conditionsOp = conditionsOp ?: And,
        timeFrame = timeFrame ?: TimeFrame(current, current, SlidingWindow),
        showBalance = showBalance != false,
      )
    is CalendarReportMeta ->
      copy(
        conditions = conditions.orEmpty(),
        conditionsOp = conditionsOp ?: And,
        timeFrame = timeFrame ?: TimeFrame(current, current, Full),
      )
    is CrossoverReportMeta ->
      copy(
        timeFrame =
          timeFrame
            ?: TimeFrame(
              current.minus(DEFAULT_CROSSOVER_MONTHS, MONTH),
              current.minusMonth(),
              Full,
            ),
        safeWithdrawalRate = safeWithdrawalRate ?: DEFAULT_SAFE_WITHDRAWAL_RATE,
        projectionType = projectionType?.takeIf { it != Unknown } ?: Hampel,
        showHiddenCategories = showHiddenCategories == true,
        expenseAdjustmentFactor = expenseAdjustmentFactor ?: 1.0,
      )
    is SankeyReportMeta -> withDefaults(current)
    is BalanceForecastReportMeta ->
      copy(
        conditions = conditions.orEmpty(),
        conditionsOp = conditionsOp ?: And,
        timeFrame =
          forecastTimeFrame()
            ?: TimeFrame(current, current.plus(DEFAULT_FORECAST_MONTHS - 1, MONTH), Static),
        granularity = granularity?.takeIf { it != Unknown } ?: Monthly,
        source = if (source == TrackingBudget) TrackingBudget else Schedules,
      )
    is AgeOfMoneyReportMeta ->
      copy(
        conditions = conditions.orEmpty(),
        conditionsOp = conditionsOp ?: And,
        timeFrame = timeFrame ?: defaultTimeFrame(current),
        granularity = granularity?.takeIf { it != Unknown } ?: Monthly,
      )

    is CustomReportMeta,
    is FormulaReportMeta,
    is MarkdownReportMeta,
    is MonteCarloReportMeta,
    is SpendingReportMeta,
    is SummaryReportMeta,
    is UnsupportedReportMeta -> this
  }

private fun SankeyReportMeta.withDefaults(current: YearMonth): SankeyReportMeta {
  val layers = normalizeSankeyLayers(mode, SankeyLayer.parse(layerFrom), SankeyLayer.parse(layerTo))
  return copy(
    conditions = conditions.orEmpty(),
    conditionsOp = conditionsOp ?: And,
    timeFrame = timeFrame ?: TimeFrame(current, current, SlidingWindow),
    mode = if (mode == Budgeted) Budgeted else Spent,
    topNCategories = topNCategories ?: DEFAULT_TOP_N_CATEGORIES,
    categorySort = categorySort?.takeIf { it != Unknown } ?: PerGroup,
    showPercentages = showPercentages == true,
    groupAccounts = groupAccounts == true,
    showTransfers = showTransfers == true,
    layerFrom = layers.from.key,
    layerTo = layers.to.key,
  )
}
