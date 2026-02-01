package aktual.budget.sync.vm

import aktual.api.client.AktualApis
import aktual.api.client.AktualApisStateHolder
import aktual.api.client.BudgetSyncApi
import aktual.budget.db.dao.MessagesCrdtDao
import aktual.budget.model.BudgetId
import aktual.budget.model.BudgetScope
import aktual.budget.model.DbMetadata.Companion.CloudFileId
import aktual.budget.model.DbMetadata.Companion.GroupId
import aktual.budget.model.DbMetadata.Companion.LastSyncedTimestamp
import aktual.budget.model.SyncState
import aktual.budget.model.SyncStateHolder
import aktual.budget.model.Timestamp
import aktual.budget.prefs.BudgetLocalPreferences
import aktual.budget.prefs.get
import aktual.core.model.Token
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import logcat.logcat
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

interface BudgetSyncController {
  fun start(token: Token, id: BudgetId, since: Timestamp?)
  fun cancel()
}

/**
 * From _fullSync() in actual/packages/loot-core/src/server/sync/index.ts
 */
@Inject
@ContributesBinding(BudgetScope::class)
@SingleIn(BudgetScope::class)
internal class BudgetSyncControllerImpl(
  private val scope: CoroutineScope,
  private val syncStateHolder: SyncStateHolder,
  private val apiStateHolder: AktualApisStateHolder,
  private val apiFactory: BudgetSyncApi.Factory,
  private val contexts: CoroutineContexts,
  private val messagesDao: MessagesCrdtDao,
  private val prefs: BudgetLocalPreferences,
  private val clock: Clock,
) : BudgetSyncController {
  private val mutex = Mutex()
  private var job: Job? = null

  override fun cancel() {
    job?.cancel()
    job = null
    syncStateHolder.update { SyncState.Inactive }
  }

  override fun start(token: Token, id: BudgetId, since: Timestamp?) {
    val apis = apiStateHolder.value
    if (apis == null) {
      syncStateHolder.update { SyncState.Disconnected }
      logcat.d { "Aborting sync - disconnected" }
      return
    }

    if (mutex.isLocked) {
      syncStateHolder.update { SyncState.Queued }
      logcat.d { "Sync queued!" }
    }

    job?.cancel()
    job = scope.launch {
      mutex.withLock {
        syncStateHolder.update { SyncState.InProgress }
        logcat.d { "Sync in progress" }

        try {
          syncUnsafely(apis, token, since)
          syncStateHolder.update { SyncState.Success }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          logcat.e(e) { "Failed syncing" }
          syncStateHolder.update { SyncState.OtherFailure(e.requireMessage()) }
        }
      }
    }
  }

  private suspend fun syncUnsafely(apis: AktualApis, token: Token, since: Timestamp?) {
    val budgetId = requireNotNull(prefs[CloudFileId]) { "No budget ID found in prefs" }
    val groupId = requireNotNull(prefs[GroupId]) { "No group ID found in prefs" }
    val lastSyncedTimestamp = prefs[LastSyncedTimestamp]

    val sinceTimestamp = since
      ?: lastSyncedTimestamp
      ?: Timestamp(instant = clock.now() - 5.minutes, counter = 0, node = "0")

    val messages = withContext(contexts.io) { messagesDao.getMessagesSince(sinceTimestamp) }
    val budgetApi = apiFactory(apis.serverUrl, apis.client)
    val response = budgetApi.syncBudget(token, budgetId, groupId, sinceTimestamp, messages)

    // Abort if the file is either no longer loaded, the group id has changed because of a sync reset
    if (prefs[GroupId] != groupId) {
      logcat.e { "Group ID has changed, sync is no longer valid! Old = $groupId, new = ${prefs[GroupId]}" }
      syncStateHolder.reset()
      return
    }

//    val localTimeChanged = clock.timestamp.toString() !== currentTime;
  }
}
