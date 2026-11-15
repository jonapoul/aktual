package aktual.budget.transactions.domain

// isEmptyRuleTarget() in upstream's transactions/table/utils.ts. Emptying a field is manual input
// too, so rules don't get to fill it back in
fun TransactionFields.isEmpty(field: TransactionField): Boolean =
  when (field) {
    Account -> account == null
    Date -> false
    Amount -> amount == Zero
    Payee -> payee == null
    Category -> category == null
    Notes -> notes.isNullOrEmpty()
    Cleared -> !cleared
  }

/**
 * What [draft] takes from [afterRules], the result of running the rules on it after the user set
 * [updated]. As onUpdate() in upstream's mobile TransactionEdit.tsx, rules only fill fields that
 * are empty or add to the notes, unless the payee changed, which applies everything. Fields the
 * user [emptied] stay empty either way.
 */
fun applyRuleChanges(
  draft: TransactionFields,
  afterRules: TransactionFields,
  updated: TransactionField,
  emptied: Set<TransactionField>,
): TransactionFields {
  fun applies(field: TransactionField): Boolean =
    field !in emptied && (updated == Payee || shouldApply(field, draft, afterRules))
  return draft.copy(
    account = if (applies(Account)) afterRules.account else draft.account,
    date = if (applies(Date)) afterRules.date else draft.date,
    amount = if (applies(Amount)) afterRules.amount else draft.amount,
    payee = if (applies(Payee)) afterRules.payee else draft.payee,
    category = if (applies(Category)) afterRules.category else draft.category,
    notes = if (applies(Notes)) afterRules.notes else draft.notes,
    cleared = if (applies(Cleared)) afterRules.cleared else draft.cleared,
  )
}

// shouldApplyRuleChange() in transactions/table/utils.ts
private fun shouldApply(
  field: TransactionField,
  draft: TransactionFields,
  afterRules: TransactionFields,
): Boolean {
  if (draft.isEmpty(field)) return true
  if (field != Notes) return false
  return addsToNote(current = draft.notes.orEmpty(), next = afterRules.notes.orEmpty())
}

// Whether a rule kept the note and put text before or after it, rather than replacing it. Rules
// run again on every change, so text that an earlier run already added isn't added twice
private fun addsToNote(current: String, next: String): Boolean {
  val index = next.indexOf(current)
  if (next == current || index < 0) return false
  val prepended = next.substring(0, index)
  val appended = next.substring(index + current.length)
  return !(current.startsWith(prepended) && current.endsWith(appended))
}
