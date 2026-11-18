package aktual.budget.transactions.domain

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put

/**
 * One column of upstream's transaction table, as its column manager saves them in the
 * transaction-table-columns synced prefs. See
 * packages/desktop-client/src/components/transactions/table/columns.ts
 */
internal data class TransactionTableColumn(val id: String, val hidden: Boolean)

internal const val BALANCE_COLUMN = "balance"

// In their default order
private val COLUMN_IDS =
  listOf(
    "date",
    "account",
    "payee",
    "notes",
    "group",
    "category",
    "payment",
    "deposit",
    BALANCE_COLUMN,
    "cleared",
  )

// The running balance and category group are opt in
private fun isHiddenByDefault(id: String) = id == BALANCE_COLUMN || id == "group"

/**
 * parseTransactionTableColumns(): unknown and repeated columns are dropped, and missing ones are
 * put back at their default position. Malformed JSON gives the defaults
 */
internal fun parseTransactionTableColumns(raw: String?): List<TransactionTableColumn> {
  val saved = savedColumns(raw).toMutableList()

  // New transactions need an amount, so at least one of these stays visible
  val payment = saved.indexOfFirst { it.id == "payment" }
  val deposit = saved.indexOfFirst { it.id == "deposit" }
  if (payment >= 0 && deposit >= 0 && saved[payment].hidden && saved[deposit].hidden) {
    saved[payment] = saved[payment].copy(hidden = false)
    saved[deposit] = saved[deposit].copy(hidden = false)
  }

  for ((index, id) in COLUMN_IDS.withIndex()) {
    if (saved.any { it.id == id }) continue
    val column = TransactionTableColumn(id, isHiddenByDefault(id))
    val insertAt = saved.indexOfFirst { COLUMN_IDS.indexOf(it.id) > index }
    if (insertAt == -1) saved.add(column) else saved.add(insertAt, column)
  }
  return saved
}

internal fun List<TransactionTableColumn>.encode(): String = buildJsonArray {
  for (column in this@encode) {
    add(
      buildJsonObject {
        put("id", column.id)
        put("hidden", column.hidden)
      },
    )
  }
}
  .toString()

private fun savedColumns(raw: String?): List<TransactionTableColumn> {
  if (raw.isNullOrEmpty()) return emptyList()
  val array =
    try {
      Json.parseToJsonElement(raw) as? JsonArray
    } catch (_: SerializationException) {
      null
    }
  val saved = mutableListOf<TransactionTableColumn>()
  for (entry in array.orEmpty()) {
    val obj = entry as? JsonObject ?: continue
    val id = (obj["id"] as? JsonPrimitive)?.contentOrNull ?: continue
    if (id !in COLUMN_IDS || saved.any { it.id == id }) continue
    // The date drives editing, so upstream never hides it
    val hidden = (obj["hidden"] as? JsonPrimitive)?.booleanOrNull == true && id != "date"
    saved.add(TransactionTableColumn(id, hidden))
  }
  return saved
}
