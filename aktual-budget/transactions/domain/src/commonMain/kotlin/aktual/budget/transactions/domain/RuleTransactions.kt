package aktual.budget.transactions.domain

import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.RuleTransaction
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * The rows to insert for a new transaction that the rules ran on, with makeSplitTransaction() in
 * packages/loot-core/src/shared/transactions.ts for one a rule split. The parent comes first.
 */
fun RuleTransaction.toNewTransactions(
  newId: () -> TransactionId,
  importedId: String? = null,
  rawSyncedData: String? = null,
): List<NewTransaction> {
  val children = subtransactions
  val isSplit = children.isNotEmpty()
  val id = newId()
  val difference = amount.toLong() - children.sumOf { it.amount.toLong() }
  val parent =
    NewTransaction(
      id = id,
      account = account,
      date = date,
      amount = amount,
      payee = payee,
      category = category,
      notes = notes,
      importedId = importedId,
      importedPayee = importedPayee,
      cleared = cleared,
      reconciled = reconciled,
      schedule = schedule,
      isParent = isSplit,
      error = if (isSplit && difference != 0L) splitError(difference) else null,
      rawSyncedData = rawSyncedData,
    )
  return listOf(parent) +
    children.mapIndexed { index, child ->
      NewTransaction(
        id = newId(),
        account = account,
        date = date,
        amount = child.amount,
        payee = child.payee,
        category = child.category,
        notes = child.notes,
        cleared = cleared,
        reconciled = reconciled,
        parentId = id,
        sortOrder = -index.toLong(),
      )
    }
}

private fun splitError(difference: Long): JsonObject = buildJsonObject {
  put("type", "SplitTransactionError")
  put("version", 1)
  put("difference", difference)
}
