@file:Suppress("MagicNumber")

package aktual.budget.reports.vm.dashboard

import aktual.budget.model.DateRangeType
import aktual.budget.model.ResolvedDateRange
import aktual.budget.model.resolve
import aktual.budget.reports.vm.SummaryChartType
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.todayIn
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

internal data class WidgetConfig(
  val name: String?,
  val dateRange: ResolvedDateRange,
  val markdownContent: String?,
  val summaryType: SummaryChartType,
)

internal fun parseWidgetConfig(
  meta: JsonObject?,
  today: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
): WidgetConfig {
  val name = meta?.string("name")
  val dateRange =
    parseTimeFrame(meta?.get("timeFrame") as? JsonObject, today)
      ?: DateRangeType.Last6Months.resolve(today)

  val markdownContent = meta?.string("content")

  val summaryContent =
    when (val element = meta?.get("content")) {
      is JsonPrimitive -> parseObjectAsString(element)
      is JsonObject -> element
      is JsonArray,
      null -> null
    }

  val summaryType = parseSummaryType(summaryContent)

  return WidgetConfig(name, dateRange, markdownContent, summaryType)
}

private fun parseObjectAsString(primitive: JsonPrimitive): JsonObject? =
  if (primitive.isString) {
    try {
      Json.parseToJsonElement(primitive.content) as? JsonObject
    } catch (_: SerializationException) {
      null
    }
  } else {
    null
  }

private fun parseTimeFrame(timeFrame: JsonObject?, today: LocalDate): ResolvedDateRange? {
  if (timeFrame == null) return null

  val mode = timeFrame.string("mode")
  val startStr = timeFrame.string("start")
  val endStr = timeFrame.string("end")

  // Match named date range types (e.g. "Last6Months", "yearToDate")
  if (mode != null) {
    val rangeType = DateRangeType.entries.find { it.value == mode || it.name == mode }
    if (rangeType != null) return rangeType.resolve(today)
  }

  if (startStr != null && endStr != null) {
    if (mode == "sliding-window") {
      return parseSlidingWindow(startStr, endStr, today)
    }
    // Static mode: parse as literal dates (YYYY-MM-DD or YYYY-MM)
    return parseStaticRange(startStr, endStr)
  }

  return null
}

/** Sliding window: start/end encode a window length (in months) anchored to today. */
private fun parseSlidingWindow(
  startStr: String,
  endStr: String,
  today: LocalDate,
): ResolvedDateRange? {
  val startYm = parseYearMonth(startStr) ?: return null
  val endYm = parseYearMonth(endStr) ?: return null
  val offsetMonths = (endYm.year - startYm.year) * 12 + (endYm.month.number - startYm.month.number)
  val liveStart = LocalDate(today.year, today.month, 1).minus(offsetMonths, DateTimeUnit.MONTH)
  return ResolvedDateRange(liveStart, today)
}

private fun parseStaticRange(startStr: String, endStr: String): ResolvedDateRange? {
  try {
    return ResolvedDateRange(LocalDate.parse(startStr), LocalDate.parse(endStr))
  } catch (_: Exception) {
    // Try YYYY-MM format
  }
  val startYm = parseYearMonth(startStr) ?: return null
  val endYm = parseYearMonth(endStr) ?: return null
  val start = LocalDate(startYm.year, startYm.month, 1)
  return ResolvedDateRange(start, lastDayOfMonth(endYm.year, endYm.month.number))
}

private fun lastDayOfMonth(year: Int, month: Int): LocalDate {
  val nextMonth = if (month == 12) LocalDate(year + 1, 1, 1) else LocalDate(year, month + 1, 1)
  return nextMonth.minus(1, DateTimeUnit.DAY)
}

/** Parse "YYYY-MM" to year and month. */
private fun parseYearMonth(str: String): YearMonth? {
  val parts = str.split("-")
  if (parts.size < 2) return null
  val year = parts[0].toIntOrNull() ?: return null
  val month = parts[1].toIntOrNull() ?: return null
  return YearMonth(year, Month(month))
}

private fun parseSummaryType(config: JsonObject?): SummaryChartType =
  when (config?.string("type")) {
    "avgPerMonth" -> SummaryChartType.AveragePerMonth
    "avgPerYear" -> SummaryChartType.AveragePerYear
    "avgPerTransact" -> SummaryChartType.AveragePerTransaction
    "percentage" -> SummaryChartType.Percentage
    else -> SummaryChartType.Sum
  }

private fun JsonObject.string(key: String): String? =
  (get(key) as? JsonPrimitive)?.takeIf { it.isString }?.content?.takeIf { it.isNotBlank() }
