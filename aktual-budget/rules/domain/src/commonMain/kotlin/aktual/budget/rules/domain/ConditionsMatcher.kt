package aktual.budget.rules.domain

import aktual.budget.model.ConditionsFilter
import aktual.budget.model.ExactTags
import aktual.budget.model.tagNamesInNotes

/** Matches transactions against a [ConditionsFilter] */
class ConditionsMatcher(filter: ConditionsFilter) {
  private val sets = filter.sets.map { TransactionFilter.compile(it.conditions, it.op) }
  private val tags = filter.tags

  fun matches(row: FilterRow): Boolean =
    sets.all { it.matches(row) } && (tags == null || tags.matches(row.notes))

  // tagGroups.ts groupQueryDataByTags(), as reports bucket transactions by tag
  private fun ExactTags.matches(notes: String?): Boolean {
    val found = tagNamesInNotes(notes.orEmpty()).filterTo(mutableSetOf()) { it in scope }
    return found == names.toSet()
  }
}
