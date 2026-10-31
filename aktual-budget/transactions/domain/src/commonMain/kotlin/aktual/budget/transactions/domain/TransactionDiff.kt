package aktual.budget.transactions.domain

import aktual.budget.model.AccountId
import aktual.budget.model.Amount
import aktual.budget.model.TransactionId
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * What saving [draft] over [saved] writes, or null if nothing differs. As diffItems() in
 * packages/loot-core/src/shared/util.ts, only the fields that changed are sent.
 */
fun transactionDiff(
  id: TransactionId,
  saved: TransactionFields,
  draft: TransactionFields,
): TransactionUpdate? {
  val update =
    TransactionUpdate(
      id = id,
      account = draft.account.takeIf { it != saved.account },
      date = draft.date.takeIf { it != saved.date },
      amount = draft.amount.takeIf { it != saved.amount },
      cleared = draft.cleared.takeIf { it != saved.cleared },
      reconciled = draft.reconciled.takeIf { it != saved.reconciled },
      payee = patch(saved.payee, draft.payee),
      category = patch(saved.category, draft.category),
      notes = patch(saved.notes.orEmpty(), draft.notes.orEmpty()) { it.ifEmpty { null } },
      schedule = patch(saved.schedule, draft.schedule),
    )
  return update.takeIf { it != TransactionUpdate(id) }
}

/**
 * The same for a transaction with its split's parts: the parent picks up isParent and its split
 * error, and its parts are added, updated or deleted. A part that stays also takes what changed of
 * the parent's account, date, cleared and reconciled, as upstream's makeChild() gives it them.
 */
fun transactionDiff(
  id: TransactionId,
  saved: TransactionDraft,
  draft: TransactionDraft,
): TransactionChanges? {
  val fields = transactionDiff(id, saved.fields, draft.fields) ?: TransactionUpdate(id)
  val parent =
    fields.copy(
      isParent = draft.isSplit.takeIf { it != saved.isSplit },
      error = patch(saved.splitDifference, draft.splitDifference) { it?.let(::splitError) },
    )

  val savedParts = saved.parts.associateBy { it.id }
  val draftIds = draft.parts.map { it.id }.toSet()
  val added = draft.parts.filter { it.id !in savedParts }
  val updated =
    draft.parts.mapNotNull { part ->
      val old = savedParts[part.id] ?: return@mapNotNull null
      val update =
        TransactionUpdate(
          id = part.id,
          account = fields.account,
          date = fields.date,
          amount = part.amount.takeIf { it != old.amount },
          cleared = fields.cleared,
          reconciled = fields.reconciled,
          payee = patch(old.payee, part.payee),
          category = patch(old.category, part.category),
          notes = patch(old.notes.orEmpty(), part.notes.orEmpty()) { it.ifEmpty { null } },
        )
      update.takeIf { it != TransactionUpdate(part.id) }
    }

  val changes =
    TransactionChanges(
      added =
        if (added.isEmpty()) {
          emptyList()
        } else {
          val account = requireNotNull(draft.fields.account) { "A split's parts need an account" }
          added.map { it.toNewTransaction(id, account, draft.fields) }
        },
      updated = listOfNotNull(parent.takeIf { it != TransactionUpdate(id) }) + updated,
      deleted = saved.parts.map { it.id }.filterNot { it in draftIds },
    )
  return changes.takeIf { it != TransactionChanges() }
}

// makeChild(): a part to insert under its parent
fun SplitPart.toNewTransaction(
  parent: TransactionId,
  account: AccountId,
  fields: TransactionFields,
): NewTransaction =
  NewTransaction(
    account = account,
    date = fields.date,
    amount = amount,
    payee = payee,
    category = category,
    notes = notes?.ifEmpty { null },
    cleared = fields.cleared,
    reconciled = fields.reconciled,
    parentId = parent,
    sortOrder = sortOrder,
    id = id,
  )

// SplitTransactionError() in packages/loot-core/src/shared/transactions.ts
fun splitError(difference: Amount): JsonObject = buildJsonObject {
  put("type", "SplitTransactionError")
  put("version", 1)
  put("difference", difference.toLong())
}

private fun <T> patch(saved: T, draft: T): Patch<T?> = patch(saved, draft) { it }

private inline fun <T, R> patch(saved: T, draft: T, write: (T) -> R): Patch<R> =
  if (draft == saved) Keep else Patch.To(write(draft))
