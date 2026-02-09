package aktual.budget.proto

import aktual.budget.model.DbMetadata
import aktual.budget.model.SyncResponse
import okio.Source

fun interface SyncResponseDecoder {
  suspend operator fun invoke(
      source: Source,
      metadata: DbMetadata,
  ): SyncResponse
}
