package aktual.budget.sync.domain

import aktual.api.client.BudgetSyncApi
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.DbMetadata.Companion.CloudFileId
import aktual.budget.model.DbMetadata.Companion.GroupId
import aktual.budget.model.DbMetadata.Companion.LastSyncedTimestamp
import aktual.budget.model.MerkleOperations
import aktual.budget.model.Timestamp
import aktual.budget.prefs.BudgetLocalPreferences
import aktual.budget.proto.SyncRequestEncoder
import dev.zacsweers.metro.Inject
import io.ktor.utils.io.CancellationException
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.update
import kotlinx.io.IOException
import logcat.logcat

@Inject
internal class IncrementalSyncer(
  private val syncApi: BudgetSyncApi,
  private val encoder: SyncRequestEncoder,
  private val budgetMetadata: BudgetLocalPreferences,
  private val clock: Clock,
  private val syncDao: SyncDao,
) {
  suspend fun sync(): SyncResult =
    try {
      fullSync(sinceTimestamp = null, count = 0, prevDiffTime = null)
    } catch (e: CancellationException) {
      throw e
    } catch (e: IOException) {
      SyncResult.NetworkError(e)
    } catch (e: Exception) {
      logcat.e(e) { "Failed syncing" }
      SyncResult.OtherError(e)
    }

  private tailrec suspend fun fullSync(
    sinceTimestamp: Timestamp?,
    count: Int,
    prevDiffTime: Long?,
  ): SyncResult {
    val groupId = budgetMetadata[GroupId]
    val budgetId = budgetMetadata[CloudFileId]
    if (groupId == null || budgetId == null) {
      logcat.w { "Null required prefs! groupId=$groupId, budgetId=$budgetId" }
      return SyncResult.MissingPrefError(
        missingGroupId = groupId == null,
        missingBudgetId = budgetId == null,
      )
    }

    val since = sinceTimestamp ?: budgetMetadata[LastSyncedTimestamp] ?: fiveMinutesAgo()
    val localMessages = syncDao.getMessagesSince(since)
    val requestBody = encoder(groupId, budgetId, since, localMessages)
    val response = syncApi.syncBudget(requestBody)

    val currentMerkle = syncDao.getCurrentMerkle()
    val applyResult = syncDao.applyMessages(response.messages, currentMerkle)

    val responseMerkle by response.merkle
    val diffTime = MerkleOperations.diff(responseMerkle, applyResult.merkle)

    if (diffTime != null) {
      if (count < MAX_RECURSIONS && (count < MAX_SAME_DIFF || diffTime != prevDiffTime)) {
        logcat.d { "Merkle diff at $diffTime, recursing (attempt ${count + 1})" }
        return fullSync(
          sinceTimestamp = Timestamp.fromMilliseconds(diffTime),
          count = count + 1,
          prevDiffTime = diffTime,
        )
      }
      logcat.w { "Out of sync after $count attempts, diffTime=$diffTime" }
      return SyncResult.OutOfSyncError
    }

    val now = clock.now()
    budgetMetadata.update { m ->
      m.set(LastSyncedTimestamp, Timestamp(now, counter = 0, node = Timestamp.BASE_NODE))
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
