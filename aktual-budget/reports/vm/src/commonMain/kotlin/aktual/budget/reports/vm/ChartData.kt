package aktual.budget.reports.vm

import aktual.budget.formula.ErrorValue
import aktual.budget.formula.FormulaWarning
import aktual.budget.model.Amount
import aktual.budget.model.CategoryId
import aktual.budget.model.ConditionSet
import aktual.budget.model.WidgetType
import aktual.core.model.Percent
import androidx.compose.runtime.Immutable
import fallback.serializer.Fallback
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable sealed interface ChartData

data class CashFlowData(val title: String?, val items: ImmutableMap<YearMonth, CashFlowDatum>) :
  ChartData

data class CashFlowDatum(
  val income: Amount,
  val expenses: Amount,
  val transfers: Amount,
  val balance: Amount,
  val change: Amount = income + expenses + transfers,
)

data class NetWorthData(
  val title: String?,
  val items: ImmutableMap<YearMonth, Amount>,
  val mode: NetWorthMode = Trend,
  // In stacking order, see netWorthData
  val accounts: ImmutableList<NetWorthAccount> = persistentListOf(),
) : ChartData

data class AgeOfMoneyData(
  val title: String?,
  val start: YearMonth,
  val end: YearMonth,
  val granularity: AgeOfMoneyGranularity,
  val items: ImmutableMap<LocalDate, Int>,
  val currentAge: Int?,
  val trend: AgeOfMoneyTrend,
  val insufficientData: Boolean,
) : ChartData

enum class AgeOfMoneyTrend {
  Up,
  Down,
  Stable,
}

data class BudgetAnalysisData(
  val title: String?,
  val items: ImmutableMap<YearMonth, BudgetAnalysisDatum>,
  val graphType: GraphType,
  val showBalance: Boolean,
  val balanceOnly: Boolean,
) : ChartData {
  val totalBudgeted: Amount = items.values.fold(Zero) { total, datum -> total + datum.budgeted }
  val totalSpent: Amount = items.values.fold(Zero) { total, datum -> total + datum.spent }
  val totalOverspendingAdjustment: Amount =
    items.values.fold(Zero) { total, datum -> total + datum.overspendingAdjustment }
  val endingBalance: Amount = items.values.lastOrNull()?.balance ?: Zero
}

// Spending is negative. overspendingAdjustment is positive, the amount reset from last month
data class BudgetAnalysisDatum(
  val budgeted: Amount,
  val spent: Amount,
  val overspendingAdjustment: Amount,
  val balance: Amount,
)

data class CrossoverData(
  val title: String?,
  val items: ImmutableMap<YearMonth, CrossoverDatum>,
  val crossover: YearMonth?,
  val yearsToRetire: Double?,
) : ChartData

data class CrossoverDatum(
  val investmentIncome: Amount,
  val expenses: Amount,
  val nestEgg: Amount,
  // Only set on projected months
  val adjustedExpenses: Amount? = null,
)

data class BalanceForecastData(
  val title: String?,
  val start: YearMonth,
  val end: YearMonth,
  val granularity: ForecastGranularity,
  val source: ForecastSource,
  // Monthly points are keyed by the first day of the month, with the balance at its end
  val items: ImmutableMap<LocalDate, Amount>,
  val today: LocalDate,
  val scheduledCount: Int,
) : ChartData

data class MonteCarloData(
  val title: String?,
  // Share of simulations that last until targetAge
  val successRate: Percent,
  val currentAge: Int,
  val targetAge: Int,
  // One band per age, from currentAge to targetAge, in today's money
  val bands: ImmutableList<MonteCarloBand>,
  val medianEndingBalance: Amount,
  // Median age the money runs out among failed simulations
  val medianDepletionAge: Int?,
  val simulationCount: Int,
) : ChartData

data class MonteCarloBand(
  val age: Int,
  val p10: Amount,
  val p25: Amount,
  val p50: Amount,
  val p75: Amount,
  val p90: Amount,
)

const val MIN_SANKEY_NODES = 2

data class SankeyData(
  val title: String?,
  val start: YearMonth,
  val end: YearMonth,
  val showPercentages: Boolean,
  // Within a column, nodes are drawn top to bottom in list order
  val nodes: ImmutableList<SankeyNode>,
  val links: ImmutableList<SankeyLink>,
  // Graphs with fewer nodes per layer, for charts too short to fit nodes. The one at index i keeps
  // at most MIN_SANKEY_NODES + i nodes per layer, and the list ends before the first that would
  // match nodes and links
  val reduced: ImmutableList<SankeyGraph> = persistentListOf(),
) : ChartData

fun SankeyData.limitTo(maxNodes: Int): SankeyData {
  val graph = reduced.getOrNull(maxNodes.coerceAtLeast(MIN_SANKEY_NODES) - MIN_SANKEY_NODES)
  return graph?.let { copy(nodes = it.nodes, links = it.links, reduced = persistentListOf()) }
    ?: this
}

data class SankeyGraph(val nodes: ImmutableList<SankeyNode>, val links: ImmutableList<SankeyLink>)

data class SankeyNode(
  val key: String,
  val label: SankeyLabel,
  val column: Int,
  val value: Amount,
  val percent: Percent,
  val color: SankeyColor,
)

// source and target are indices into SankeyData.nodes. grouped lists the nodes merged into an
// "Other" node at either end, biggest first
data class SankeyLink(
  val source: Int,
  val target: Int,
  val value: Amount,
  val color: SankeyColor,
  val grouped: ImmutableList<SankeyGroupedItem> = persistentListOf(),
)

@Immutable data class SankeyGroupedItem(val name: String, val value: Amount)

@Immutable
sealed interface SankeyLabel {
  data class Text(val value: String) : SankeyLabel

  data object Income : SankeyLabel

  data object Other : SankeyLabel
}

@Immutable
sealed interface SankeyColor {
  data class Palette(val index: Int) : SankeyColor

  data object Primary : SankeyColor

  data object Negative : SankeyColor
}

@Immutable
sealed interface SummaryData : ChartData, DateRange {
  val title: String

  data class Sum(
    override val title: String,
    override val start: LocalDate,
    override val end: LocalDate,
    val value: Amount,
  ) : SummaryData

  data class AveragePerMonth(
    override val title: String,
    override val start: LocalDate,
    override val end: LocalDate,
    val numMonths: Float,
    val total: Amount,
    val average: Amount,
  ) : SummaryData

  data class AveragePerYear(
    override val title: String,
    override val start: LocalDate,
    override val end: LocalDate,
    val numYears: Float,
    val total: Amount,
    val average: Amount,
  ) : SummaryData

  data class AveragePerTransaction(
    override val title: String,
    override val start: LocalDate,
    override val end: LocalDate,
    val numTransactions: Int,
    val total: Amount,
    val average: Amount,
  ) : SummaryData

  data class Percentage(
    override val title: String,
    override val start: LocalDate,
    override val end: LocalDate,
    val numerator: Amount,
    val denominator: Amount,
    val percent: Percent,
    val divisor: PercentageDivisor,
  ) : SummaryData
}

@Immutable
sealed interface PercentageDivisor : DateRange {
  data class Specific(override val start: LocalDate, override val end: LocalDate) :
    PercentageDivisor

  data object AllTime : PercentageDivisor {
    override val start = null
    override val end = null
  }
}

@Immutable
interface DateRange {
  val start: LocalDate?
  val end: LocalDate?
}

@Serializable
enum class SummaryChartType {
  @SerialName("sum") @Fallback Sum,
  @SerialName("avgPerMonth") AveragePerMonth,
  @SerialName("avgPerYear") AveragePerYear,
  @SerialName("avgPerTransact") AveragePerTransaction,
  @SerialName("percentage") Percentage,
}

data class CalendarData(
  val title: String,
  val start: YearMonth,
  val end: YearMonth,
  val income: Amount,
  val expenses: Amount,
  val firstDayOfWeek: DayOfWeek,
  val months: ImmutableList<CalendarMonth>,
  // The report's own conditions, which a tapped day's transactions also have to match
  val conditions: ConditionSet? = null,
) : ChartData

@Immutable
data class CalendarMonth(
  val income: Amount,
  val expenses: Amount,
  val month: YearMonth,
  val days: ImmutableList<CalendarDay>,
)

@Immutable data class CalendarDay(val day: Int, val income: Amount, val expenses: Amount)

enum class DateRangeMode {
  Live,
  Static,
}

@Immutable
data class ChartDateConfig(
  val mode: DateRangeMode,
  val start: YearMonth,
  val end: YearMonth,
  val range: YearMonthRange,
)

data class SpendingData(
  val title: String,
  val mode: DateRangeMode,
  val targetMonth: YearMonth,
  val comparison: SpendingComparison,
  val difference: Amount,
  val days: ImmutableList<SpendingDay>,
) : ChartData

@Immutable
sealed interface SpendingDayNumber {
  @JvmInline value class Specific(val number: Int) : SpendingDayNumber

  data object End : SpendingDayNumber
}

data class SpendingDay(val number: SpendingDayNumber, val target: Amount?, val comparison: Amount)

@Immutable
sealed interface SpendingComparison {
  data class SingleMonth(val value: YearMonth) : SpendingComparison

  data object Budgeted : SpendingComparison

  data object Average : SpendingComparison
}

data class TextData(val content: String, val align: TextAlign = Left) : ChartData

/**
 * [color] is the colour formula's result, a CSS colour or a `theme_<name>` placeholder (see
 * [FORMULA_THEME_PREFIX]), and null if there's no valid one
 */
data class FormulaData(
  val title: String?,
  val showTitle: Boolean,
  val output: FormulaOutput,
  val color: String?,
  val fontSizeMode: FontSizeMode,
  val staticFontSize: Double,
  val warnings: ImmutableList<FormulaWarning>,
) : ChartData

@Immutable
sealed interface FormulaOutput {
  data class Money(val amount: Amount) : FormulaOutput

  data class Text(val value: String) : FormulaOutput

  data class Failure(val error: ErrorValue) : FormulaOutput
}

data class UnsupportedData(
  val reason: UnsupportedReason,
  val type: WidgetType,
  val name: String?,
  val stackTrace: String? = null,
) : ChartData

enum class UnsupportedReason {
  Filters,
  InvalidMeta,
  CustomBudgeted,
  MissingReport,
  ReportType,
  SankeyBudgeted,
}

/**
 * [groups] is what the report is grouped by, and [categoryGroups] each category group with its
 * categories. Both leave out empty groups unless the report shows them, and are sorted as the
 * report asks. [trends] has a line per group when the report shows trend lines. [intervalCount] is
 * how many intervals the report's dates cover, before any are trimmed.
 */
data class CustomData(
  val title: String,
  val config: CustomReportConfig,
  val range: LocalDateRange,
  val intervals: ImmutableList<CustomIntervalTotals>,
  val intervalCount: Int,
  val groups: ImmutableList<CustomGroupTotals>,
  val categoryGroups: ImmutableList<CustomGroupTotals>,
  val totals: CustomTotals,
  val trends: ImmutableMap<CustomGroup, CustomTrend>,
  // What a drilled down list needs to leave out the rows the report doesn't show
  val hiddenCategories: ImmutableSet<CategoryId> = persistentSetOf(),
  val tagScope: ImmutableList<String> = persistentListOf(),
) : ChartData
