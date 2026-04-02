package aktual.budget.reports.vm

import aktual.budget.model.Condition
import aktual.budget.model.CustomReportId
import aktual.budget.model.Interval
import aktual.budget.model.WidgetType
import alakazam.kotlin.SerializableByString
import alakazam.kotlin.enumStringSerializer
import androidx.compose.runtime.Immutable
import kotlinx.datetime.YearMonth
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

@Immutable
@Serializable
data class CustomReportMeta(@SerialName("id") val id: CustomReportId) : ReportMeta

@Immutable
@Serializable
data class NetWorthReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: Condition.Op? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("interval") val interval: Interval? = null,
  @SerialName("mode") val mode: NetWorthMode? = null,
) : ReportMeta

@Immutable
@Serializable
data class CashFlowReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: Condition.Op? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("showBalance") val showBalance: Boolean? = null,
) : ReportMeta

@Immutable
@Serializable
data class SpendingReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: Condition.Op? = null,
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
  @SerialName("conditionsOp") val conditionsOp: Condition.Op? = null,
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
  @SerialName("conditionsOp") val conditionsOp: Condition.Op? = null,
  @SerialName("timeFrame") val timeFrame: TimeFrame? = null,
  @SerialName("content") val content: String? = null,
) : ReportMeta

@Immutable
@Serializable
data class CalendarReportMeta(
  @SerialName("name") val name: String? = null,
  @SerialName("conditions") val conditions: List<Condition>? = null,
  @SerialName("conditionsOp") val conditionsOp: Condition.Op? = null,
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
  @SerialName("conditionsOp") val conditionsOp: Condition.Op?,
  @SerialName("timeFrame") val timeFrame: TimeFrame?,
)

@Immutable
@Serializable
data class TimeFrame(
  @SerialName("start") val start: YearMonth,
  @SerialName("end") val end: YearMonth,
  @SerialName("mode") val mode: TimeFrameMode,
)

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
