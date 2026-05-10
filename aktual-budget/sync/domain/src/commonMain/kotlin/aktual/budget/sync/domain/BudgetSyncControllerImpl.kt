package aktual.budget.sync.domain

import aktual.budget.db.dao.SyncDao
import aktual.budget.model.BudgetSyncController
import aktual.budget.model.LocalChange
import aktual.budget.model.SyncState
import aktual.budget.model.SyncState.Inactive
import aktual.budget.model.SyncState.NoToken
import aktual.budget.model.SyncState.SyncFailed
import aktual.budget.model.SyncState.Syncing
import aktual.budget.model.SyncStateHolder
import aktual.di.BudgetScope
import aktual.di.Closeable
import aktual.prefs.AppPreferences
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logcat.logcat

@SingleIn(BudgetScope::class)
@ContributesBinding(BudgetScope::class, binding<BudgetSyncController>())
@ContributesIntoSet(BudgetScope::class, binding<Closeable>())
class BudgetSyncControllerImpl
internal constructor(
  private val syncStateHolder: SyncStateHolder,
  private val syncer: IncrementalSyncer,
  private val scope: CoroutineScope,
  private val prefs: AppPreferences,
  private val syncDao: SyncDao,
) : BudgetSyncController, Closeable {
  private var syncJob: Job? = null
  private var inactiveJob: Job? = null

  override suspend fun syncChanges(vararg changes: LocalChange) = syncChanges(changes.toList())

  override suspend fun syncChanges(changes: List<LocalChange>) {
    syncDao.sendMessages(changes)
    schedule()
  }

  override fun schedule() {
    synchronized(this) {
      inactiveJob?.cancel()
      syncJob?.cancel()
      syncJob = scope.launch { scheduleSync() }
    }
  }

  override fun close() {
    synchronized(this) {
      syncJob?.cancel()
      syncJob = null
      inactiveJob?.cancel()
      inactiveJob = null
    }
    update(Inactive)
  }

  private suspend fun scheduleSync() {
    val token = prefs.token.get()
    if (token == null) {
      update(NoToken)
      scheduleInactive()
      return
    }

    update(Syncing)
    when (val result = syncer.sync()) {
      is SyncResult.Success -> update(Inactive)
      is SyncResult.Error -> update(SyncFailed(result.toString()))
    }
    scheduleInactive()
  }

  private fun update(state: SyncState) {
    logcat.d { "Updating state to $state" }
    syncStateHolder.update { state }
  }

  private fun scheduleInactive() {
    inactiveJob?.cancel()
    inactiveJob = scope.launch {
      delay(INACTIVE_DELAY)
      update(Inactive)
    }
  }

  private companion object {
    val INACTIVE_DELAY = 2.seconds
  }
}
