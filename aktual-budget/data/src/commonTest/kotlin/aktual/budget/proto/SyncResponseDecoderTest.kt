package aktual.budget.proto

import aktual.budget.encryption.BufferDecrypterImpl
import aktual.budget.encryption.EncryptionKeys
import aktual.budget.model.DbMetadata
import aktual.budget.model.Message
import aktual.budget.model.MessageEnvelope
import aktual.budget.model.SyncResponse
import aktual.budget.model.Timestamp
import aktual.core.model.Base64String
import aktual.test.RESOURCES_DIR
import aktual.test.isEqualToList
import alakazam.test.core.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.hasLength
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import kotlinx.coroutines.test.runTest
import okio.source
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.BeforeTest
import kotlin.test.Test
import aktual.budget.model.MessageValue.Number as MsgNum
import aktual.budget.model.MessageValue.String as MsgStr

class SyncResponseDecoderTest {
  private lateinit var metadata: DbMetadata
  private lateinit var decoder: SyncResponseDecoder

  @BeforeTest
  fun before() {
    metadata = DbMetadata()
  }

  @Test
  fun `Read big response with no changes`() = runTest {
    val response = readResponse("proto-response-no-changes-big.txt")
    assertThat(response.messages).isEmpty()
    assertThat(response.merkle).hasLength(38162)
  }

  @Test
  fun `Read small response with no changes`() = runTest {
    val response = readResponse("proto-response-no-changes-small.txt")
    assertThat(response.messages).isEmpty()
    assertThat(response.merkle).hasLength(2)
  }

  @Test
  fun `Read big response with one addition`() = runTest {
    val response = readResponse("proto-response-added-big.txt")
    assertThat(response.merkle).hasLength(38130)
    assertThat(response.messages).hasSize(9)
    val dataset = "transactions"
    val row = "b5d4b867-78ed-436b-afcf-29ed8f26b59a"
    assertThat(response.messages.map(MessageEnvelope::content)).isEqualToList(
      Message(dataset, row, "acct", MsgStr("e0f919d2-7e0c-4d32-801c-9f907de3b65c")),
      Message(dataset, row, "category", MsgStr("711b4c36-86f5-4206-a815-01bfa83cc5b0")),
      Message(dataset, row, "cleared", MsgNum(0.0)),
      Message(dataset, row, "amount", MsgNum(-12345.0)),
      Message(dataset, row, "description", MsgStr("8843776a-7041-4e04-9908-110053214806")),
      Message(dataset, row, "notes", MsgStr("sagese")),
      Message(dataset, row, "date", MsgNum(20251213.0)),
      Message(dataset, row, "sort_order", MsgNum(1_765_617_408_523.0)),
      Message(dataset, row, "reconciled", MsgNum(0.0)),
    )
  }

  @Test
  fun `Read small response with one addition`() = runTest {
    val response = readResponse("proto-response-added-small.txt")
    val dataset = "transactions"
    val row = "dbad3b3c-df72-4469-a083-4b3e606ad30d"
    assertThat(response.merkle).hasLength(403)
    assertThat(response.messages.map(MessageEnvelope::content)).isEqualToList(
      Message(dataset, row, "acct", MsgStr("e0f919d2-7e0c-4d32-801c-9f907de3b65c")),
      Message(dataset, row, "category", MsgStr("711b4c36-86f5-4206-a815-01bfa83cc5b0")),
      Message(dataset, row, "cleared", MsgNum(1.0)),
      Message(dataset, row, "amount", MsgNum(-12345.0)),
      Message(dataset, row, "description", MsgStr("8843776a-7041-4e04-9908-110053214806")),
      Message(dataset, row, "notes", MsgStr("Notes go here")),
      Message(dataset, row, "date", MsgNum(20251213.0)),
      Message(dataset, row, "sort_order", MsgNum(1765627806804.0)),
      Message(dataset, row, "reconciled", MsgNum(0.0)),
    )
  }

  @Test
  fun `Read big response with one deletion`() = runTest {
    val response = readResponse("proto-response-deleted-big.txt")
    assertThat(response.merkle).hasLength(38162)
    assertThat(response.messages).isEqualToList(
      MessageEnvelope(
        timestamp = Timestamp.parse("2025-12-13T09:17:39.190Z-0000-8b2714dc84c02f41"),
        isEncrypted = true,
        content = Message(
          dataset = "transactions",
          row = "b5d4b867-78ed-436b-afcf-29ed8f26b59a",
          column = "tombstone",
          value = MsgNum(1.0),
        ),
      ),
    )
  }

  @Test
  fun `Read small response with one deletion`() = runTest {
    val response = readResponse("proto-response-deleted-small.txt")
    assertThat(response.merkle).hasLength(403) // small-ish...
    assertThat(response.messages).isEqualToList(
      MessageEnvelope(
        timestamp = Timestamp.parse("2025-12-13T12:10:51.482Z-0000-b32f9550ad92ec07"),
        isEncrypted = true,
        content = Message(
          dataset = "transactions",
          row = "dbad3b3c-df72-4469-a083-4b3e606ad30d",
          column = "tombstone",
          value = MsgNum(1.0),
        ),
      ),
    )
  }

  private suspend fun readResponse(name: String): SyncResponse {
    val source = RESOURCES_DIR
      .resolve(name)
      .source()

    val keys = EncryptionKeys { KEY }
    val contexts = TestCoroutineContexts(EmptyCoroutineContext)
    val decrypter = BufferDecrypterImpl(contexts, keys)
    decoder = SyncResponseDecoderImpl(decrypter)
    return decoder(source, metadata)
  }

  private companion object {
    val KEY = Base64String("cCclVMAjFoSONwoY8Shs0jUEX8QHZGLOsUymiNQgV8I=")
  }
}
