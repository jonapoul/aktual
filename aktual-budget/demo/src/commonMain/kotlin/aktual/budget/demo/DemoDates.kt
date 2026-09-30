package aktual.budget.demo

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive

private const val MILLIS_PER_DAY = 86_400_000L
private const val MONTHS_PER_YEAR = 12

/**
 * Upstream generates the demo relative to the current date, so move the bundled copy forward by the
 * time since it was generated. Day-level dates shift by days, budget months by whole months.
 */
internal suspend fun SqlDriver.shiftDemoDates(from: LocalDate, to: LocalDate) {
  val days = from.daysUntil(to)
  val months = to.monthIndex() - from.monthIndex()
  if (days == 0 && months == 0) return

  val statements =
    listOf(
      "UPDATE transactions SET date = ${shiftDay("date", days)} WHERE date IS NOT NULL",
      """
      UPDATE schedules_next_date SET
        local_next_date = ${shiftDay("local_next_date", days)},
        base_next_date = ${shiftDay("base_next_date", days)},
        local_next_date_ts = local_next_date_ts + ${days * MILLIS_PER_DAY},
        base_next_date_ts = base_next_date_ts + ${days * MILLIS_PER_DAY}
      """
        .trimIndent(),
    ) +
      shiftBudgetMonths(table = "zero_budgets", months) +
      shiftBudgetMonths(table = "reflect_budgets", months) +
      shiftIds(
        table = "zero_budget_months",
        newId = "strftime('%Y-%m', id || '-01', '$months months')",
      )

  // No transaction needed: this runs on a throwaway copy, which gets replaced on the next open
  statements.forEach { sql -> execute(identifier = null, sql = sql, parameters = 0).await() }
  shiftRuleDates(DatePeriod(days = days))
}

// Upstream generates transactions up to the current day, so the latest one marks when it ran
internal suspend fun SqlDriver.demoGeneratedOn(): LocalDate {
  val date =
    executeQuery(
        identifier = null,
        sql = "SELECT max(date) FROM transactions",
        mapper = { cursor ->
          QueryResult.AsyncValue {
            cursor.next().await()
            cursor.getLong(0)
          }
        },
        parameters = 0,
      )
      .await()
  return LocalDate.Formats.ISO_BASIC.parse(
    requireNotNull(date) { "No demo transactions" }.toString()
  )
}

private fun LocalDate.monthIndex(): Int = year * MONTHS_PER_YEAR + month.ordinal

// Dates stored as YYYYMMDD integers
private fun shiftDay(column: String, days: Int): String {
  val iso = "printf('%04d-%02d-%02d', $column / 10000, $column / 100 % 100, $column % 100)"
  return "CAST(strftime('%Y%m%d', $iso, '$days days') AS INTEGER)"
}

// Month stored as a YYYYMM integer, and at the start of the ID as "YYYYMM-categoryId"
private fun shiftBudgetMonths(table: String, months: Int): List<String> {
  val newMonth =
    "strftime('%Y%m', printf('%04d-%02d-01', month / 100, month % 100), '$months months')"
  return listOf("UPDATE $table SET month = CAST($newMonth AS INTEGER)") +
    shiftIds(table, newId = "month || substr(id, 7)")
}

// Two passes via a temporary prefix, so a shifted ID never collides with one not yet shifted
private fun shiftIds(table: String, newId: String): List<String> =
  listOf(
    "UPDATE $table SET id = '$TMP_PREFIX' || $newId",
    "UPDATE $table SET id = substr(id, ${TMP_PREFIX.length + 1})",
  )

private const val TMP_PREFIX = "shift:"

// Schedules store their dates inside the JSON conditions of their rule
private suspend fun SqlDriver.shiftRuleDates(period: DatePeriod) {
  val rules =
    executeQuery(
        identifier = null,
        sql = "SELECT id, conditions FROM rules WHERE conditions IS NOT NULL",
        mapper = { cursor ->
          QueryResult.AsyncValue {
            buildList {
              while (cursor.next().await()) {
                add(requireNotNull(cursor.getString(0)) to requireNotNull(cursor.getString(1)))
              }
            }
          }
        },
        parameters = 0,
      )
      .await()

  for ((id, conditions) in rules) {
    val shifted = shiftConditions(conditions, period)
    if (shifted == conditions) continue
    execute(
        identifier = null,
        sql = "UPDATE rules SET conditions = ? WHERE id = ?",
        parameters = 2,
        binders = {
          bindString(0, shifted)
          bindString(1, id)
        },
      )
      .await()
  }
}

internal fun shiftConditions(conditions: String, period: DatePeriod): String {
  val array = Json.parseToJsonElement(conditions) as? JsonArray ?: return conditions
  val shifted = JsonArray(array.map { it.shiftDateCondition(period) })
  return if (shifted == array) conditions else shifted.toString()
}

private fun JsonElement.shiftDateCondition(period: DatePeriod): JsonElement {
  if (this !is JsonObject || this["field"]?.jsonPrimitive?.content != "date") return this
  val value = this["value"] ?: return this
  val shiftedValue =
    when (value) {
      // A one-off date
      is JsonPrimitive -> value.shiftDate(period)
      // A recurring config
      is JsonObject ->
        JsonObject(
          value.mapValues { (key, v) ->
            if (key in RECUR_DATE_KEYS && v is JsonPrimitive) v.shiftDate(period) else v
          }
        )
      is JsonArray -> value
    }
  return JsonObject(this + ("value" to shiftedValue))
}

private val RECUR_DATE_KEYS = setOf("start", "endDate")

private fun JsonPrimitive.shiftDate(period: DatePeriod): JsonPrimitive {
  if (!isString) return this
  val date = runCatching { LocalDate.parse(content) }.getOrNull() ?: return this
  return JsonPrimitive(date.plus(period).toString())
}
