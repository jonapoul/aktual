package aktual.budget.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonPrimitive

/**
 * A local change to be recorded in the CRDT log. Corresponds to upstream's db.update/insert/delete_
 * in db/index.ts.
 */
data class LocalChange(
  val dataset: String,
  val row: String,
  val column: String,
  val value: MessageValue,
)

fun localChange(
  dataset: String,
  row: String,
  column: String,
  value: String?,
) = LocalChange(dataset, row, column, value?.let(MessageValue::String) ?: MessageValue.Null)

fun localChange(
  dataset: String,
  row: String,
  column: String,
  value: Long,
) = LocalChange(dataset, row, column, MessageValue.Number(value))

fun localChange(
  dataset: String,
  row: String,
  column: String,
  value: Boolean?,
) = LocalChange(dataset, row, column, value.messageValue())

inline fun <reified E : Enum<E>> localChange(
  dataset: String,
  row: String,
  column: String,
  value: E?,
) =
  localChange(
    dataset = dataset,
    row = row,
    column = column,
    value = value?.let { Json.encodeToJsonElement(it).jsonPrimitive.content },
  )

fun tombstone(dataset: String, row: String): LocalChange =
  localChange(dataset, row, column = "tombstone", value = 1)

fun untombstone(dataset: String, row: String): LocalChange =
  localChange(dataset, row, column = "tombstone", value = 0)
