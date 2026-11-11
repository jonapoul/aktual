package aktual.budget.sync.domain

import aktual.budget.BudgetLocalPreferences
import aktual.budget.BudgetSyncController
import aktual.budget.SyncEvents
import aktual.budget.SyncStateHolder
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.DbMetadataKey.Companion.CloudFileId
import aktual.budget.model.LocalChange
import aktual.budget.model.SyncState
import aktual.budget.model.SyncState.SyncFailed
import aktual.core.model.ForegroundStateHolder
import aktual.core.utils.runCatchingCancellable
import aktual.di.BudgetCoroutineScope
import aktual.di.BudgetScope
import aktual.di.Closeable
import aktual.di.Initializable
import aktual.log
import aktual.prefs.AppPreferences
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.ForScope
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@SingleIn(BudgetScope::class)
@ContributesBinding(BudgetScope::class, binding<BudgetSyncController>())
@ContributesIntoSet(BudgetScope::class, binding<@ForScope(BudgetScope::class) Initializable>())
@ContributesIntoSet(BudgetScope::class, binding<@ForScope(BudgetScope::class) Closeable>())
class BudgetSyncControllerImpl
internal constructor(
  private val syncStateHolder: SyncStateHolder,
  private val syncer: IncrementalSyncer,
  private val scope: BudgetCoroutineScope,
  private val prefs: AppPreferences,
  private val syncDao: SyncDao,
  private val budgetMetadata: BudgetLocalPreferences,
  private val syncEvents: SyncEvents,
  private val foregroundState: ForegroundStateHolder,
) : BudgetSyncController, Initializable, Closeable {
  private var syncJob: Job? = null
  private var inactiveJob: Job? = null

  // Fetch anything that changed on the server since the budget was last open
  override fun initialize() {
    scope.launch { rebuildScheduleJsonPaths() }
    schedule()
    // Fetch what changed on other devices while the app was in the background
    scope.launch { foregroundState.drop(1).filter { it }.collect { schedule() } }
  }

  private suspend fun rebuildScheduleJsonPaths() =
    runCatchingCancellable(
      attempt = { syncDao.rebuildScheduleJsonPaths() },
      onFailure = { e ->
        // The database closes with the budget, which can happen while this is still running
        currentCoroutineContext().ensureActive()
        log.e(e) { "Failed rebuilding schedule JSON paths" }
      },
    )

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
    if (budgetMetadata[CloudFileId] == null) {
      log.d { "Local-only budget, skipping sync" }
      syncEvents.complete(Succeeded)
      return
    }

    val token = prefs.token.get()
    if (token == null) {
      update(NoToken)
      syncEvents.complete(Failed)
      scheduleInactive()
      return
    }

    log.i { "Sync started" }
    update(Syncing)
    when (val result = syncer.sync()) {
      is Success -> {
        update(Inactive)
        syncEvents.complete(Succeeded)
      }

      ClockDriftError -> {
        // Kept on screen, since every retry fails the same way until the device clock is fixed
        update(ClockDrift)
        syncEvents.complete(Failed)
        return
      }

      is SyncResult.Error -> {
        update(SyncFailed(result.toString()))
        syncEvents.complete(Failed)
      }
    }
    scheduleInactive()
  }

  private fun update(state: SyncState) {
    log.d { "Updating state to $state" }
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
