package aktual.budget.model

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

fun tombstone(dataset: String, row: String): LocalChange =
  LocalChange(dataset, row, column = "tombstone", value = MessageValue.Number(1))

fun untombstone(dataset: String, row: String): LocalChange =
  LocalChange(dataset, row, column = "tombstone", value = MessageValue.Number(0))
