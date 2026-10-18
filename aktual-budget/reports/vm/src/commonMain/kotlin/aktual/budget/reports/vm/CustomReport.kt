package aktual.budget.reports.vm

import aktual.budget.db.CustomReports
import aktual.budget.model.BalanceType
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.CustomReportMode
import aktual.budget.model.DateRangeType
import aktual.budget.model.FirstDayOfWeek
import aktual.budget.model.GraphType
import aktual.budget.model.GroupBy
import aktual.budget.model.Interval
import aktual.budget.model.ReportDate
import aktual.budget.model.SelectedCategory
import aktual.budget.model.SortBy
import aktual.budget.model.TagId
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.yearMonth
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** A row of the `custom_reports` table, with defaults filled in and unreadable values rejected */
@Immutable
data class CustomReportConfig(
  val mode: CustomReportMode,
  val groupBy: GroupBy,
  val balanceType: BalanceType,
  val graphType: GraphType,
  val interval: Interval,
  val dates: CustomReportDates,
  val showEmpty: Boolean,
  val showOffBudget: Boolean,
  val showHidden: Boolean,
  val showUncategorized: Boolean,
  val selectedCategories: ImmutableList<SelectedCategory>,
  val conditions: ImmutableList<Condition>,
  val conditionsOp: ConditionOp,
  val sortBy: SortBy,
  val trimIntervals: Boolean,
  val showTrendLines: Boolean,
  val colorScheme: String?,
  // Only used when split by tag
  val tagScope: CustomTagScope = All,
) {
  companion object {
    // packages/desktop-client/src/components/reports/ReportOptions.ts defaultReport
    val Default =
      CustomReportConfig(
        mode = Total,
        groupBy = Category,
        balanceType = Payment,
        graphType = BarGraph,
        interval = Monthly,
        dates = CustomReportDates.Live(Last6Months, includeCurrent = true),
        showEmpty = false,
        showOffBudget = false,
        showHidden = true,
        showUncategorized = true,
        selectedCategories = persistentListOf(),
        conditions = persistentListOf(),
        conditionsOp = And,
        sortBy = Desc,
        trimIntervals = false,
        showTrendLines = false,
        colorScheme = null,
      )
  }
}

// CustomReportTagScope: which tags a report split by tag buckets transactions by
@Immutable
sealed interface CustomTagScope {
  data object All : CustomTagScope

  data class Selected(val tagIds: ImmutableList<TagId>) : CustomTagScope
}

@Immutable
sealed interface CustomReportDates {
  /** Moves with the current date, see [resolveCustomReportRange] */
  data class Live(val type: DateRangeType, val includeCurrent: Boolean) : CustomReportDates

  data class Static(val start: LocalDate, val end: LocalDate) : CustomReportDates
}

// packages/loot-core/src/server/reports/app.ts reportModel.fromJS(), with the missing values taken
// from packages/desktop-client/src/components/reports/ReportOptions.ts defaultReport. Returns null
// if the row holds a value we don't understand, so we don't draw a chart from a guess.
internal fun CustomReports.toConfig(): CustomReportConfig? {
  val mode = mode ?: CustomReportMode.Total
  val groupBy = group_by ?: GroupBy.Category
  val balanceType = balance_type ?: BalanceType.Payment
  val graphType = graph_type ?: GraphType.BarGraph
  val interval = interval ?: Interval.Monthly
  val sortBy = sort_by ?: SortBy.Desc
  val conditionsOp = conditions_op ?: ConditionOp.Default
  val dates = dates() ?: return null

  val hasUnknown =
    mode == Unknown ||
      groupBy == Unknown ||
      balanceType == Unknown ||
      graphType == Unknown ||
      interval == Unknown ||
      sortBy == Unknown ||
      conditionsOp == Unknown
  if (hasUnknown) return null

  return CustomReportConfig(
    mode = mode,
    groupBy = groupBy,
    balanceType = balanceType,
    graphType = graphType,
    interval = interval,
    dates = dates,
    showEmpty = show_empty == true,
    showOffBudget = show_offbudget == true,
    showHidden = show_hidden == true,
    showUncategorized = show_uncategorized == true,
    selectedCategories = selected_categories?.toImmutableList() ?: persistentListOf(),
    conditions = conditions?.toImmutableList() ?: persistentListOf(),
    conditionsOp = conditionsOp,
    sortBy = sortBy,
    trimIntervals = trim_intervals == true,
    showTrendLines = show_trend_lines == true,
    colorScheme = color_scheme,
    tagScope = metadata?.get("tagScope").toTagScope(),
  )
}

// packages/desktop-client/src/components/reports/tagScope.ts normalizeTagScope(): anything but a
// non-empty selection covers all tags
private fun JsonElement?.toTagScope(): CustomTagScope {
  val scope = this as? JsonObject ?: return All
  val mode = (scope["mode"] as? JsonPrimitive)?.contentOrNull
  val ids =
    (scope["tagIds"] as? JsonArray)
      ?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull?.let(::TagId) }
      .orEmpty()
  return if (mode == "selected" && ids.isNotEmpty()) {
    CustomTagScope.Selected(ids.toImmutableList())
  } else {
    All
  }
}

private fun CustomReports.dates(): CustomReportDates? =
  if (date_static == true) {
    val start = start_date ?: return null
    val end = end_date ?: return null
    CustomReportDates.Static(start.date, end.lastDay())
  } else {
    // getLiveRange() treats a missing range like "All time"
    val type = date_range ?: DateRangeType.AllTime
    if (type == Unknown) {
      null
    } else {
      CustomReportDates.Live(type, includeCurrent = include_current == true)
    }
  }

// A month-only end date covers the whole month
private fun ReportDate.lastDay(): LocalDate =
  when (this) {
    is Date -> date
    is Month -> yearMonth.lastDay
  }

// packages/desktop-client/src/components/reports/getLiveRange.ts getLiveRange(). Without any
// transactions, upstream uses today for both the earliest and latest transaction.
@Suppress("CyclomaticComplexMethod")
internal fun resolveCustomReportRange(
  dates: CustomReportDates,
  today: LocalDate,
  earliestTransaction: LocalDate,
  latestTransaction: LocalDate,
  firstDayOfWeek: FirstDayOfWeek,
): LocalDateRange {
  val live =
    when (dates) {
      is Static -> return dates.start..dates.end
      is Live -> dates
    }

  val month = today.yearMonth
  val earlierBy = if (live.includeCurrent) 0 else 1
  fun clamped(start: LocalDate, end: LocalDate) = maxOf(start, earliestTransaction)..end
  fun months(offset: Int, add: Int = offset) = monthRange(month, offset, add)
  fun weeks(offset: Int, add: Int = offset) = weekRange(today, firstDayOfWeek, offset, add)

  return when (live.type) {
    ThisWeek -> weeks(offset = 0)
    LastWeek -> weeks(offset = 1, add = 1 - earlierBy)
    ThisMonth -> months(offset = 0)
    LastMonth -> months(offset = 1, add = 1 - earlierBy)
    Last3Months -> months(offset = 3, add = 3 - earlierBy)
    Last6Months -> months(offset = 6, add = 6 - earlierBy)
    Last12Months -> months(offset = 12, add = 12 - earlierBy)
    Last30Days -> clamped(today.minus(LAST_30_DAYS_OFFSET, DAY), today)
    CurrentQuarter -> quarter(month).let { clamped(it.start.firstDay, it.endInclusive.lastDay) }
    PreviousQuarter ->
      quarter(month.minus(MONTHS_PER_QUARTER, MONTH)).let {
        clamped(it.start.firstDay, it.endInclusive.lastDay)
      }
    YearToDate -> clamped(YearMonth(today.year, JANUARY).firstDay, today)
    LastYear ->
      clamped(
        YearMonth(today.year - 1, JANUARY).firstDay,
        YearMonth(today.year - 1, DECEMBER).lastDay,
      )
    PriorYearToDate ->
      clamped(YearMonth(today.year - 1, JANUARY).firstDay, today.minus(DatePeriod(years = 1)))
    AllTime,
    Unknown -> earliestTransaction..latestTransaction
  }
}

// packages/desktop-client/src/components/reports/reportRanges.ts getSpecificRange()
private fun monthRange(current: YearMonth, offset: Int, add: Int): LocalDateRange {
  val start = current.minus(offset, MONTH)
  return start.firstDay..start.plus(add, MONTH).lastDay
}

private fun weekRange(
  today: LocalDate,
  firstDayOfWeek: FirstDayOfWeek,
  offset: Int,
  add: Int,
): LocalDateRange {
  val start = weekStart(today, firstDayOfWeek).minus(offset, WEEK)
  return start..start.plus(add, WEEK).plus(DAYS_PER_WEEK - 1, DAY)
}

private const val LAST_30_DAYS_OFFSET = 29
