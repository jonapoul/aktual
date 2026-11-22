package aktual.budget.utils

import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import okio.Path

/** Unpacks a zip in Actual's export format into the budget directory for [id]. */
fun interface BudgetImporter {
  /** [updateMetadata] adjusts the zip's metadata before it's written. */
  suspend fun import(
    id: BudgetId,
    zipPath: Path,
    updateMetadata: (DbMetadata) -> DbMetadata,
  ): ImportResult
}

sealed interface ImportResult {
  @JvmInline value class Success(val meta: DbMetadata) : ImportResult

  sealed interface Failure : ImportResult

  data object NotZipFile : Failure

  data object InvalidZipFile : Failure

  data object InvalidMetaFile : Failure

  @JvmInline value class OtherFailure(val message: String) : Failure
}
