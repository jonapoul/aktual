package aktual.budget.rules.domain

import aktual.budget.model.TransactionSearch
import aktual.budget.model.parseAmountInput
import kotlin.math.abs

/**
 * Matches transactions against an account list's search. The text is matched as upstream's
 * transactionsSearch() in packages/loot-core/src/client/queries.ts matches it: found in the payee,
 * its transfer account, the notes, category or account name, or read as an amount and equal to the
 * transaction's either side of zero, or to its whole part when it has no cents. Dates aren't
 * matched. The conditions must match too.
 */
class TransactionSearchMatcher(search: TransactionSearch) {
  private val text = search.text.trim()
  private val filter = TransactionFilter.compile(search.conditions.conditions, search.conditions.op)

  // In cents, unsigned
  private val amount: Long? =
    text.takeIf { AMOUNT.matches(it) }?.let { parseAmountInput(it.trimStart('+', '-')) }?.toLong()

  fun matches(row: FilterRow): Boolean = matchesText(row) && filter.matches(row)

  private fun matchesText(row: FilterRow): Boolean {
    if (text.isEmpty()) return true
    val names =
      listOf(row.payeeName, row.transferAccountName, row.notes, row.categoryName, row.accountName)
    if (names.any { it != null && it.contains(text, ignoreCase = true) }) return true
    val amount = amount ?: return false
    val rowAmount = abs(row.amount.toLong())
    val matchesWhole = amount % CENTS == 0L && rowAmount / CENTS == amount / CENTS
    return rowAmount == amount || matchesWhole
  }

  private companion object {
    val AMOUNT = Regex("""[+-]?\d[\d,]*([.,]\d{0,2})?""")
    const val CENTS = 100L
  }
}
