package aktual.budget.sync.domain

import aktual.api.client.BudgetSyncApi
import aktual.api.client.SyncRejectedException
import aktual.budget.db.dao.ApplyResult
import aktual.budget.db.dao.SyncDao
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.Merkle
import aktual.budget.model.MerkleOperations
import aktual.budget.model.Message
import aktual.budget.model.MessageEnvelope
import aktual.budget.model.MessageValue
import aktual.budget.model.SyncResponse
import aktual.budget.model.Timestamp
import aktual.budget.proto.SyncRequestEncoder
import aktual.budget.utils.BudgetLocalPreferences
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlin.test.Test
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

class IncrementalSyncerTest {
  private val requests = mutableListOf<Request>()
  private val responses = ArrayDeque<List<MessageEnvelope>>()
  private val syncDao = mockk<SyncDao>()
  private val syncApi = mockk<BudgetSyncApi>()

  private val encoder = SyncRequestEncoder { _, _, since, messages ->
    requests += Request(since, messages)
    "request-${requests.size}".encodeUtf8()
  }

  @Test
  fun `Sends a large backlog to the server in batches`() = runTest {
    val bigValue = MessageValue.String("x".repeat(BIG_MESSAGE_BYTES))
    val local = List(6) { i -> message("note-$i", bigValue, timestamp(minute = 20 + i)) }
    val other = message("other", MessageValue.Number(100), timestamp(minute = 0))
    val later = message("later", MessageValue.Number(200), timestamp(minute = 30))
    responses.addAll(
      listOf(listOf(envelope(other)), emptyList(), listOf(envelope(later), envelope(other))),
    )

    val applied = slot<List<MessageEnvelope>>()
    coEvery { syncDao.getClockTimestamp() } returns CLOCK
    coEvery { syncDao.getMessagesSince(SINCE) } returns local
    coEvery { syncDao.getCurrentMerkle() } returns MerkleOperations.emptyTrie()
    coEvery { syncDao.applyMessages(capture(applied), any()) } returns
      ApplyResult(MerkleOperations.emptyTrie(), affectedTables = emptySet())
    coEvery { syncApi.syncBudget(any<ByteString>()) } answers
      {
        SyncResponse(responses.removeFirst(), Merkle(EMPTY_MERKLE))
      }

    val result = syncer().sync()

    assertThat(result).isInstanceOf<SyncResult.Success>()
    assertThat(requests.map { it.messages.size }).containsExactly(2, 2, 2)
    assertThat(requests.map { it.since }).containsExactly(SINCE, CLOCK, CLOCK)
    assertThat(requests.flatMap { it.messages }).isEqualTo(local)
    assertThat(applied.captured.map { it.content.row }).containsExactly("other", "later")
    coVerify(exactly = 1) { syncDao.applyMessages(any(), any()) }
  }

  @Test
  fun `Budget deleted from the server`() = runTest {
    coEvery { syncDao.getClockTimestamp() } returns CLOCK
    coEvery { syncDao.getMessagesSince(SINCE) } returns emptyList()
    coEvery { syncApi.syncBudget(any<ByteString>()) } throws SyncRejectedException(FileNotFound)

    assertThat(syncer().sync()).isEqualTo(FileNotFoundError)
  }

  @Test
  fun `Still sends one request with nothing to upload`() {
    assertThat(splitIntoSyncBatches(emptyList())).containsExactly(emptyList<Message>())
  }

  @Test
  fun `Oversized message gets a batch of its own`() {
    val small = message("small", MessageValue.Number(1), timestamp(minute = 0))
    val huge = message("huge", MessageValue.String("x".repeat(500)), timestamp(minute = 1))

    val batches = splitIntoSyncBatches(listOf(small, huge, small), maxBytes = 300)

    assertThat(batches).containsExactly(listOf(small), listOf(huge), listOf(small))
  }

  @Test
  fun `Batches stay under the size limit`() {
    val value = MessageValue.String("x".repeat(1000))
    val messages = List(100) { i -> message("row-$i", value, timestamp(minute = i)) }

    val batches = splitIntoSyncBatches(messages, maxBytes = 10_000)

    // Each message is ~1.1KB once the overhead is added, so 8 fit under the limit
    assertThat(batches.map { it.size }).isEqualTo(List(12) { 8 } + 4)
    assertThat(batches.flatten()).isEqualTo(messages)
  }

  private fun syncer() =
    IncrementalSyncer(
      syncApi = syncApi,
      encoder = encoder,
      budgetMetadata =
        TestMetadata(
          DbMetadata(groupId = "group", cloudFileId = BudgetId("abc"), lastSyncedTimestamp = SINCE),
        ),
      clock = Clock.System,
      syncDao = syncDao,
    )

  private data class Request(val since: Timestamp, val messages: List<Message>)

  @OptIn(ExperimentalForInheritanceCoroutinesApi::class)
  private class TestMetadata(initial: DbMetadata) :
    BudgetLocalPreferences, MutableStateFlow<DbMetadata> by MutableStateFlow(initial)

  private companion object {
    const val BIG_MESSAGE_BYTES = 1_572_864 // 1.5MB, so two fit in a 4MB batch but three don't
    const val EMPTY_MERKLE = """{"hash":0}"""
    val SINCE = timestamp(minute = 10)
    val CLOCK = timestamp(minute = 26)

    fun timestamp(minute: Int) = Timestamp.fromMilliseconds(1_700_000_000_000L + minute * 60_000L)

    fun message(row: String, value: MessageValue, timestamp: Timestamp) =
      Message(dataset = "notes", row = row, column = "note", timestamp = timestamp, value = value)

    fun envelope(message: Message) =
      MessageEnvelope(timestamp = message.timestamp, isEncrypted = false, content = message)
  }
}
