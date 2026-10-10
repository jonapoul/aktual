package aktual.budget.transactions.domain

import aktual.budget.model.TransactionId

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
    )
  return update.takeIf { it != TransactionUpdate(id) }
}

private fun <T> patch(saved: T, draft: T): Patch<T?> = patch(saved, draft) { it }

private inline fun <T, R> patch(saved: T, draft: T, write: (T) -> R): Patch<R> =
  if (draft == saved) Keep else Patch.To(write(draft))
