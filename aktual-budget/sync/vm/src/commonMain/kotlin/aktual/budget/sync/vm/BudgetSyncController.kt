package aktual.budget.sync.vm

import aktual.api.client.AktualApisStateHolder
import aktual.budget.db.dao.MessagesCrdtDao
import aktual.budget.model.BudgetScope
import aktual.budget.model.DbMetadata
import aktual.budget.model.SyncState
import aktual.budget.model.SyncStateHolder
import aktual.budget.model.Timestamp
import aktual.budget.prefs.BudgetLocalPreferences
import aktual.budget.prefs.get
import aktual.budget.proto.SyncRequestEncoder
import aktual.budget.proto.SyncResponseDecoder
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
  fun start(since: Timestamp?)
  fun cancel()
}

@Inject
@ContributesBinding(BudgetScope::class)
@SingleIn(BudgetScope::class)
internal class BudgetSyncControllerImpl(
  private val scope: CoroutineScope,
  private val syncStateHolder: SyncStateHolder,
  private val apiStateHolder: AktualApisStateHolder,
  private val contexts: CoroutineContexts,
  private val messagesDao: MessagesCrdtDao,
  private val encoder: SyncRequestEncoder,
  private val decoder: SyncResponseDecoder,
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

  override fun start(since: Timestamp?) {
    val syncApi = apiStateHolder.value?.sync
    if (syncApi == null) {
      syncStateHolder.update { SyncState.Disconnected }
      logcat.d { "Aborting sync - disconnected" }
      return
    }

    if (mutex.isLocked) {
      syncStateHolder.update { SyncState.Queued }
      logcat.d { "Sync queued!" }
    }

    scope.launch {
      mutex.withLock {
        syncStateHolder.update { SyncState.InProgress }
        logcat.d { "Sync in progress" }

        try {
          syncUnsafely(since)
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

  private suspend fun syncUnsafely(sinceTimestamp: Timestamp?) {
    val budgetId = requireNotNull(prefs[DbMetadata.CloudFileId]) { "No budget ID found in prefs" }
    val groupId = requireNotNull(prefs[DbMetadata.GroupId]) { "No group ID found in prefs" }
    val lastSyncedTimestamp = prefs[DbMetadata.LastSyncedTimestamp]

    val since = sinceTimestamp
      ?: lastSyncedTimestamp
      ?: Timestamp(instant = clock.now() - 5.minutes, counter = 0, node = "0")

    val messages = withContext(contexts.io) { messagesDao.getMessagesSince(since) }

    val encoded = encoder(groupId, budgetId, since, messages)
  }
}
