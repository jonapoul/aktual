package aktual.budget.reports.vm

import kotlinx.datetime.YearMonth

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
    is AgeOfMoneyReportMeta ->
      copy(
        conditions = conditions.orEmpty(),
        conditionsOp = conditionsOp ?: And,
        timeFrame = timeFrame ?: defaultTimeFrame(current),
        granularity = granularity?.takeIf { it != Unknown } ?: Monthly,
      )

    is BalanceForecastReportMeta,
    is CrossoverReportMeta,
    is CustomReportMeta,
    is FormulaReportMeta,
    is MarkdownReportMeta,
    is MonteCarloReportMeta,
    is NetWorthReportMeta,
    is SankeyReportMeta,
    is SpendingReportMeta,
    is SummaryReportMeta,
    is UnsupportedReportMeta -> this
  }
