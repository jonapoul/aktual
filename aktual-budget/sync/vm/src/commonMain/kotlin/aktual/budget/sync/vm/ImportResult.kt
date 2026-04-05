package aktual.budget.sync.vm

import aktual.budget.model.DbMetadata

sealed interface ImportResult {
  @JvmInline value class Success(val meta: DbMetadata) : ImportResult

  sealed interface Failure : ImportResult

  data object NotZipFile : Failure

  data object InvalidZipFile : Failure

  data object InvalidMetaFile : Failure

  @JvmInline value class OtherFailure(val message: String) : Failure
}
