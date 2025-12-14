package aktual.budget.proto

import aktual.budget.encryption.BufferDecrypter
import aktual.budget.encryption.DecryptResult
import aktual.budget.encryption.Meta
import aktual.budget.model.BudgetScope
import aktual.budget.model.DbMetadata
import aktual.budget.model.Merkle
import aktual.budget.model.Message
import aktual.budget.model.MessageEnvelope
import aktual.budget.model.MessageValue
import aktual.budget.model.SyncResponse
import aktual.budget.model.Timestamp
import aktual.core.model.Base64String
import aktual.core.model.KeyId
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.Source
import okio.buffer
import aktual.budget.proto.EncryptedData as ProtoEncryptedData
import aktual.budget.proto.Message as ProtoMessage
import aktual.budget.proto.MessageEnvelope as ProtoMessageEnvelope
import aktual.budget.proto.SyncResponse as ProtoSyncResponse

fun interface SyncResponseDecoder {
  suspend operator fun invoke(source: Source, metadata: DbMetadata): SyncResponse
}

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
    return MessageEnvelope(
      timestamp = Timestamp.parse(timestamp),
      isEncrypted = isEncrypted,
      content = with(protoMessage) { Message(dataset, row, column, parseValue(value_)) },
    )
  }

  private suspend fun decryptContent(content: ByteString, encryptKeyId: String?): ByteString {
    val encryptedData = ProtoEncryptedData.ADAPTER.decode(content)
    val buffer = Buffer()
    buffer.write(encryptedData.data_)
    val meta = getMeta(encryptedData, encryptKeyId)
    val result = decrypter(meta, buffer)
    check(result is DecryptResult.DecryptedBuffer) { "Failed decrypting buffer: $result" }
    return result.buffer.readByteString()
  }

  private fun getMeta(data: ProtoEncryptedData, encryptKeyId: String?): Meta = object : Meta {
    override val keyId = encryptKeyId?.let(::KeyId)
    override val algorithm = "aes-256-gcm"
    override val iv = Base64String(data.iv)
    override val authTag = Base64String(data.authTag)
  }

  private fun parseValue(value: String): MessageValue = when (val type = value[0]) {
    'N' -> MessageValue.Number(value.substring(startIndex = 2).toDouble())
    'S' -> MessageValue.String(value.substring(startIndex = 2))
    '0' -> MessageValue.Null
    else -> throw IllegalArgumentException("Unexpected type '$type' for content '$value'")
  }
}
