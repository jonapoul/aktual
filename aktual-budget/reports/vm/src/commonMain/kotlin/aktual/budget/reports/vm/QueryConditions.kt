package aktual.budget.reports.vm

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap

// Moves a query to a new name, keeping its place in the list
fun ImmutableMap<String, FormulaQuery>.renamed(
  from: String,
  to: String,
): ImmutableMap<String, FormulaQuery> =
  entries.associate { (name, query) -> (if (name == from) to else name) to query }.toImmutableMap()
