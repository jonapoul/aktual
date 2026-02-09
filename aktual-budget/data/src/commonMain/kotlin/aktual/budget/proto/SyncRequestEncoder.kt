package aktual.budget.proto

import aktual.budget.model.BudgetId
import aktual.budget.model.Message
import aktual.budget.model.Timestamp
import okio.ByteString

fun interface SyncRequestEncoder {
  suspend operator fun invoke(
      groupId: String,
      budgetId: BudgetId,
      since: Timestamp,
      messages: List<Message>,
  ): ByteString
}
