package aktual.budget.rules.vm.edit

import aktual.budget.model.Amount
import aktual.budget.model.Field
import aktual.budget.model.TransactionId
import aktual.budget.rules.domain.RuleTransaction
import aktual.budget.transactions.domain.RuleMatch
import aktual.budget.transactions.domain.RuleMatches
import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

// The transactions the draft rule matches, and what its actions would do to them
@Immutable
sealed interface RuleMatchesState {
  data object Loading : RuleMatchesState

  // An action can't run, so nothing would change
  data object Invalid : RuleMatchesState

  data object Failed : RuleMatchesState

  data class Loaded(
    val total: Int,
    val changing: Int,
    // Newest first
    val rows: ImmutableList<MatchRow>,
    // Ticked rows, which only include rows that would change
    val selected: ImmutableSet<TransactionId> = persistentSetOf(),
    val isApplying: Boolean = false,
  ) : RuleMatchesState
}

@Immutable
data class MatchRow(
  val id: TransactionId,
  val date: LocalDate,
  val amount: Amount,
  val payee: String?,
  val changes: ImmutableList<MatchChange>,
) {
  val isChanged: Boolean
    get() = changes.isNotEmpty()
}

@Immutable
sealed interface MatchChange {
  // A payee, category, account or notes, by name
  data class Named(val field: Field, val from: String?, val to: String?) : MatchChange

  data class Moved(val from: LocalDate, val to: LocalDate) : MatchChange

  data class Amounted(val from: Amount, val to: Amount) : MatchChange

  @JvmInline value class Cleared(val isCleared: Boolean) : MatchChange

  @JvmInline value class SplitInto(val parts: Int) : MatchChange

  data object Deleted : MatchChange
}

// Only counts rows with a change to show, since only they can be ticked
internal fun RuleMatches.toState(): RuleMatchesState.Loaded {
  val rows = matches.map { it.toRow(names) }
  return RuleMatchesState.Loaded(
    total = matches.size,
    changing = rows.count { it.isChanged },
    rows = rows.toImmutableList(),
  )
}

private fun RuleMatch.toRow(names: Map<String, String>): MatchRow {
  fun name(id: String?) = id?.let(names::get)
  return MatchRow(
    id = requireNotNull(before.id),
    date = before.date,
    amount = before.amount,
    payee = name(after.payee?.value) ?: name(before.payee?.value) ?: before.importedPayee,
    changes = changes(before, after, ::name).toImmutableList(),
  )
}

private fun changes(
  before: RuleTransaction,
  after: RuleTransaction,
  name: (String?) -> String?,
): List<MatchChange> = buildList {
  if (after.tombstone) {
    add(Deleted)
    return@buildList
  }
  fun named(field: Field, from: String?, to: String?) {
    if (from != to) add(MatchChange.Named(field, from, to))
  }
  named(Payee, name(before.payee?.value), name(after.payee?.value))
  named(Category, name(before.category?.value), name(after.category?.value))
  named(Account, name(before.account.value), name(after.account.value))
  named(Notes, before.notes, after.notes)
  if (before.date != after.date) add(MatchChange.Moved(before.date, after.date))
  if (before.amount != after.amount) add(MatchChange.Amounted(before.amount, after.amount))
  if (before.cleared != after.cleared) add(MatchChange.Cleared(after.cleared))
  if (after.subtransactions.isNotEmpty()) add(MatchChange.SplitInto(after.subtransactions.size))
}
