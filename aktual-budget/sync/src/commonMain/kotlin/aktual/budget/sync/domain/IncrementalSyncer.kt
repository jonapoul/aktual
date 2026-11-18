package aktual.budget.sync.domain

import aktual.api.client.BudgetSyncApi
import aktual.api.client.SyncRejectedException
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.DbMetadataKey.Companion.CloudFileId
import aktual.budget.model.DbMetadataKey.Companion.GroupId
import aktual.budget.model.DbMetadataKey.Companion.LastSyncedTimestamp
import aktual.budget.model.Merkle
import aktual.budget.model.MerkleOperations
import aktual.budget.model.MessageEnvelope
import aktual.budget.model.Timestamp
import aktual.budget.proto.SyncRequestEncoder
import aktual.budget.utils.BudgetLocalPreferences
import aktual.core.utils.runCatchingCancellable
import aktual.log
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.update
import okio.IOException

@Inject
internal class IncrementalSyncer(
  private val syncApi: BudgetSyncApi,
  private val encoder: SyncRequestEncoder,
  private val budgetMetadata: BudgetLocalPreferences,
  private val clock: Clock,
  private val syncDao: SyncDao,
) {
  suspend fun sync(): SyncResult =
    runCatchingCancellable(
      attempt = { fullSync(sinceTimestamp = null, count = 0, prevDiffTime = null) },
      onFailure = { e ->
        when (e) {
          is IOException -> SyncResult.NetworkError(e)

          is SyncRejectedException if e.isClockDrift -> {
            log.w { "Server rejected sync, device clock is too far ahead" }
            ClockDriftError
          }

          else -> {
            log.e(e) { "Failed syncing" }
            SyncResult.OtherError(e)
          }
        }
      },
    )

  private tailrec suspend fun fullSync(
    sinceTimestamp: Timestamp?,
    count: Int,
    prevDiffTime: Long?,
  ): SyncResult {
    val groupId = budgetMetadata[GroupId]
    val budgetId = budgetMetadata[CloudFileId]
    if (groupId == null || budgetId == null) {
      log.w { "Null required prefs! groupId=$groupId, budgetId=$budgetId" }
      return SyncResult.MissingPrefError(
        missingGroupId = groupId == null,
        missingBudgetId = budgetId == null,
      )
    }

    // Snapshot the point at which we are currently syncing
    val currentTime = syncDao.getClockTimestamp()
    val since = sinceTimestamp ?: budgetMetadata[LastSyncedTimestamp] ?: fiveMinutesAgo()
    val localMessages = syncDao.getMessagesSince(since)
    val batches = splitIntoSyncBatches(localMessages)
    val serverMessages = linkedMapOf<Timestamp, MessageEnvelope>()
    var serverMerkle: Merkle? = null

    for ((i, batch) in batches.withIndex()) {
      if (batches.size > 1) log.i { "Sending sync batch ${i + 1}/${batches.size}" }

      // Later batches only fetch messages newer than the snapshot. Older ones came back with the
      // first batch or are caught by the merkle diff below
      val batchSince = if (i == 0) since else currentTime
      val response = syncApi.syncBudget(encoder(groupId, budgetId, batchSince, batch))
      response.messages.forEach { msg -> serverMessages[msg.timestamp] = msg }
      serverMerkle = response.merkle
    }

    val currentMerkle = syncDao.getCurrentMerkle()
    val applyResult = syncDao.applyMessages(serverMessages.values.toList(), currentMerkle)

    val responseMerkle by requireNotNull(serverMerkle)
    val diffTime = MerkleOperations.diff(responseMerkle, applyResult.merkle)

    if (diffTime != null) {
      if (count < MAX_RECURSIONS && (count < MAX_SAME_DIFF || diffTime != prevDiffTime)) {
        log.d { "Merkle diff at $diffTime, recursing (attempt ${count + 1})" }
        return fullSync(
          sinceTimestamp = Timestamp.fromMilliseconds(diffTime),
          count = count + 1,
          prevDiffTime = diffTime,
        )
      }
      log.w { "Out of sync after $count attempts, diffTime=$diffTime" }
      return OutOfSyncError
    }

    val now = clock.now()
    budgetMetadata.update { m ->
      m.set(
        LastSyncedTimestamp,
        Timestamp(now, counter = 0, node = Timestamp.BASE_NODE),
      )
    }
    return SyncResult.Success(affectedTables = applyResult.affectedTables)
  }

  private fun fiveMinutesAgo(): Timestamp =
    Timestamp(instant = clock.now() - 5.minutes, counter = 0, node = Timestamp.BASE_NODE)

  private companion object {
    const val MAX_RECURSIONS = 100
    const val MAX_SAME_DIFF = 10
  }
}
