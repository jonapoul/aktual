package aktual.budget.reports.vm

import aktual.budget.db.DbJson
import aktual.budget.model.Amount
import aktual.budget.model.Condition
import aktual.budget.model.ConditionOp
import aktual.core.model.Percent
import kotlin.math.roundToLong
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

// packages/desktop-client/src/components/reports/spreadsheets/summary-spreadsheet.ts

// SummaryWidget.meta.content, stored as a JSON string
@Serializable
internal data class SummaryContent(
  @SerialName("type") val type: SummaryChartType = Sum,
  @SerialName("divisorConditions") val divisorConditions: List<Condition>? = null,
  @SerialName("divisorConditionsOp") val divisorConditionsOp: ConditionOp? = null,
  @SerialName("divisorAllTimeDateRange") val divisorAllTimeDateRange: Boolean? = null,
)

private val summaryJson = Json { ignoreUnknownKeys = true }

// Upstream falls back to a sum when the content is missing or unreadable
internal fun parseSummaryContent(raw: String?): SummaryContent =
  raw
    ?.let { runCatching { summaryJson.decodeFromString(SummaryContent.serializer(), it) } }
    ?.getOrNull() ?: SummaryContent()

val SummaryReportMeta.summaryType: SummaryChartType
  get() = parseSummaryContent(content).type

val SummaryReportMeta.allTimeDivisor: Boolean
  get() = parseSummaryContent(content).divisorAllTimeDateRange == true

fun SummaryReportMeta.withSummaryType(type: SummaryChartType): SummaryReportMeta =
  withContent("type" to DbJson.encodeToJsonElement(SummaryChartType.serializer(), type))

fun SummaryReportMeta.withAllTimeDivisor(allTime: Boolean): SummaryReportMeta =
  withContent("divisorAllTimeDateRange" to JsonPrimitive(allTime))

// The content is a JSON string inside the meta, so keep its other values. Unreadable content is
// replaced, like upstream falling back to a default when parsing fails
private fun SummaryReportMeta.withContent(value: Pair<String, JsonElement>): SummaryReportMeta {
  val current =
    content?.let { runCatching { DbJson.parseToJsonElement(it) as? JsonObject }.getOrNull() }
      ?: JsonObject(emptyMap())
  return copy(content = DbJson.encodeToString(JsonObject(current + value)))
}

// Summaries whose content is written differently but reads the same show the same report
internal fun ReportMeta.isSameAs(other: ReportMeta): Boolean =
  if (this is SummaryReportMeta && other is SummaryReportMeta) {
    copy(content = null) == other.copy(content = null) &&
      parseSummaryContent(content) == parseSummaryContent(other.content)
  } else {
    this == other
  }

internal data class SummaryParams(
  val title: String,
  val start: YearMonth,
  val end: YearMonth,
  val content: SummaryContent,
)

private const val MONTHS_PER_YEAR = 12f
private const val PERCENT_FACTOR = 10_000.0
private const val PERCENT_DIVISOR = 100.0

/**
 * [amounts] are the filtered transactions in the range, [divisorAmounts] the ones matching the
 * divisor's conditions, only read for a percentage.
 */
internal fun calculateSummary(
  params: SummaryParams,
  amounts: List<Long>,
  divisorAmounts: List<Long>,
): SummaryData {
  val start = params.start.firstDay
  val end = params.end.lastDay
  val total = amounts.sum()
  return when (params.content.type) {
    Sum -> {
      SummaryData.Sum(params.title, start, end, Amount(total))
    }

    AveragePerMonth -> {
      val months = months(params.start, params.end)
      SummaryData.AveragePerMonth(
        title = params.title,
        start = start,
        end = end,
        numMonths = months.toFloat(),
        total = Amount(total),
        average = average(total, months.toDouble()),
      )
    }

    AveragePerYear -> {
      val years = months(params.start, params.end) / MONTHS_PER_YEAR
      SummaryData.AveragePerYear(
        title = params.title,
        start = start,
        end = end,
        numYears = years,
        total = Amount(total),
        average = average(total, years.toDouble()),
      )
    }

    AveragePerTransaction -> {
      SummaryData.AveragePerTransaction(
        title = params.title,
        start = start,
        end = end,
        numTransactions = amounts.size,
        total = Amount(total),
        average = average(total, amounts.size.toDouble()),
      )
    }

    Percentage -> {
      val denominator = divisorAmounts.sum()
      val percent =
        if (denominator == 0L) {
          Percent.Zero
        } else {
          // Rounded to two decimal places, like upstream
          Percent((total * PERCENT_FACTOR / denominator).roundToLong() / PERCENT_DIVISOR)
        }
      SummaryData.Percentage(
        title = params.title,
        start = start,
        end = end,
        numerator = Amount(total),
        denominator = Amount(denominator),
        percent = percent,
        divisor =
          if (params.content.divisorAllTimeDateRange == true) {
            PercentageDivisor.AllTime
          } else {
            PercentageDivisor.Specific(start, end)
          },
      )
    }
  }
}

private fun months(start: YearMonth, end: YearMonth): Int = (start..end).count()

private fun average(total: Long, count: Double): Amount =
  if (count > 0) Amount((total / count).roundToLong()) else Zero

internal val SummaryContent.divisorOp: ConditionOp
  get() = divisorConditionsOp?.takeIf { it != Unknown } ?: And

// Used for an all-time divisor
internal val ALL_TIME_START = LocalDate(1, 1, 1)
internal val ALL_TIME_END = LocalDate(9999, 12, 31)
