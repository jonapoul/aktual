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
  val isSplit = subtransactions.isNotEmpty()
  val id = newId()
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
      error = splitError(),
      rawSyncedData = rawSyncedData,
    )
  return listOf(parent) + newChildren(id, newId)
}

// The rows to insert for the children a rule split this into
internal fun RuleTransaction.newChildren(
  parent: TransactionId,
  newId: () -> TransactionId,
): List<NewTransaction> = subtransactions.mapIndexed { index, child ->
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
    parentId = parent,
    sortOrder = -index.toLong(),
  )
}

// Null unless a rule split this and the children don't add up to it
internal fun RuleTransaction.splitError(): JsonObject? {
  if (subtransactions.isEmpty()) return null
  val difference = amount.toLong() - subtransactions.sumOf { it.amount.toLong() }
  return if (difference == 0L) null else splitError(difference)
}

private fun splitError(difference: Long): JsonObject = buildJsonObject {
  put("type", "SplitTransactionError")
  put("version", 1)
  put("difference", difference)
}
