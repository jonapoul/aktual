package aktual.budget.db.dao

import aktual.budget.db.BudgetDatabase
import aktual.budget.model.LocalChange
import aktual.budget.model.MerkleOperations
import aktual.budget.model.Message
import aktual.budget.model.MessageEnvelope
import aktual.budget.model.MessageValue
import aktual.budget.model.Timestamp
import app.cash.sqldelight.async.coroutines.await
import app.cash.sqldelight.async.coroutines.awaitAsList
import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import app.cash.sqldelight.db.SqlDriver
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Inject
class SyncDao(
  private val database: BudgetDatabase,
  private val driver: SqlDriver,
  private val clock: Clock,
) {
  private val crdtQueries = database.messagesCrdtQueries
  private val clockQueries = database.messagesClockQueries

  /**
   * Record local changes in the CRDT log and apply them to the target tables. Each change gets a
   * monotonically increasing timestamp via [Timestamp.send]. Mirrors upstream's sendMessages ->
   * applyMessages flow in packages/loot-core/src/server/sync/index.ts.
   */
  suspend fun sendMessages(changes: List<LocalChange>): ApplyResult {
    var clockTimestamp = getClockTimestamp()
    val envelopes = changes.map { change ->
      clockTimestamp = Timestamp.send(clockTimestamp, clock)
      MessageEnvelope(
        timestamp = clockTimestamp,
        isEncrypted = false,
        content =
          Message(
            dataset = change.dataset,
            row = change.row,
            column = change.column,
            timestamp = clockTimestamp,
            value = change.value,
          ),
      )
    }
    return applyMessages(
      envelopes = envelopes,
      currentMerkle = getCurrentMerkle(),
      updatedClockTimestamp = clockTimestamp,
    )
  }

  /** Fetch all CRDT messages with timestamps after [since]. */
  suspend fun getMessagesSince(since: Timestamp): List<Message> =
    crdtQueries.getMessagesSince(since).awaitAsList().map { row ->
      Message(
        dataset = row.dataset,
        row = row.row,
        column = row.column,
        timestamp = row.timestamp,
        value = MessageValue.decode(row.value_.decodeToString()),
      )
    }

  /**
   * Apply incoming messages from the server to the local database. For each message: if a newer
   * local message already exists for the same (dataset, row, column), the message is "old" and only
   * inserted into the CRDT log + merkle. Otherwise it is also applied to the target table. All work
   * runs in a single transaction.
   */
  suspend fun applyMessages(
    envelopes: List<MessageEnvelope>,
    currentMerkle: JsonObject,
    updatedClockTimestamp: Timestamp? = null,
  ): ApplyResult {
    val compared = compareMessages(envelopes)
    val sorted = compared.sortedBy { (envelope, _) -> envelope.timestamp.toString() }
    val affectedTables = mutableSetOf<String>()
    var merkle = currentMerkle

    database.transaction {
      for ((envelope, isOld) in sorted) {
        val msg = envelope.content

        if (!isOld) {
          applyToTable(msg.dataset, msg.row, msg.column, msg.value)
          if (msg.dataset != PREFS_DATASET) {
            affectedTables += msg.dataset
          }
        }

        crdtQueries.insert(
          timestamp = envelope.timestamp,
          dataset = msg.dataset,
          row = msg.row,
          column = msg.column,
          value_ = msg.value.encode().encodeToByteArray(),
        )

        merkle = MerkleOperations.insert(merkle, envelope.timestamp)
      }

      if (DatabaseTables.RULES in affectedTables) rebuildScheduleJsonPaths()

      merkle = MerkleOperations.prune(merkle)
      saveClock(merkle, updatedClockTimestamp)
    }

    // applyToTable writes raw SQL, which doesn't notify SQLDelight's query flows by itself
    affectedTables.forEach(driver::notifyListeners)

    return ApplyResult(merkle = merkle, affectedTables = affectedTables)
  }

  // schedules_json_paths isn't synced, so it's derived from the rules whenever they change. Mirrors
  // trackJSONPaths() and onRuleUpdate() in packages/loot-core/src/server/schedules/app.ts
  suspend fun rebuildScheduleJsonPaths() {
    driver.await(identifier = null, sql = REBUILD_SCHEDULE_JSON_PATHS, parameters = 0)
    driver.notifyListeners(SCHEDULES_JSON_PATHS)
  }

  /** Read the current merkle trie from the messages_clock table. */
  suspend fun getCurrentMerkle(): JsonObject {
    val row = clockQueries.getFirst().awaitAsOneOrNull() ?: return MerkleOperations.emptyTrie()
    val clock = row.clock ?: return MerkleOperations.emptyTrie()
    return clock[MERKLE_KEY]?.jsonObject ?: MerkleOperations.emptyTrie()
  }

  /**
   * Compare incoming messages against local CRDT state. Returns pairs of (envelope, isOld). A
   * message is "old" if a local message with the same (dataset, row, column) and an equal-or-newer
   * timestamp already exists -- meaning our local data is already up to date for that cell.
   */
  private suspend fun compareMessages(
    envelopes: List<MessageEnvelope>,
  ): List<Pair<MessageEnvelope, Boolean>> = envelopes.mapNotNull { envelope ->
    val msg = envelope.content
    val existing =
      crdtQueries.getTimestamp(msg.dataset, msg.row, msg.column, envelope.timestamp).awaitAsList()
    when {
      existing.isEmpty() -> envelope to false // New message
      existing.first() != envelope.timestamp -> envelope to true // Old message (local is newer)
      else -> null // Exact duplicate already in CRDT log, skip entirely
    }
  }

  /**
   * Apply a single message to the target table. Tries UPDATE first; if no rows were affected, falls
   * back to INSERT. Skips the "prefs" pseudo-dataset which is handled separately by the upstream
   * protocol.
   */
  private suspend fun applyToTable(
    dataset: String,
    row: String,
    column: String,
    value: MessageValue,
  ) {
    if (dataset == PREFS_DATASET) return

    // Try UPDATE first
    val updated =
      driver.await(
        identifier = null,
        sql = "UPDATE $dataset SET $column = ? WHERE id = ?",
        parameters = 2,
      ) {
        when (value) {
          is MessageValue.Number -> bindLong(0, value.value)
          is MessageValue.String -> bindString(0, value.value)
          Null -> bindString(0, null)
        }
        bindString(1, row)
      }

    // If no rows updated, INSERT
    if (updated == 0L) {
      driver.await(
        identifier = null,
        sql = "INSERT INTO $dataset (id, $column) VALUES (?, ?)",
        parameters = 2,
      ) {
        bindString(0, row)
        when (value) {
          is MessageValue.Number -> bindLong(1, value.value)
          is MessageValue.String -> bindString(1, value.value)
          Null -> bindString(1, null)
        }
      }
    }
  }

  /** Read the current clock timestamp from the messages_clock table, or a zero-epoch fallback. */
  private suspend fun getClockTimestamp(): Timestamp {
    val clock = clockQueries.getFirst().awaitAsOneOrNull()?.clock ?: return EPOCH_TIMESTAMP
    val raw = clock[TIMESTAMP_KEY]?.jsonPrimitive?.contentOrNull ?: return EPOCH_TIMESTAMP
    return Timestamp.parse(raw)
  }

  private suspend fun saveClock(merkle: JsonObject, clockTimestamp: Timestamp? = null) {
    val existing = clockQueries.getFirst().awaitAsOneOrNull()
    val updatedClock = buildJsonObject {
      existing?.clock?.forEach(::put)
      put(MERKLE_KEY, merkle)
      if (clockTimestamp != null) {
        put(TIMESTAMP_KEY, JsonPrimitive(clockTimestamp.toString()))
      }
    }
    clockQueries.insert(updatedClock)
  }

  private companion object {
    const val PREFS_DATASET = "prefs"
    const val SCHEDULES_JSON_PATHS = "schedules_json_paths"

    // The path of each schedule condition, picked like extractScheduleConds() in
    // packages/loot-core/src/shared/schedules.ts
    const val REBUILD_SCHEDULE_JSON_PATHS =
      """
      INSERT OR REPLACE INTO schedules_json_paths (schedule_id, payee, account, amount, date)
      SELECT
        json_extract(a.value, '$.value'),
        (
          SELECT '$[' || c.key || ']' FROM json_each(r.conditions) c
          WHERE json_extract(c.value, '$.op') = 'is'
            AND json_extract(c.value, '$.field') IN ('payee', 'description')
          ORDER BY json_extract(c.value, '$.field') = 'payee' DESC, c.key
          LIMIT 1
        ),
        (
          SELECT '$[' || c.key || ']' FROM json_each(r.conditions) c
          WHERE json_extract(c.value, '$.op') = 'is'
            AND json_extract(c.value, '$.field') IN ('account', 'acct')
          ORDER BY json_extract(c.value, '$.field') = 'account' DESC, c.key
          LIMIT 1
        ),
        (
          SELECT '$[' || c.key || ']' FROM json_each(r.conditions) c
          WHERE json_extract(c.value, '$.op') IN ('is', 'isapprox', 'isbetween')
            AND json_extract(c.value, '$.field') = 'amount'
          ORDER BY c.key
          LIMIT 1
        ),
        (
          SELECT '$[' || c.key || ']' FROM json_each(r.conditions) c
          WHERE json_extract(c.value, '$.op') IN ('is', 'isapprox')
            AND json_extract(c.value, '$.field') = 'date'
          ORDER BY c.key
          LIMIT 1
        )
      FROM rules r, json_each(r.actions) a
      WHERE r.tombstone = 0
        AND json_valid(r.conditions)
        AND json_valid(r.actions)
        AND json_extract(a.value, '$.op') = 'link-schedule'
        AND json_extract(a.value, '$.value') IS NOT NULL
      """
        .trimIndent()
    const val MERKLE_KEY = "merkle"
    const val TIMESTAMP_KEY = "timestamp"
    val EPOCH_TIMESTAMP = Timestamp.fromMilliseconds(millis = 0)
  }
}
