package aktual.budget.encryption

import aktual.budget.model.BudgetFiles
import aktual.budget.model.BudgetId
import aktual.budget.model.decryptedZip
import alakazam.kotlin.core.CoroutineContexts
import alakazam.kotlin.core.requireMessage
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import logcat.logcat
import okio.Buffer
import okio.Path
import okio.Sink
import okio.Source

// Adapted from packages/loot-core/src/server/encryption/encryption-internals.ts

/**
 * Decrypts a file at the given [filePath] and creates another at [decryptedZip].
 */
fun interface FileDecrypter {
  suspend operator fun invoke(id: BudgetId, meta: Meta, filePath: Path): DecryptResult
}

/**
 * Decrypts a [Buffer] of bytes and returns another containing the decrypted data
 */
fun interface BufferDecrypter {
  suspend operator fun invoke(meta: Meta, buffer: Buffer): DecryptResult
}

@Inject
@ContributesBinding(AppScope::class)
class FileDecrypterImpl(
  private val contexts: CoroutineContexts,
  private val keys: EncryptionKeys,
  private val files: BudgetFiles,
) : FileDecrypter {
  override suspend fun invoke(id: BudgetId, meta: Meta, filePath: Path): DecryptResult {
    val decryptedPath = files.decryptedZip(id, mkdirs = true)
    logcat.d { "Decrypting $id from $filePath into $decryptedPath with $meta" }
    return decrypt(
      contexts = contexts,
      keys = keys,
      meta = meta,
      source = files.fileSystem.source(filePath),
      sink = files.fileSystem.sink(decryptedPath),
      result = { DecryptResult.DecryptedFile(decryptedPath) },
    )
  }
}

@Inject
@ContributesBinding(AppScope::class)
class BufferDecrypterImpl(
  private val contexts: CoroutineContexts,
  private val keys: EncryptionKeys,
) : BufferDecrypter {
  override suspend operator fun invoke(meta: Meta, buffer: Buffer): DecryptResult {
    logcat.d { "Decrypting ${buffer.size} bytes with $meta" }
    val output = Buffer()
    return decrypt(
      contexts = contexts,
      keys = keys,
      meta = meta,
      source = buffer,
      sink = output,
      result = { DecryptResult.DecryptedBuffer(output) },
    )
  }
}

private suspend fun decrypt(
  keys: EncryptionKeys,
  contexts: CoroutineContexts,
  meta: Meta,
  source: Source,
  sink: Sink,
  result: () -> DecryptResult.Success,
): DecryptResult = try {
  val key = keys[meta.keyId] ?: return DecryptResult.MissingKey
  withContext(contexts.io) {
    source.decryptToSink(
      key = key.decode(),
      iv = meta.iv.decode(),
      authTag = meta.authTag.decode(),
      algorithm = meta.algorithm,
      sink = sink,
    )
  }
  result()
} catch (e: UnknownAlgorithmException) {
  DecryptResult.UnknownAlgorithm(e.algorithm)
} catch (e: CancellationException) {
  throw e
} catch (e: Exception) {
  DecryptResult.OtherFailure(e.requireMessage())
} finally {
  if (sink !is Buffer) runCatching { sink.close() }
  if (source !is Buffer) runCatching { source.close() }
}
