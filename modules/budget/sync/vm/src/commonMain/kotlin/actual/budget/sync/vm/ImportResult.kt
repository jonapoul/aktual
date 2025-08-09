package actual.budget.sync.vm

import actual.budget.model.DbMetadata

sealed interface ImportResult {
  data class Success(val meta: DbMetadata) : ImportResult

  sealed interface Failure : ImportResult
  data object NotZipFile : Failure
  data object InvalidZipFile : Failure
  data object InvalidMetaFile : Failure
  data class OtherFailure(val message: String) : Failure
}
