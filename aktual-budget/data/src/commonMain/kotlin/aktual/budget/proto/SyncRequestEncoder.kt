package aktual.budget.proto

import aktual.budget.encryption.BufferEncrypter
import aktual.budget.encryption.EncryptResult
import aktual.budget.model.BudgetId
import aktual.budget.model.BudgetScope
import aktual.budget.model.DbMetadata
import aktual.budget.model.Message
import aktual.budget.model.Timestamp
import aktual.budget.prefs.BudgetLocalPreferences
import aktual.budget.prefs.get
import aktual.core.model.KeyId
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import okio.Buffer
import okio.ByteString

fun interface SyncRequestEncoder {
  suspend operator fun invoke(
    groupId: String,
    budgetId: BudgetId,
    since: Timestamp,
    messages: List<Message>,
  ): ByteString
}

@BindingContainer
@ContributesTo(BudgetScope::class)
internal interface SyncRequestEncoderContainer {
  @Binds val SyncRequestEncoderImpl.binds: SyncRequestEncoder
}

@Inject
internal class SyncRequestEncoderImpl(
  private val encrypter: BufferEncrypter,
  private val prefs: BudgetLocalPreferences,
) : SyncRequestEncoder {
  override suspend operator fun invoke(
    groupId: String,
    budgetId: BudgetId,
    since: Timestamp,
    messages: List<Message>,
  ): ByteString {
    val keyId = prefs[DbMetadata.EncryptKeyId]?.let(::KeyId)

    val envelopes = messages.map { message ->
      ProtoMessageEnvelope(
        timestamp = message.timestamp.toString(),
        isEncrypted = keyId != null,
        content = encodeContent(message, keyId),
      )
    }

    val request = ProtoSyncRequest(
      messages = envelopes,
      fileId = budgetId.value,
      groupId = groupId,
      keyId = keyId?.value.orEmpty(),
      since = since.toString(),
    )

    return request.encodeByteString()
  }

  private suspend fun encodeContent(message: Message, keyId: KeyId?): ByteString {
    val protoMessage = with(message) { ProtoMessage(dataset, row, column, value.encode()) }
    val encodedMessage = ProtoMessage.ADAPTER.encodeByteString(protoMessage)
    return if (keyId != null) {
      val messageBuffer = Buffer().also { it.write(encodedMessage) }
      val result = encrypter(keyId, messageBuffer)
      require(result is EncryptResult.EncryptedBuffer) { "Failed encrypting message: $message" }

      val encryptedData = ProtoEncryptedData(
        iv = result.meta.iv,
        authTag = result.meta.authTag,
        data_ = result.buffer.readByteString(),
      )

      encryptedData.encodeByteString()
    } else {
      encodedMessage
    }
  }
}
