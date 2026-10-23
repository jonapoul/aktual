package aktual.budget.transactions.domain

/** A draft moved onto a newer version of its transaction, by [rebase]. */
data class RebasedDraft(
  val draft: TransactionDraft,
  // Whether the newer version changed something the draft changed too, which the draft's value
  // overwrites when it's saved
  val hasConflicts: Boolean,
)

/**
 * Moves [draft], started from [base], onto [saved], a newer version of the same transaction made
 * elsewhere, e.g. by a sync. Each field the draft left as it was in [base] takes what's saved, and
 * each one it changed keeps the draft's value, so saving only writes what the user touched. A
 * split's parts are matched by ID: parts added elsewhere are added, and parts deleted elsewhere are
 * dropped.
 */
fun rebase(base: TransactionDraft, saved: TransactionDraft, draft: TransactionDraft): RebasedDraft {
  val merger = Merger()
  val fields = merger.fields(base.fields, saved.fields, draft.fields)
  val parts = merger.parts(base.parts, saved.parts, draft.parts)
  return RebasedDraft(TransactionDraft(fields, parts), merger.hasConflicts)
}

private class Merger {
  var hasConflicts = false

  fun <T> pick(base: T, saved: T, draft: T, same: (T, T) -> Boolean = { a, b -> a == b }): T =
    when {
      same(draft, base) -> saved
      !same(saved, base) && !same(saved, draft) -> draft.also { hasConflicts = true }
      else -> draft
    }

  fun fields(base: TransactionFields, saved: TransactionFields, draft: TransactionFields) =
    TransactionFields(
      account = pick(base.account, saved.account, draft.account),
      date = pick(base.date, saved.date, draft.date),
      amount = pick(base.amount, saved.amount, draft.amount),
      payee = pick(base.payee, saved.payee, draft.payee),
      category = pick(base.category, saved.category, draft.category),
      notes = pick(base.notes, saved.notes, draft.notes, ::sameNotes),
      cleared = pick(base.cleared, saved.cleared, draft.cleared),
      reconciled = pick(base.reconciled, saved.reconciled, draft.reconciled),
    )

  fun parts(
    base: List<SplitPart>,
    saved: List<SplitPart>,
    draft: List<SplitPart>,
  ): List<SplitPart> {
    if (draft == base) return saved
    if (saved == base) return draft

    val baseParts = base.associateBy { it.id }
    val savedParts = saved.associateBy { it.id }
    val draftIds = draft.map { it.id }.toSet()
    val kept = draft.mapNotNull { part ->
      val old = baseParts[part.id] ?: return@mapNotNull part
      val new = savedParts[part.id]
      // A part deleted elsewhere can't be saved again under its ID, so it's lost
      if (new == null) {
        if (part != old) hasConflicts = true
        null
      } else {
        part(old, new, part)
      }
    }

    // Removing a part throws away what was changed in it elsewhere
    if (
      base.any { old -> old.id !in draftIds && savedParts[old.id].let { it != null && it != old } }
    ) {
      hasConflicts = true
    }

    // Parts added elsewhere join the draft's, unless it's no longer split or added its own, since
    // then the two sets of parts wouldn't add up
    val added = saved.filter { it.id !in baseParts }
    return when {
      added.isEmpty() -> kept
      draft.isEmpty() || draft.any { it.id !in baseParts } -> kept.also { hasConflicts = true }
      else -> kept + added
    }
  }

  private fun part(base: SplitPart, saved: SplitPart, draft: SplitPart) =
    SplitPart(
      id = draft.id,
      amount = pick(base.amount, saved.amount, draft.amount),
      payee = pick(base.payee, saved.payee, draft.payee),
      category = pick(base.category, saved.category, draft.category),
      notes = pick(base.notes, saved.notes, draft.notes, ::sameNotes),
      sortOrder = pick(base.sortOrder, saved.sortOrder, draft.sortOrder),
    )

  // Empty notes are saved as null
  private fun sameNotes(a: String?, b: String?) = a.orEmpty() == b.orEmpty()
}
