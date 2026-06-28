package aktual.budget.model

import aktual.di.AppScope
import alakazam.kotlin.StateHolder
import androidx.compose.runtime.Immutable
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@Immutable
sealed interface SyncState {
  data object Inactive : SyncState

  data object Syncing : SyncState

  sealed interface Error : SyncState

  data object NoToken : Error

  data class SyncFailed(val cause: String) : Error
}

@Inject @SingleIn(AppScope::class) class SyncStateHolder : StateHolder<SyncState>(Inactive)

interface BudgetSyncController {
  suspend fun syncChanges(vararg changes: LocalChange) = syncChanges(changes.toList())

  suspend fun syncChanges(changes: List<LocalChange>)

  fun schedule()
}

/**
 * A local change to be recorded in the CRDT log. Corresponds to upstream's db.update/insert/delete_
 * in db/index.ts.
 */
data class LocalChange(
  val dataset: String,
  val row: String,
  val column: String,
  val value: MessageValue,
)

fun tombstone(dataset: String, row: String): LocalChange =
  LocalChange(dataset, row, column = "tombstone", value = MessageValue.Number(1))

fun untombstone(dataset: String, row: String): LocalChange =
  LocalChange(dataset, row, column = "tombstone", value = MessageValue.Number(0))
