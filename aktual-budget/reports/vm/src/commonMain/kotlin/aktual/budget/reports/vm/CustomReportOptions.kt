package aktual.budget.reports.vm

import aktual.budget.model.BalanceType
import aktual.budget.model.CustomReportMode
import aktual.budget.model.DateRangeType
import aktual.budget.model.GraphType
import aktual.budget.model.GroupBy
import aktual.budget.model.Interval
import aktual.budget.model.SortBy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth

// packages/desktop-client/src/components/reports/ReportOptions.ts and disabledList.ts. Category and
// group is only for donut graphs, which aren't drawn yet, so neither is offered

val CUSTOM_MODES = persistentListOf<CustomReportMode>(Total, Time)

val CUSTOM_GROUP_BYS =
  persistentListOf<GroupBy>(
    Category,
    Group,
    Payee,
    Account,
    GroupBy.Interval,
  )

val CUSTOM_BALANCE_TYPES =
  persistentListOf<BalanceType>(
    Payment,
    Deposit,
    Net,
    NetPayment,
    NetDeposit,
    Budgeted,
  )

val CUSTOM_SORT_BYS = persistentListOf<SortBy>(Asc, Desc, Name, Budget)

val CUSTOM_INTERVALS = Interval.known

// ReportTopbar.tsx: the bar button draws stacked bars in time mode
val CustomReportConfig.graphOptions: ImmutableList<GraphType>
  get() = persistentListOf(TableGraph, barGraph(mode), LineGraph, AreaGraph)

fun CustomReportConfig.isGraphEnabled(graph: GraphType): Boolean = graph !in disabledGraphs(mode)

fun CustomReportConfig.isGroupByEnabled(option: GroupBy): Boolean {
  val budgetedOnly = balanceType == Budgeted && (option == Payee || option == Account)
  return !budgetedOnly && option !in rules().disabledGroupBys
}

// CustomReport.tsx disabledItems('type'): bar graphs of intervals allow every type
fun CustomReportConfig.isBalanceTypeEnabled(option: BalanceType): Boolean =
  (graphType == BarGraph && groupBy == GroupBy.Interval) || option !in rules().disabledBalanceTypes

fun CustomReportConfig.isIntervalEnabled(option: Interval): Boolean =
  balanceType != Budgeted || (option != Daily && option != Weekly)

// ReportSidebar.tsx disableSort
val CustomReportConfig.showsSort: Boolean
  get() = graphType == TableGraph || (groupBy != GroupBy.Interval && !rules().disableSort)

// Only ranges ending in the past can be stretched to include the current period
val CustomReportConfig.canIncludeCurrent: Boolean
  get() = (dates as? CustomReportDates.Live)?.type in INCLUDE_CURRENT_RANGES

val CustomReportConfig.canShowTrendLines: Boolean
  get() = graphType == LineGraph

fun dateRangeOptions(interval: Interval): ImmutableList<DateRangeType> =
  DateRangeType.known.filter { interval in it.intervals() }.toImmutableList()

// ReportSidebar.tsx onChangeMode() then CustomReport.tsx defaultModeItems()
fun CustomReportConfig.withMode(mode: CustomReportMode): CustomReportConfig {
  val swapped =
    when (graphType) {
      BarGraph,
      StackedBarGraph -> barGraph(mode)
      else -> graphType
    }
  val graph = if (swapped in disabledGraphs(mode)) TableGraph else swapped
  return copy(mode = mode, graphType = graph).withAllowedGroupBy().withAllowedBalanceType()
}

// ReportTopbar.tsx onChangeGraph() then CustomReport.tsx defaultItems()
fun CustomReportConfig.withGraphType(graph: GraphType): CustomReportConfig =
  copy(graphType = graph).withAllowedGroupBy().withAllowedBalanceType().withDefaultSort()

// ReportSidebar.tsx onChangeSplit()
fun CustomReportConfig.withGroupBy(option: GroupBy): CustomReportConfig =
  copy(groupBy = option).withAllowedBalanceType().withDefaultSort()

// ReportSidebar.tsx onChangeBalanceType(): budgets have no payee or account, and are monthly
fun CustomReportConfig.withBalanceType(option: BalanceType): CustomReportConfig {
  var config = copy(balanceType = option)
  if (option == Budgeted) {
    if (groupBy == Payee || groupBy == Account) config = config.withGroupBy(Category)
    if (interval == Daily || interval == Weekly) config = config.withInterval(Monthly)
  }
  return config
}

// A live range the interval doesn't offer moves to the interval's default
fun CustomReportConfig.withInterval(option: Interval): CustomReportConfig {
  val live = dates as? Live
  val newDates =
    if (live != null && live.type !in dateRangeOptions(option)) {
      live.copy(type = defaultRange(option))
    } else {
      dates
    }
  return copy(interval = option, dates = newDates)
}

fun CustomReportConfig.withDateRange(type: DateRangeType): CustomReportConfig {
  val includeCurrent = (dates as? Live)?.includeCurrent ?: true
  return copy(dates = CustomReportDates.Live(type, includeCurrent))
}

fun CustomReportConfig.withIncludeCurrent(includeCurrent: Boolean): CustomReportConfig {
  val live = dates as? Live ?: return this
  return copy(dates = live.copy(includeCurrent = includeCurrent))
}

// The static range starts as whatever the live range covers now, whole months either side
fun CustomReportConfig.withStaticDates(current: LocalDateRange): CustomReportConfig =
  copy(
    dates =
      CustomReportDates.Static(
        current.start.yearMonth.firstDay,
        current.endInclusive.yearMonth.lastDay,
      ),
  )

fun CustomReportConfig.withLiveDates(): CustomReportConfig =
  copy(dates = CustomReportDates.Live(defaultRange(interval), includeCurrent = true))

// Moving one end past the other moves both
fun CustomReportConfig.withStaticMonths(start: YearMonth, end: YearMonth): CustomReportConfig =
  copy(dates = CustomReportDates.Static(start.firstDay, maxOf(start, end).lastDay))

private class GraphRules(
  val disabledGroupBys: Set<GroupBy>,
  val defaultGroupBy: GroupBy,
  val disabledBalanceTypes: Set<BalanceType>,
  val defaultSort: SortBy,
  val disableSort: Boolean,
)

private fun CustomReportConfig.rules(): GraphRules =
  when (mode) {
    Time -> timeRules(graphType)
    Total,
    Unknown -> totalRules(graphType)
  }

private fun totalRules(graph: GraphType): GraphRules =
  when (graph) {
    TableGraph -> GraphRules(setOf(CategoryGroup), Category, emptySet(), Budget, false)
    AreaGraph ->
      GraphRules(
        disabledGroupBys = GroupBy.entries.toSet() - GroupBy.Interval,
        defaultGroupBy = GroupBy.Interval,
        disabledBalanceTypes = emptySet(),
        defaultSort = Desc,
        disableSort = true,
      )
    DonutGraph -> GraphRules(emptySet(), Category, setOf(Net), Desc, false)
    BarGraph,
    BarLineGraph,
    LineGraph,
    StackedBarGraph,
    Unknown -> GraphRules(setOf(CategoryGroup), Category, emptySet(), Desc, false)
  }

private fun timeRules(graph: GraphType): GraphRules {
  val disabledGroupBys = setOf(GroupBy.Interval, CategoryGroup)
  val disabledBalanceTypes =
    if (graph == TableGraph) setOf<BalanceType>(NetPayment, NetDeposit) else emptySet()
  return GraphRules(disabledGroupBys, Category, disabledBalanceTypes, Desc, true)
}

private fun CustomReportConfig.withAllowedGroupBy(): CustomReportConfig {
  val rules = rules()
  return if (groupBy in rules.disabledGroupBys) copy(groupBy = rules.defaultGroupBy) else this
}

private fun CustomReportConfig.withAllowedBalanceType(): CustomReportConfig =
  if (isBalanceTypeEnabled(balanceType)) this else copy(balanceType = Payment)

private fun CustomReportConfig.withDefaultSort(): CustomReportConfig =
  copy(sortBy = rules().defaultSort)

private fun barGraph(mode: CustomReportMode): GraphType =
  if (mode == Time) StackedBarGraph else BarGraph

// disabledList.ts modeOptions disabledGraph. Total mode can't draw a stacked bar either, since the
// bar button means a plain bar there
private fun disabledGraphs(mode: CustomReportMode): Set<GraphType> =
  when (mode) {
    Time -> setOf(AreaGraph, DonutGraph, BarGraph)
    Total,
    Unknown -> setOf(LineGraph, StackedBarGraph)
  }

// disabledList.ts intervalOptions defaultRange
private fun defaultRange(interval: Interval): DateRangeType =
  when (interval) {
    Daily -> ThisMonth
    Weekly -> Last3Months
    Yearly -> YearToDate
    Monthly,
    Unknown -> Last6Months
  }

// ReportOptions.ts dateRangeOptions: which intervals each range is offered for
@Suppress("CyclomaticComplexMethod")
private fun DateRangeType.intervals(): Set<Interval> =
  when (this) {
    ThisWeek,
    LastWeek -> setOf(Daily, Weekly)
    ThisMonth,
    LastMonth,
    CurrentQuarter,
    PreviousQuarter,
    Last30Days,
    Last3Months -> setOf(Daily, Weekly, Monthly)
    Last6Months,
    Last12Months -> setOf(Monthly)
    YearToDate,
    LastYear,
    PriorYearToDate,
    AllTime -> setOf(Weekly, Monthly, Yearly)
    Unknown -> emptySet()
  }

// disabledList.ts currentIntervalOptions lists the ranges that can't include the current period
private val INCLUDE_CURRENT_RANGES =
  setOf<DateRangeType>(
    LastWeek,
    Last3Months,
    Last6Months,
    Last12Months,
  )
