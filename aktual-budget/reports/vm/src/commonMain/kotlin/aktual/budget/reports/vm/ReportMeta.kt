package aktual.budget.reports.vm

import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.budget.model.CustomReportId
import aktual.budget.model.Interval
import aktual.budget.model.WidgetType
import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.datetime.yearMonth
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import logcat.logcat

// From packages/loot-core/src/types/models/dashboard.ts
@Immutable
sealed interface ReportMeta {
  companion object {
    fun serializer(type: WidgetType): KSerializer<out ReportMeta> =
      when (type) {
        WidgetType.BudgetAnalysis -> BudgetAnalysisReportMeta.serializer()
        WidgetType.Calendar -> CalendarReportMeta.serializer()
        WidgetType.CashFlow -> CashFlowReportMeta.serializer()
        WidgetType.Custom -> CustomReportMeta.serializer()
        WidgetType.Markdown -> MarkdownReportMeta.serializer()
        WidgetType.NetWorth -> NetWorthReportMeta.serializer()
        WidgetType.Spending -> SpendingReportMeta.serializer()
        WidgetType.Summary -> SummaryReportMeta.serializer()
        WidgetType.Formula -> FormulaReportMeta.serializer()
      }
  }
}

// Sentinel for a widget whose stored metadata couldn't be deserialized — corrupt data, or an
// upstream schema change we don't model yet. Surfaced instead of crashing the whole dashboard.
// It's never persisted, so it's intentionally not @Serializable; we keep the raw json around so
// the original row isn't lost.
@Immutable
data class UnsupportedReportMeta(
  val type: WidgetType,
  val raw: JsonObject,
  val reason: String,
) : ReportMeta

@Immutable
@Serializable
data class CustomReportMeta(@SerialName("id") val id: CustomReportId) : ReportMeta

@Immutable
@Serializable
data class NetWorthReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("interval") val interval: Interval? = null,
  @SerialName("mode") val mode: NetWorthMode? = null,
) : ReportMeta

@Immutable
@Serializable
data class CashFlowReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("showBalance") val showBalance: Boolean? = null,
) : ReportMeta

@Immutable
@Serializable
data class SpendingReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("compare") val compare: String? = null,
  @SerialName("compareTo") val compareTo: String? = null,
  @SerialName("isLive") val isLive: Boolean? = null,
  @SerialName("mode") val mode: SpendingMode? = null,
) : ReportMeta

@Immutable
@Serializable
data class BudgetAnalysisReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("interval") val interval: Interval? = null,
  @SerialName("graphType") val graphType: GraphType? = null,
  @SerialName("showBalance") val showBalance: Boolean? = null,
) : ReportMeta

@Immutable
@Serializable
data class MarkdownReportMeta(
  @SerialName("content") val content: String,
  @SerialName("text_align") val textAlign: TextAlign? = null,
) : ReportMeta

@Immutable
@Serializable
data class SummaryReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("content") val content: String? = null,
) : ReportMeta

@Immutable
@Serializable
data class CalendarReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: ConditionOp? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
) : ReportMeta

@Immutable
@Serializable
data class FormulaReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("formula") val formula: String? = null,
  @SerialName("fontSize") val fontSize: Int? = null,
  @SerialName("fontSizeMode") val fontSizeMode: FontSizeMode? = null,
  @SerialName("staticFontSize") val staticFontSize: Int? = null,
  @SerialName("colorFormula") val colorFormula: String? = null,
  @SerialName("queriesVersion") val queriesVersion: Int? = null,
  @SerialName("queries") val queries: Map<String, FormulaQuery> = emptyMap(),
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
  override val descriptor =
    PrimitiveSerialDescriptor("aktual.LenientYearMonth", PrimitiveKind.STRING)

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

@Serializable(TimeFrameMode.Serializer::class)
enum class TimeFrameMode(override val value: String) : SerializableByString {
  SlidingWindow("sliding-window"),
  Static("static"),
  Full("full"),
  LastMonth("lastMonth"),
  LastYear("lastYear"),
  YearToDate("yearToDate"),
  PriorYearToDate("priorYearToDate");

  object Serializer : KSerializer<TimeFrameMode> by enumStringSerializer()
}

@Serializable(NetWorthMode.Serializer::class)
enum class NetWorthMode(override val value: String) : SerializableByString {
  Trend("trend"),
  Stacked("stacked");

  object Serializer : KSerializer<NetWorthMode> by enumStringSerializer()
}

@Serializable(SpendingMode.Serializer::class)
enum class SpendingMode(override val value: String) : SerializableByString {
  SingleMonth("single-month"),
  Budget("budget"),
  Average("average");

  object Serializer : KSerializer<SpendingMode> by enumStringSerializer()
}

@Serializable(GraphType.Serializer::class)
enum class GraphType(override val value: String) : SerializableByString {
  Line("Line"),
  Bar("Bar");

  object Serializer : KSerializer<GraphType> by enumStringSerializer()
}

@Serializable(TextAlign.Serializer::class)
enum class TextAlign(override val value: String) : SerializableByString {
  Left("left"),
  Right("right"),
  Center("center");

  object Serializer : KSerializer<TextAlign> by enumStringSerializer()
}

@Serializable(FontSizeMode.Serializer::class)
enum class FontSizeMode(override val value: String) : SerializableByString {
  Dynamic("dynamic"),
  Static("static");

  object Serializer : KSerializer<FontSizeMode> by enumStringSerializer()
}
