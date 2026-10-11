package aktual.budget.transactions.domain

import aktual.budget.model.Amount
import aktual.budget.model.TransactionId

// The split editing of packages/loot-core/src/shared/transactions.ts, on the one transaction its
// editor holds rather than a list of them. A part's account, date, cleared and reconciled aren't
// kept on it, so the parent's reach it when the draft is saved

/**
 * splitTransaction(): the payee moves to the parts, which start with no amount. [count] is how many
 * to make, two for the makeEmptySplitSubtransactions() that upstream's mobile editor splits with.
 */
fun TransactionDraft.split(newId: () -> TransactionId, count: Int = 1): TransactionDraft {
  if (isSplit) return this
  val made =
    List(count) { index ->
      SplitPart(
        id = newId(),
        payee = fields.payee,
        category = fields.category,
        sortOrder = -(index + 1L),
      )
    }
  return copy(fields = fields.copy(payee = null), parts = made)
}

// addSplitTransaction(): an empty part after the last one, with its payee
fun TransactionDraft.addPart(newId: () -> TransactionId): TransactionDraft {
  val previous = parts.lastOrNull() ?: return this
  val part =
    SplitPart(
      id = newId(),
      payee = previous.payee ?: fields.payee,
      category = fields.category,
      sortOrder = (previous.sortOrder ?: 0) - 1,
    )
  return copy(parts = parts + part)
}

/**
 * updateTransaction() on the parent. A part that had the parent's payee follows it to the new one,
 * and as makeChild() has it, a part with no payee takes the parent's.
 */
fun TransactionDraft.withFields(updated: TransactionFields): TransactionDraft =
  copy(
    fields = updated,
    parts =
      parts.map { part ->
        val payee = if (part.payee == fields.payee) updated.payee else part.payee
        part.copy(payee = payee ?: updated.payee)
      },
  )

// updateTransaction() on a child
fun TransactionDraft.withPart(updated: SplitPart): TransactionDraft =
  copy(parts = parts.map { if (it.id == updated.id) updated else it })

// deleteTransaction() on a child. Without its last part the parent stops being a split
fun TransactionDraft.withoutPart(id: TransactionId): TransactionDraft =
  when {
    parts.none { it.id == id } -> this
    parts.size == 1 -> copy(parts = emptyList())
    else -> copy(parts = parts.filterNot { it.id == id })
  }

/**
 * recalculateSplit(): the difference of its SplitTransactionError, which is what the parent's
 * amount has over the sum of its parts. Null if they add up, or this isn't a split.
 */
val TransactionDraft.splitDifference: Amount?
  get() {
    if (!isSplit) return null
    val total = parts.fold(Amount.Zero) { sum, part -> sum + part.amount }
    return (fields.amount - total).takeIf { it != Zero }
  }

// Puts what's left to assign on one part, so the split adds up
fun TransactionDraft.fillRemaining(id: TransactionId): TransactionDraft {
  val difference = splitDifference ?: return this
  val part = parts.firstOrNull { it.id == id } ?: return this
  return withPart(part.copy(amount = part.amount + difference))
}
