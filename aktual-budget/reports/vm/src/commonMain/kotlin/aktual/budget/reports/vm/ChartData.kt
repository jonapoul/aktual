package aktual.budget.reports.vm

import aktual.budget.model.Amount
import aktual.budget.model.DateRangeType
import aktual.budget.model.WidgetType
import aktual.core.model.Percent
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.YearMonthRange

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

data class NetWorthData(val title: String?, val items: ImmutableMap<YearMonth, Amount>) : ChartData

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

enum class SummaryChartType {
  Sum,
  AveragePerMonth,
  AveragePerYear,
  AveragePerTransaction,
  Percentage,
}

data class CalendarData(
  val title: String,
  val start: YearMonth,
  val end: YearMonth,
  val income: Amount,
  val expenses: Amount,
  val months: ImmutableList<CalendarMonth>,
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

data class TextData(val content: String) : ChartData

data class UnsupportedData(
  val reason: UnsupportedReason,
  val type: WidgetType,
  val name: String?,
) : ChartData

enum class UnsupportedReason {
  Filters,
  ReportType,
}

data class CustomData(val title: String, val mode: DateRangeMode, val range: ReportTimeRange) :
  ChartData

@Immutable
sealed interface ReportTimeRange {
  @JvmInline value class Relative(val type: DateRangeType) : ReportTimeRange

  data class Specific(val range: YearMonthRange) : ReportTimeRange
}
