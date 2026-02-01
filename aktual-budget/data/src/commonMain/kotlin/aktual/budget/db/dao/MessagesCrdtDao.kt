package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.db.GetMessagesSince
import aktual.budget.model.Message
import aktual.budget.model.MessageValue
import aktual.budget.model.Timestamp
import app.cash.sqldelight.async.coroutines.awaitAsList
import dev.zacsweers.metro.Inject

@Inject
class MessagesCrdtDao(database: BudgetDatabase) {
  private val queries = database.messagesCrdtQueries

  suspend fun getMessagesSince(timestamp: Timestamp): List<Message> =
    queries
      .getMessagesSince(timestamp)
      .awaitAsList()
      .map(::toMessage)

  private fun toMessage(m: GetMessagesSince) = Message(
    row = m.row,
    column = m.column,
    timestamp = m.timestamp,
    value = MessageValue.decode(m.value_.decodeToString()),
    dataset = m.dataset,
  )
}
