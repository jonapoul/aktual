package aktual.budget.proto

import aktual.budget.encryption.BufferEncrypterImpl
import aktual.budget.model.BudgetId
import aktual.budget.model.DbMetadata
import aktual.budget.model.Message
import aktual.budget.model.MessageValue.Number as MsgNum
import aktual.budget.model.MessageValue.String as MsgStr
import aktual.budget.model.Timestamp
import aktual.core.model.EncryptionKeys
import aktual.core.model.base64
import aktual.test.RESOURCES_DIR
import aktual.test.TestBudgetLocalPreferences
import alakazam.test.TestCoroutineContexts
import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import okio.ByteString

class SyncRequestEncoderTest {
  @Test
  fun `Encode unencrypted empty sync request`() = runTest {
    val groupId = "493b2630-132b-4a27-b001-2c3ceb49501a"
    val since = Timestamp.parse("2025-12-14T11:06:38.067Z-0000-b5e4d1594ac5787b")
    val budgetId = BudgetId("657f6253-c412-4458-8a1b-de5b85ed8011")
    val messages = emptyList<Message>()

    val encoder = buildEncoder(key = null)
    val encoded = encoder(groupId, budgetId, since, messages)

    assertThat(encoded.base64())
      .isEqualTo(RESOURCES_DIR.resolve("proto-request-unencrypted-empty.txt").readLines().first())
  }

  @Test
  fun `Encode unencrypted add one row request`() = runTest {
    val groupId = "493b2630-132b-4a27-b001-2c3ceb49501a"
    val since = Timestamp.parse("2025-12-14T11:06:38.067Z-0000-b5e4d1594ac5787b")
    val budgetId = BudgetId("657f6253-c412-4458-8a1b-de5b85ed8011")

    val row = "1e4b2db3-eb38-4db9-b4e6-8ced8e424793"
    val dataset = "transactions"
    val time = Timestamp.parse("2025-12-14T11:53:11.334Z-0000-b5e4d1594ac5787b")
    val time2 = Timestamp.parse("2025-12-14T11:53:11.335Z-0000-b5e4d1594ac5787b")

    val messages =
      listOf(
        Message(dataset, row, "acct", time(0), MsgStr("a30f58c3-e097-4951-adfc-4d23b5228aa3")),
        Message(dataset, row, "cleared", time(1), MsgNum(0)),
        Message(dataset, row, "amount", time(2), MsgNum(0)),
        Message(dataset, row, "reconciled", time(3), MsgNum(0)),
        Message(
          dataset,
          row,
          "description",
          time2(0),
          MsgStr("c1205fa3-b69f-460e-94e0-5550e2098cc3"),
        ),
        Message(dataset, row, "date", time2(1), MsgNum(20_251_214)),
        Message(dataset, row, "sort_order", time2(2), MsgNum(1_765_713_190_167)),
      )

    val encoder = buildEncoder(key = null)
    val encoded = encoder(groupId, budgetId, since, messages)

    assertThat(encoded.base64())
      .isEqualTo(RESOURCES_DIR.resolve("proto-request-unencrypted-added.txt").readLines().first())
  }

  @Test
  fun `Encode encrypted new local account request`() = runTest {
    val groupId = "9f2366d8-8503-448f-98fe-15f215fe2a53"
    val since = Timestamp.parse("2025-12-14T16:57:28.257Z-007F-8afeb8e9723b4e9e")
    val budgetId = BudgetId("2cc03f9c-4d94-4196-a0de-1750e645db18")

    val rowAcc = "4391f151-f31e-46c1-aed5-ea3286b2ab72"
    val rowPay = "4624e922-0819-4355-8c5a-8d12c877a30c"

    val time1 = Timestamp.parse("2025-12-14T17:02:10.557Z-0000-8afeb8e9723b4e9e")
    val time2 = Timestamp.parse("2025-12-14T17:02:10.591Z-0000-8afeb8e9723b4e9e")
    val time3 = Timestamp.parse("2025-12-14T17:02:10.600Z-0000-8afeb8e9723b4e9e")

    val messages =
      listOf(
        // new account
        Message("accounts", rowAcc, "name", time1(0), MsgStr("Dummy")),
        Message("accounts", rowAcc, "offbudget", time1(1), MsgNum(0)),
        Message("accounts", rowAcc, "closed", time1(2), MsgNum(0)),
        Message("accounts", rowAcc, "sort_order", time1(3), MsgNum(229_376)),

        // new payee
        Message("payees", rowPay, "name", time2(0), MsgStr("")),
        Message("payees", rowPay, "transfer_acct", time2(1), MsgStr(rowAcc)),

        // mapping
        Message("payee_mapping", rowPay, "targetId", time3(0), MsgStr(rowPay)),
      )

    val encoder =
      buildEncoder(
        key = "whQKldE0yvZp1QjVhWsfCxP9uBMIKFk9NBUhpTTCtLE=".base64(),
        iv =
          sequenceOf(
            "Z1ru0ctb4AL04Rz/",
            "z5Q74nOw/bLtRQ65",
            "hhZkH22Y5auwMpzO",
            "zvWWVB0QasE8SRae",
            "IvQpMkbqvVVLhpVC",
            "9eMTHvs5B80fDbw7",
            "xTAtW1uXcZi2ZDvR",
          ),
        metadata = DbMetadata(DbMetadata.EncryptKeyId to "de88d4dd-3536-4369-9191-8b5ac0ad2bf4"),
      )
    val encoded = encoder(groupId, budgetId, since, messages)

    assertThat(encoded.base64())
      .isEqualTo(
        RESOURCES_DIR.resolve("proto-request-encrypted-new-account.txt").readLines().first()
      )
  }

  @Test
  fun `Encode encrypted delete local account request`() = runTest {
    val groupId = "9f2366d8-8503-448f-98fe-15f215fe2a53"
    val since = Timestamp.parse("2025-12-14T14:55:21.611Z-0005-b56c5557dc0f979f")
    val budgetId = BudgetId("2cc03f9c-4d94-4196-a0de-1750e645db18")

    val messages =
      listOf(
        Message(
          dataset = "accounts",
          row = "044c125e-011b-471c-96a2-27641f4995ea",
          column = "tombstone",
          timestamp = Timestamp.parse("2025-12-14T14:55:30.250Z-0000-b56c5557dc0f979f"),
          value = MsgNum(1),
        )
      )

    val encoder =
      buildEncoder(
        key = "whQKldE0yvZp1QjVhWsfCxP9uBMIKFk9NBUhpTTCtLE=".base64(),
        iv = sequenceOf("myuDvoceP6VR3Y5Z"),
        metadata = DbMetadata(DbMetadata.EncryptKeyId to "de88d4dd-3536-4369-9191-8b5ac0ad2bf4"),
      )
    val encoded = encoder(groupId, budgetId, since, messages)

    assertThat(encoded.base64())
      .isEqualTo(
        RESOURCES_DIR.resolve("proto-request-encrypted-delete-account.txt").readLines().first()
      )
  }

  private fun buildEncoder(
    key: ByteString?,
    iv: Sequence<String> = emptySequence(),
    metadata: DbMetadata = DbMetadata(),
  ): SyncRequestEncoder {
    val contexts = TestCoroutineContexts(EmptyCoroutineContext)
    val keys = EncryptionKeys { key }
    val itr = iv.iterator()
    val random =
      mockk<Random> {
        every { nextBytes(size = any()) } answers { itr.next().base64().toByteArray() }
      }
    val encrypter = BufferEncrypterImpl(contexts, keys, random)
    val prefs = TestBudgetLocalPreferences(metadata)
    return SyncRequestEncoderImpl(encrypter, prefs)
  }
}
