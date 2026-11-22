package aktual.budget.utils

import okio.Path

/** Builds a zip of the open budget in the same format as upstream's "Export data". */
fun interface BudgetExporter {
  suspend fun export(): BudgetExport
}

data class BudgetExport(val path: Path, val fileName: String)
