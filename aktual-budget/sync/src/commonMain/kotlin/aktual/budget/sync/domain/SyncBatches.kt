package aktual.budget.sync.domain

import aktual.budget.model.Message

// The server rejects sync requests over 20MB by default, so large uploads are split into batches of
// roughly this size. Mirrors splitIntoSyncBatches() in packages/loot-core/src/server/sync/index.ts
internal const val SYNC_BATCH_MAX_BYTES = 4 * 1024 * 1024

// Generous allowance for the timestamp, protobuf framing and encryption
private const val SYNC_MESSAGE_OVERHEAD_BYTES = 100

private fun Message.estimatedSize(): Int =
  dataset.utf8Size() +
    row.utf8Size() +
    column.utf8Size() +
    value.encode().utf8Size() +
    SYNC_MESSAGE_OVERHEAD_BYTES

private fun String.utf8Size(): Int = encodeToByteArray().size

// Always returns at least one batch so that a sync with nothing to send still fetches the server's
// messages. A message over the limit gets a batch of its own.
internal fun splitIntoSyncBatches(
  messages: List<Message>,
  maxBytes: Int = SYNC_BATCH_MAX_BYTES,
): List<List<Message>> {
  val batches = mutableListOf(mutableListOf<Message>())
  var size = 0
  for (msg in messages) {
    val msgSize = msg.estimatedSize()
    if (size > 0 && size + msgSize > maxBytes) {
      batches += mutableListOf<Message>()
      size = 0
    }
    batches.last() += msg
    size += msgSize
  }
  return batches
}
