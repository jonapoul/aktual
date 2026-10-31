package aktual.budget.reports.vm

import aktual.budget.model.AccountId
import aktual.budget.model.CategoryId
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.CustomReportId
import aktual.budget.model.Interval
import aktual.budget.model.WidgetType
import androidx.compose.runtime.Immutable
import fallback.serializer.Fallback
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import logcat.logcat

// From packages/loot-core/src/types/models/dashboard.ts
@Immutable
sealed interface ReportMeta {
  companion object {
    fun serializer(type: WidgetType): KSerializer<out ReportMeta> =
      when (type) {
        BudgetAnalysis -> BudgetAnalysisReportMeta.serializer()
        Calendar -> CalendarReportMeta.serializer()
        CashFlow -> CashFlowReportMeta.serializer()
        Custom -> CustomReportMeta.serializer()
        Markdown -> MarkdownReportMeta.serializer()
        NetWorth -> NetWorthReportMeta.serializer()
        Spending -> SpendingReportMeta.serializer()
        Summary -> SummaryReportMeta.serializer()
        Formula -> FormulaReportMeta.serializer()
        Crossover -> CrossoverReportMeta.serializer()
        Sankey -> SankeyReportMeta.serializer()
        BalanceForecast -> BalanceForecastReportMeta.serializer()
        AgeOfMoney -> AgeOfMoneyReportMeta.serializer()
        MonteCarlo -> MonteCarloReportMeta.serializer()
        Unknown -> error("Unknown widget type")
      }
  }
}

// A widget whose stored metadata couldn't be deserialized, shown instead of crashing the dashboard.
// Never persisted, so not @Serializable. The raw json is kept so the original row isn't lost
data class UnsupportedReportMeta(
  val type: WidgetType,
  val raw: JsonObject,
  val stackTrace: String? = null,
) : ReportMeta

@Serializable data class CustomReportMeta(@SerialName("id") val id: CustomReportId) : ReportMeta

@Serializable
data class NetWorthReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("interval") val interval: Interval? = null,
  @SerialName("mode") val mode: NetWorthMode? = null,
) : ReportMeta

@Serializable
data class CashFlowReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("showBalance") val showBalance: Boolean? = null,
) : ReportMeta

@Serializable
data class SpendingReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("compare") val compare: String? = null,
  @SerialName("compareTo") val compareTo: String? = null,
  @SerialName("isLive") val isLive: Boolean? = null,
  @SerialName("mode") val mode: SpendingMode? = null,
  @SerialName("averageRange") val averageRange: SpendingAverageRange? = null,
) : ReportMeta

@Serializable
data class BudgetAnalysisReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("interval") val interval: Interval? = null,
  @SerialName("graphType") val graphType: GraphType? = null,
  @SerialName("showBalance") val showBalance: Boolean? = null,
  @SerialName("balanceOnly") val balanceOnly: Boolean? = null,
  @SerialName("showHiddenCategories") val showHiddenCategories: Boolean? = null,
) : ReportMeta

@Serializable
data class MarkdownReportMeta(
  @SerialName("content") val content: String = "",
  @SerialName("text_align") val textAlign: TextAlign? = null,
) : ReportMeta

@Serializable
data class SummaryReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("content") val content: String? = null,
) : ReportMeta

@Serializable
data class CalendarReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
) : ReportMeta

@Serializable
data class FormulaReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("formula") val formula: String? = null,
  @SerialName("fontSize") val fontSize: Double? = null,
  @SerialName("fontSizeMode") val fontSizeMode: FontSizeMode? = null,
  @SerialName("staticFontSize") val staticFontSize: Double? = null,
  @SerialName("showTitle") val showTitle: Boolean? = null,
  @SerialName("colorFormula") val colorFormula: String? = null,
  @SerialName("queriesVersion") val queriesVersion: Int? = null,
  @SerialName("queries") val queries: Map<String, FormulaQuery> = emptyMap(),
) : ReportMeta

@Serializable
data class CrossoverReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("expenseCategoryIds") val expenseCategoryIds: List<CategoryId>? = null,
  @SerialName("incomeAccountIds") val incomeAccountIds: List<AccountId>? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("safeWithdrawalRate") val safeWithdrawalRate: Double? = null,
  @SerialName("estimatedReturn") val estimatedReturn: Double? = null,
  @SerialName("expectedContribution") val expectedContribution: Double? = null,
  @SerialName("projectionType") val projectionType: ProjectionType? = null,
  @SerialName("showHiddenCategories") val showHiddenCategories: Boolean? = null,
  @SerialName("expenseAdjustmentFactor") val expenseAdjustmentFactor: Double? = null,
) : ReportMeta

@Serializable
data class SankeyReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("mode") val mode: SankeyMode? = null,
  @SerialName("topNcategories") val topNCategories: Int? = null,
  @SerialName("categorySort") val categorySort: CategorySort? = null,
  @SerialName("showPercentages") val showPercentages: Boolean? = null,
  @SerialName("groupAccounts") val groupAccounts: Boolean? = null,
  @SerialName("showTransfers") val showTransfers: Boolean? = null,
  @SerialName("layerFrom") val layerFrom: String? = null,
  @SerialName("layerTo") val layerTo: String? = null,
) : ReportMeta

@Serializable
data class BalanceForecastReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("startDate") val startDate: String? = null,
  @SerialName("endDate") val endDate: String? = null,
  @SerialName("accounts") val accounts: List<AccountId>? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("granularity") val granularity: ForecastGranularity? = null,
  @SerialName("source") val source: ForecastSource? = null,
) : ReportMeta

@Serializable
data class AgeOfMoneyReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("granularity") val granularity: AgeOfMoneyGranularity? = null,
) : ReportMeta

@Immutable
@Serializable
data class FormulaQuery(
  @SerialName("conditions") val conditions: List<Condition>,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp?,
  @SerialName("timeFrame") val timeFrame: TimeFrame?,
)

@Immutable
@Serializable
data class TimeFrame(
  @SerialName("start") @Serializable(LenientYearMonthSerializer::class) val start: YearMonth,
  @SerialName("end") @Serializable(LenientYearMonthSerializer::class) val end: YearMonth,
  @SerialName("mode") val mode: TimeFrameMode,
)

// Upstream persists TimeFrame start/end as plain date strings, which may be either a year-month
// ("2011-10") or a full ISO date ("2011-10-05"). The default YearMonth serializer only accepts
// the former and crashes on the latter, so parse leniently and normalise both down to a YearMonth.
internal object LenientYearMonthSerializer : KSerializer<YearMonth> {
  override val descriptor = PrimitiveSerialDescriptor("aktual.LenientYearMonth", STRING)

  override fun serialize(encoder: Encoder, value: YearMonth) =
    encoder.encodeString(value.toString())

  override fun deserialize(decoder: Decoder): YearMonth {
    val raw = decoder.decodeString()
    return raw.toYearMonthOrNull()
      ?: throw SerializationException("Can't parse '$raw' as a year-month or date")
  }
}

private fun String.toYearMonthOrNull(): YearMonth? =
  try {
    YearMonth.parse(this)
  } catch (_: IllegalArgumentException) {
    try {
      LocalDate.parse(this).yearMonth
    } catch (e: IllegalArgumentException) {
      logcat.e(e) { "Failed decoding $this as YearMonth or LocalDate" }
      null
    }
  }

@Serializable
enum class TimeFrameMode {
  @SerialName("sliding-window") SlidingWindow,
  @SerialName("static") Static,
  @SerialName("full") Full,
  @SerialName("lastMonth") LastMonth,
  @SerialName("lastYear") LastYear,
  @SerialName("yearToDate") YearToDate,
  @SerialName("priorYearToDate") PriorYearToDate,
  @SerialName("currentQuarter") CurrentQuarter,
  @SerialName("previousQuarter") PreviousQuarter,
  @Fallback Unknown,
}

@Serializable
enum class NetWorthMode {
  @SerialName("trend") Trend,
  @SerialName("stacked") Stacked,
  @Fallback Unknown,
}

@Serializable
enum class SpendingMode {
  @SerialName("single-month") SingleMonth,
  @SerialName("budget") Budget,
  @SerialName("average") Average,
  @Fallback Unknown,
}

// SpendingAverageRange in packages/loot-core/src/types/models/dashboard.ts
@Immutable
@Serializable(SpendingAverageRangeSerializer::class)
sealed interface SpendingAverageRange {
  data class LastMonths(val months: Int) : SpendingAverageRange

  data object YearToDate : SpendingAverageRange

  data object AllTime : SpendingAverageRange

  companion object {
    val Default = LastMonths(months = 3)
    val SupportedMonths = setOf(3, 6, 12)
  }
}

// Unknown modes and month counts fall back to the last 3 months, like upstream's
// normalizeSpendingAverageRange in
// packages/desktop-client/src/components/reports/spendingAverageRange.ts
internal object SpendingAverageRangeSerializer : KSerializer<SpendingAverageRange> {
  private const val LAST_N_MONTHS = "last-n-months"
  private const val YEAR_TO_DATE = "year-to-date"
  private const val ALL_TIME = "all-time"

  override val descriptor = JsonObject.serializer().descriptor

  override fun serialize(encoder: Encoder, value: SpendingAverageRange) {
    val json =
      when (value) {
        is LastMonths ->
          mapOf("mode" to JsonPrimitive(LAST_N_MONTHS), "months" to JsonPrimitive(value.months))
        YearToDate -> mapOf("mode" to JsonPrimitive(YEAR_TO_DATE))
        AllTime -> mapOf("mode" to JsonPrimitive(ALL_TIME))
      }
    encoder.encodeSerializableValue(JsonObject.serializer(), JsonObject(json))
  }

  override fun deserialize(decoder: Decoder): SpendingAverageRange {
    val json = (decoder as JsonDecoder).decodeJsonElement() as? JsonObject
    val mode = (json?.get("mode") as? JsonPrimitive)?.contentOrNull
    val months = (json?.get("months") as? JsonPrimitive)?.intOrNull
    return when (mode) {
      LAST_N_MONTHS if months in SpendingAverageRange.SupportedMonths ->
        SpendingAverageRange.LastMonths(checkNotNull(months))
      YEAR_TO_DATE -> YearToDate
      ALL_TIME -> AllTime
      else -> Default
    }
  }
}

@Serializable
enum class GraphType {
  @SerialName("Line") Line,
  @SerialName("Bar") Bar,
  @Fallback Unknown,
}

@Serializable
enum class TextAlign {
  @SerialName("left") Left,
  @SerialName("right") Right,
  @SerialName("center") Center,
  @Fallback Unknown,
}

@Serializable
enum class FontSizeMode {
  @SerialName("dynamic") Dynamic,
  @SerialName("static") Static,
  @Fallback Unknown,
}

@Serializable
enum class ProjectionType {
  @SerialName("hampel") Hampel,
  @SerialName("median") Median,
  @SerialName("mean") Mean,
  @Fallback Unknown,
}

@Serializable
enum class SankeyMode {
  @SerialName("budgeted") Budgeted,
  @SerialName("spent") Spent,
  @Fallback Unknown,
}

@Serializable
enum class CategorySort {
  @SerialName("per-group") PerGroup,
  @SerialName("global") Global,
  @SerialName("budget-order") BudgetOrder,
  @Fallback Unknown,
}

@Serializable
enum class ForecastGranularity {
  @SerialName("Daily") Daily,
  @SerialName("Monthly") Monthly,
  @Fallback Unknown,
}

@Serializable
enum class ForecastSource {
  @SerialName("schedules") Schedules,
  @SerialName("tracking-budget") TrackingBudget,
  @Fallback Unknown,
}

@Serializable
enum class AgeOfMoneyGranularity {
  @SerialName("daily") Daily,
  @SerialName("weekly") Weekly,
  @SerialName("monthly") Monthly,
  @Fallback Unknown,
}
