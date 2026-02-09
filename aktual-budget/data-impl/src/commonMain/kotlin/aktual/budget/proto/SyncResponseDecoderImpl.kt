package aktual.budget.proto

import aktual.budget.encryption.BufferDecrypter
import aktual.budget.encryption.DecryptResult
import aktual.budget.encryption.DefaultMeta
import aktual.budget.model.BudgetScope
import aktual.budget.model.DbMetadata
import aktual.budget.model.Merkle
import aktual.budget.model.Message
import aktual.budget.model.MessageEnvelope
import aktual.budget.model.MessageValue
import aktual.budget.model.SyncResponse
import aktual.budget.model.Timestamp
import aktual.core.model.KeyId
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.Source
import okio.buffer

@Inject
@ContributesBinding(BudgetScope::class)
class SyncResponseDecoderImpl(
    private val decrypter: BufferDecrypter,
) : SyncResponseDecoder {
  override suspend fun invoke(source: Source, metadata: DbMetadata): SyncResponse {
    val byteString = source.buffer().use { requireNotNull(it.readUtf8().decodeBase64()) }
    val response = ProtoSyncResponse.ADAPTER.decode(byteString)
    val encryptKeyId = metadata[DbMetadata.EncryptKeyId]
    return SyncResponse(
        merkle = Merkle(response.merkle),
        messages = response.messages.map { msg -> msg.parseEnvelope(encryptKeyId) },
    )
  }

  private suspend fun ProtoMessageEnvelope.parseEnvelope(
      encryptKeyId: String?,
  ): MessageEnvelope {
    val content = if (isEncrypted) decryptContent(content, encryptKeyId) else content
    val protoMessage = ProtoMessage.ADAPTER.decode(content)
    val time = Timestamp.parse(timestamp)
    return MessageEnvelope(
        timestamp = time,
        isEncrypted = isEncrypted,
        content =
            with(protoMessage) { Message(dataset, row, column, time, MessageValue.decode(value_)) },
    )
  }

  private suspend fun decryptContent(content: ByteString, encryptKeyId: String?): ByteString {
    val encryptedData = ProtoEncryptedData.ADAPTER.decode(content)
    val buffer = Buffer()
    buffer.write(encryptedData.data_)
    val meta = meta(encryptedData, encryptKeyId)
    val result = decrypter(meta, buffer)
    check(result is DecryptResult.DecryptedBuffer) { "Failed decrypting buffer: $result" }
    return result.buffer.readByteString()
  }

  private fun meta(data: ProtoEncryptedData, encryptKeyId: String?) =
      DefaultMeta(
          keyId = encryptKeyId?.let(::KeyId),
          algorithm = "aes-256-gcm",
          iv = data.iv,
          authTag = data.authTag,
      )
}
